#!/usr/bin/env python3
"""Collect installed-app evidence by default; run Debug tests only on request.

Never updates VERIFICATION_LIST.md: automated checks do not certify manual QA.
"""
import argparse
from datetime import datetime
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET

PACKAGE = 'com.takumayamada22.pdfutility'
ACTIVITY = PACKAGE + '/com.takumayamada22.pdfutility.MainActivity'


def get_env():
    env = os.environ.copy()
    sdk = env.get('ANDROID_HOME') or env.get('ANDROID_SDK_ROOT')
    if sdk:
        env['PATH'] = str(Path(sdk) / 'platform-tools') + os.pathsep + env.get('PATH', '')
    return env


def run_cmd(cmd, cwd=None, env=None):
    try:
        p = subprocess.run(cmd, cwd=cwd, env=env or get_env(), capture_output=True,
                           text=True, timeout=1800)
        return p.returncode, p.stdout + p.stderr
    except (OSError, subprocess.TimeoutExpired) as e:
        return 1, str(e)


def parse_version(text):
    code = re.search(r'\bversionCode=(\d+)', text)
    name = re.search(r'\bversionName=([^\s]+)', text)
    return {'name': name.group(1), 'code': int(code.group(1))} if code and name else None


def parse_test_results(paths):
    """Require actual JUnit results; a successful no-op Gradle task is insufficient."""
    count = failures = errors = skipped = 0
    for path in paths:
        node = ET.parse(path).getroot()
        suites = [node] if node.tag == 'testsuite' else list(node.iter('testsuite'))
        for suite in suites:
            count += int(suite.get('tests', 0))
            failures += int(suite.get('failures', 0))
            errors += int(suite.get('errors', 0))
            skipped += int(suite.get('skipped', 0))
    return {'tests': count, 'failures': failures, 'errors': errors, 'skipped': skipped,
            'passed': count > 0 and failures == 0 and errors == 0 and skipped == 0}


def aggregate(steps, mode):
    if any(s['status'] == 'failed' for s in steps):
        return 'failed'
    if any(s['status'] == 'not_run' for s in steps):
        return 'incomplete'
    return 'collected_manual_checks_pending' if mode == 'collect' else 'automated_checks_passed_manual_checks_pending'


def parser():
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument('--serial', required=True, help='Exact adb device serial; never chooses the first device')
    p.add_argument('--mode', choices=['collect', 'debug'], default='collect')
    p.add_argument('--clean-install', action='store_true', help='Debug only: deletes app data on a dedicated test device')
    p.add_argument('--confirm-clean', help='Repeat the selected serial to acknowledge data deletion')
    p.add_argument('--output', type=Path, help='New output directory (must not exist)')
    return p


def main(argv=None, root=None):
    p = parser()
    args = p.parse_args(argv)
    if args.clean_install and (args.mode != 'debug' or args.confirm_clean != args.serial):
        p.error('--clean-install requires --mode debug and --confirm-clean matching --serial')
    root = root or Path(__file__).resolve().parent.parent
    stamp = datetime.now().strftime('%Y%m%d_%H%M%S_%f')
    safe_serial = re.sub(r'[^A-Za-z0-9_.-]', '_', args.serial)
    dest = args.output or root / 'logs' / f'{stamp}_{safe_serial}'
    dest.mkdir(parents=True, exist_ok=False)
    env = get_env()
    env['ANDROID_SERIAL'] = args.serial
    adb = ['adb', '-s', args.serial]
    record = {'timestamp': datetime.now().astimezone().isoformat(), 'serial': args.serial,
              'mode': args.mode, 'installed_version': None, 'steps': [],
              'manual_checks': 'not_run', 'verification_list_updated': False}

    def step(name, cmd, valid=None):
        code, output = run_cmd(cmd, cwd=root, env=env)
        ok = code == 0 and (valid(output) if valid else True)
        (dest / (name + '.log')).write_text(output)
        record['steps'].append({'name': name, 'status': 'passed' if ok else 'failed', 'exit_code': code})
        return ok, output

    def finish():
        record['result'] = aggregate(record['steps'], args.mode)
        (dest / 'result.json').write_text(json.dumps(record, ensure_ascii=False, indent=2) + '\n')
        (dest / 'summary_report.txt').write_text(
            f"Result: {record['result']}\nMode: {args.mode}\nInstalled version: {record['installed_version']}\n"
            + '\n'.join(f"{s['name']}: {s['status']}" for s in record['steps'])
            + '\nManual Play upgrade, purchase restoration and TalkBack checks: NOT RUN\n'
              'VERIFICATION_LIST.md was not modified. Review logs before sharing.\n')
        print(f"{record['result']}: {dest}")
        return 1 if record['result'] in ['failed', 'incomplete'] else 0

    ok, _ = step('device', adb + ['get-state'], lambda x: x.strip() == 'device')
    if not ok:
        return finish()
    for key, prop in [('model', 'ro.product.model'), ('android', 'ro.build.version.release')]:
        ok, text = step(key, adb + ['shell', 'getprop', prop], lambda x: bool(x.strip()))
        record[key] = text.strip() if ok else None

    ok, text = step('package_before', adb + ['shell', 'dumpsys', 'package', PACKAGE],
                    lambda x: parse_version(x) is not None or (args.mode == 'debug' and 'Unable to find package' in x))
    record['installed_version'] = parse_version(text) if ok else None
    if args.mode == 'collect':
        # No launch, install, uninstall, log clearing, Gradle or app state changes.
        record['steps'].extend({'name': n, 'status': 'not_applicable'} for n in ['deployment', 'unit', 'instrumentation', 'python_qa'])
        return finish()

    # Debug suites change state. Keep Play-installed apps out of this workflow.
    if re.search(r'\binstallerPackageName=com\.android\.vending(?:\s|$)', text):
        record['steps'].append({'name': 'protect_play_install', 'status': 'failed'})
        print('Use a dedicated Debug test device; the selected device has a Play installation.', file=sys.stderr)
        return finish()
    if not ok:
        return finish()
    gradlew = str(root / ('gradlew.bat' if os.name == 'nt' else 'gradlew'))
    ok, _ = step('build_debug', [gradlew, 'app:assembleDebug', 'app:assembleDebugAndroidTest'])
    if not ok:
        record['steps'].extend({'name': n, 'status': 'not_run'} for n in ['deployment', 'startup', 'unit', 'instrumentation', 'python_qa'])
        return finish()
    apk = root / 'app/build/outputs/apk/debug/app-debug.apk'
    if args.clean_install and record['installed_version']:
        ok, _ = step('uninstall', adb + ['uninstall', PACKAGE], lambda x: 'Success' in x)
        if not ok:
            return finish()
    ok, _ = step('deployment', adb + ['install', '-r', str(apk)], lambda x: 'Success' in x)
    if not ok:
        return finish()
    ok, text = step('package_after', adb + ['shell', 'dumpsys', 'package', PACKAGE], lambda x: parse_version(x) is not None)
    record['installed_version'] = parse_version(text) if ok else None
    step('startup', adb + ['shell', 'am', 'start', '-W', '-n', ACTIVITY], lambda x: 'Status: ok' in x and 'Error' not in x)
    ok, pid = step('process', adb + ['shell', 'pidof', PACKAGE], lambda x: bool(re.fullmatch(r'\d+(\s+\d+)*', x.strip())))
    if ok:
        step('app_log', adb + ['logcat', '-d', '--pid=' + pid.split()[0], '-t', '500'])
    # Rerun tasks to prevent stale/no-op XML from certifying a new run.
    for name, task, result_dir in [
        ('unit', 'app:testDebugUnitTest', 'app/build/test-results/testDebugUnitTest'),
        ('instrumentation', 'app:connectedDebugAndroidTest', 'app/build/outputs/androidTest-results/connected')]:
        if (root / result_dir).exists():
            shutil.rmtree(root / result_dir)
        ok, _ = step(name, [gradlew, task, '--rerun-tasks'])
        paths = list((root / result_dir).rglob('TEST-*.xml'))
        try:
            data = parse_test_results(paths)
        except (ET.ParseError, ValueError, OSError) as e:
            data = {'passed': False, 'error': str(e)}
        record[name + '_junit'] = data
        if not ok or not data['passed']:
            record['steps'][-1]['status'] = 'failed'
        if paths:
            shutil.copytree(root / result_dir, dest / (name + '_results'))
    for script in ['test_capture_store_screenshots.py', 'test_review_store_screenshots.py']:
        step(script[:-3], [sys.executable, str(root / 'scripts' / script)])
    return finish()


if __name__ == '__main__':
    sys.exit(main())
