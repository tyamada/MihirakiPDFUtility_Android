#!/usr/bin/env python3
"""Safety and result-reporting regressions; never connects to a real device."""
import contextlib
import importlib.util
import io
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

spec = importlib.util.spec_from_file_location('runner', Path(__file__).with_name('run_device_tests_and_save.py'))
r = importlib.util.module_from_spec(spec)
spec.loader.exec_module(r)

VERSION = 'versionCode=43 minSdk=26\nversionName=2.1.4\ninstallerPackageName=com.android.vending'

class DeviceRunnerTests(unittest.TestCase):
    def test_version_from_device_not_source(self):
        self.assertEqual(r.parse_version(VERSION), {'name': '2.1.4', 'code': 43})
        self.assertIsNone(r.parse_version('Unable to find package'))

    def test_requires_serial(self):
        with contextlib.redirect_stderr(io.StringIO()), self.assertRaises(SystemExit):
            r.parser().parse_args([])

    def test_clean_requires_matching_confirmation_before_commands(self):
        for argv in [['--serial', 'A', '--clean-install'], ['--serial', 'A', '--mode', 'debug', '--clean-install', '--confirm-clean', 'B']]:
            with patch.object(r, 'run_cmd') as cmd, contextlib.redirect_stderr(io.StringIO()), self.assertRaises(SystemExit):
                r.main(argv)
            cmd.assert_not_called()

    def test_collect_does_not_mutate_or_launch(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            verification = root / 'VERIFICATION_LIST.md'
            verification.write_text('baseline')
            calls = []
            def fake(cmd, **kwargs):
                calls.append((cmd, kwargs))
                if 'get-state' in cmd: return 0, 'device\n'
                if 'getprop' in cmd: return 0, 'test-device\n'
                return 0, VERSION
            with patch.object(r, 'run_cmd', side_effect=fake), contextlib.redirect_stdout(io.StringIO()):
                self.assertEqual(r.main(['--serial', 'A'], root), 0)
            data = json.loads(next((root/'logs').glob('*/result.json')).read_text())
            self.assertEqual(data['result'], 'collected_manual_checks_pending')
            self.assertEqual(data['installed_version']['code'], 43)
            self.assertEqual(verification.read_text(), 'baseline')
            self.assertTrue(all(c[:3] == ['adb', '-s', 'A'] for c, _ in calls))
            self.assertTrue(all(k['env']['ANDROID_SERIAL'] == 'A' for _, k in calls))
            self.assertFalse(any(set(c) & {'install', 'uninstall', 'start', 'logcat'} for c,_ in calls))

    def test_failed_collection_nonzero_and_recorded(self):
        with tempfile.TemporaryDirectory() as tmp, patch.object(r, 'run_cmd', return_value=(1, 'offline')), contextlib.redirect_stdout(io.StringIO()):
            self.assertEqual(r.main(['--serial', 'A'], Path(tmp)), 1)
            data=json.loads(next(Path(tmp).glob('logs/*/result.json')).read_text())
            self.assertEqual(data['result'], 'failed')

    def test_play_install_protected_in_debug_mode(self):
        with tempfile.TemporaryDirectory() as tmp:
            calls=[]
            def fake(cmd, **kwargs):
                calls.append(cmd)
                return 0, 'device' if 'get-state' in cmd else VERSION
            with patch.object(r, 'run_cmd', side_effect=fake), contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(io.StringIO()):
                self.assertEqual(r.main(['--serial','A','--mode','debug','--clean-install','--confirm-clean','A'],Path(tmp)),1)
            self.assertFalse(any(set(c) & {'install','uninstall','start'} for c in calls))

    def test_failure_from_any_required_stage_dominates(self):
        for failed in ['deployment','startup','unit','instrumentation','python_qa']:
            steps=[{'name':n,'status':'failed' if n==failed else 'passed'} for n in ['deployment','startup','unit','instrumentation','python_qa']]
            self.assertEqual(r.aggregate(steps,'debug'),'failed')
        self.assertEqual(r.aggregate([{'status':'not_run'}],'debug'),'incomplete')
        self.assertIn('manual_checks_pending',r.aggregate([{'status':'passed'}],'debug'))

    def test_junit_requires_executed_unskipped_tests(self):
        with tempfile.TemporaryDirectory() as tmp:
            p=Path(tmp)/'TEST-suite.xml'
            self.assertFalse(r.parse_test_results([])['passed'])
            for tests,failures,errors,skipped,expected in [(0,0,0,0,False),(2,1,0,0,False),(2,0,1,0,False),(2,0,0,1,False),(2,0,0,0,True)]:
                p.write_text(f'<testsuite tests="{tests}" failures="{failures}" errors="{errors}" skipped="{skipped}"/>')
                self.assertEqual(r.parse_test_results([p])['passed'],expected)

    def test_debug_flow_uses_actual_version_and_does_not_ignore_junit_failure(self):
        for unit_fails in (False, True):
            with self.subTest(unit_fails=unit_fails), tempfile.TemporaryDirectory() as tmp:
                root=Path(tmp); calls=[]
                # Old passing XML must be removed before the requested test run.
                old=root/'app/build/test-results/testDebugUnitTest/TEST-old.xml'
                old.parent.mkdir(parents=True);old.write_text('<testsuite tests="99"/>')
                def fake(cmd, **kwargs):
                    calls.append(cmd)
                    if 'get-state' in cmd: return 0,'device'
                    if 'getprop' in cmd: return 0,'Test'
                    if 'dumpsys' in cmd: return 0,'versionCode=44 minSdk=26\nversionName=2.1.5\ninstallerPackageName=com.android.shell\ncom.android.vending.BILLING'
                    if 'install' in cmd:return 0,'Success'
                    if 'start' in cmd:return 0,'Status: ok'
                    if 'pidof' in cmd:return 0,'123'
                    for task,dir in [('app:testDebugUnitTest','app/build/test-results/testDebugUnitTest'),('app:connectedDebugAndroidTest','app/build/outputs/androidTest-results/connected')]:
                        if task in cmd:
                            target=root/dir/'TEST-current.xml';target.parent.mkdir(parents=True,exist_ok=True)
                            fail=1 if unit_fails and task=='app:testDebugUnitTest' else 0
                            target.write_text(f'<testsuite tests="2" failures="{fail}"/>')
                    return 0,''
                with patch.object(r,'run_cmd',side_effect=fake), contextlib.redirect_stdout(io.StringIO()):
                    code=r.main(['--serial','A','--mode','debug'],root)
                self.assertEqual(code,1 if unit_fails else 0)
                data=json.loads(next(root.glob('logs/*/result.json')).read_text())
                self.assertEqual(data['installed_version']['code'],44)
                self.assertEqual(data['unit_junit']['tests'],2)
                self.assertEqual(data['result'],'failed' if unit_fails else 'automated_checks_passed_manual_checks_pending')
                self.assertFalse(any('uninstall' in c for c in calls))
                self.assertFalse((root/'VERIFICATION_LIST.md').exists())

    def test_deployment_failure_never_triggers_uninstall(self):
        with tempfile.TemporaryDirectory() as tmp:
            root=Path(tmp);calls=[]
            def fake(cmd,**kwargs):
                calls.append(cmd)
                if 'get-state' in cmd:return 0,'device'
                if 'dumpsys' in cmd:return 0,VERSION.replace('com.android.vending','com.android.shell')
                if 'install' in cmd:return 1,'INSTALL_FAILED_UPDATE_INCOMPATIBLE'
                return 0,'Test'
            with patch.object(r,'run_cmd',side_effect=fake),contextlib.redirect_stdout(io.StringIO()):
                self.assertEqual(r.main(['--serial','A','--mode','debug'],root),1)
            self.assertFalse(any('uninstall' in c for c in calls))

if __name__ == '__main__':
    unittest.main()
