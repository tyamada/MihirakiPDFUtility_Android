"""Small deterministic fixtures: no ADB, emulator, real PDFs, or network."""
import hashlib
import io
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
import sample_pdf_inputs as samples

PDF = b'%PDF-1.4 fixture only\n'

class Response(io.BytesIO):
    def geturl(self):
        return 'https://release-assets.githubusercontent.com/fixture.pdf'

class SampleInputsTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name) / 'repo'
        self.cache = Path(self.temp.name) / 'cache'
        entry = dict(sample_id='ep1_fil', filename='tameshibu_episode1_3_tl.pdf', new_download_url=samples.PREFIX+'tameshibu_episode1_3_tl.pdf', bytes=len(PDF), sha256=hashlib.sha256(PDF).hexdigest())
        self.manifest = self.root / samples.MANIFEST
        self.manifest.parent.mkdir(parents=True)
        self.manifest.write_text(json.dumps(dict(repository='tyamada/manga-prompt-kit', fixed_tag='manga-pdf-20261103-v1', pdf_count=1, entries=[entry])))
        old = self.root / samples.LEGACY
        old.parent.mkdir(parents=True)
        old.write_text(json.dumps(dict(entries=[dict(sample_id='ep1_fil',filename='tameshibu_episode1_3_fil.pdf')])))

    def prepare(self, opener=None):
        return samples.prepare(['ep1_fil'], self.cache, self.root, download=True, opener=opener or (lambda *a,**k: Response(PDF)))

    def test_downloads_selected_only_and_keeps_fil_alias(self):
        result = self.prepare()
        self.assertEqual(result['ep1_fil'].name, 'tameshibu_episode1_3_fil.pdf')
        self.assertEqual(result['ep1_fil'].read_bytes(), PDF)
        self.assertEqual(len(list(self.cache.iterdir())),1)

    def test_verified_cache_is_offline_reusable(self):
        self.prepare()
        with patch.object(samples, 'urlopen', side_effect=AssertionError('network forbidden')):
            self.assertTrue(samples.resolve_samples(['ep1_fil'], self.cache, self.root)['ep1_fil'].is_file())
        self.prepare(lambda *a,**k: self.fail('cached input must not be downloaded'))

    def test_no_implicit_download_and_unknown_id_fail(self):
        with self.assertRaisesRegex(ValueError,'explicit --download'):
            samples.prepare(['ep1_fil'],self.cache,self.root)
        with self.assertRaisesRegex(ValueError,'Unknown sample'):
            samples.resolve_samples(['unknown'],self.cache,self.root)

    def test_corrupt_cached_input_is_preserved_and_fails(self):
        self.cache.mkdir();p=self.cache/'tameshibu_episode1_3_fil.pdf';p.write_bytes(PDF.replace(b'fixture',b'corrupt'))
        before=p.read_bytes()
        with self.assertRaisesRegex(ValueError,'SHA-256'):
            self.prepare()
        self.assertEqual(p.read_bytes(),before)

    def test_wrong_download_hash_never_publishes(self):
        with self.assertRaisesRegex(ValueError,'SHA-256'):
            self.prepare(lambda *a,**k: Response(PDF.replace(b'fixture',b'corrupt')))
        self.assertEqual(list(self.cache.iterdir()),[])

    def test_oversize_download_and_bad_header_fail(self):
        with self.assertRaisesRegex(ValueError,'exceeds'):
            self.prepare(lambda *a,**k: Response(PDF+b'x'))
        with self.assertRaisesRegex(ValueError,'header'):
            self.prepare(lambda *a,**k: Response(b'xxxxx'+PDF[5:]))

    def test_repo_inputs_and_symlinks_are_rejected(self):
        with self.assertRaisesRegex(ValueError,'outside'):
            samples.input_directory(self.root/'docs/sample',self.root)
        self.cache.mkdir();(self.cache/'target.pdf').write_bytes(PDF);(self.cache/'tameshibu_episode1_3_fil.pdf').symlink_to(self.cache/'target.pdf')
        with self.assertRaises(ValueError):
            samples.resolve_samples(['ep1_fil'],self.cache,self.root)

    def test_release_asset_name_alias_is_accepted(self):
        self.cache.mkdir();(self.cache/'tameshibu_episode1_3_tl.pdf').write_bytes(PDF)
        self.assertEqual(samples.language_samples(['tl'],self.cache,self.root)['tl'].name,'tameshibu_episode1_3_tl.pdf')

    def test_invalid_manifest_duplicate_id_or_path_is_rejected(self):
        original=json.loads(self.manifest.read_text())
        for key,value in [('filename','../tameshibu.pdf'),('new_download_url','http://example.org/a.pdf'),('sha256','bad')]:
            data=json.loads(json.dumps(original));data['entries'][0][key]=value;self.manifest.write_text(json.dumps(data))
            with self.assertRaises(ValueError):samples.catalog(self.root)
        original['entries']*=2;self.manifest.write_text(json.dumps(original))
        with self.assertRaisesRegex(ValueError,'Duplicate'):samples.catalog(self.root)

    def test_download_redirect_away_from_https_is_rejected(self):
        class Bad(Response):
            def geturl(self):return 'http://example.org/a.pdf'
        with self.assertRaisesRegex(ValueError,'HTTPS'):
            self.prepare(lambda *a,**k: Bad(PDF))

class DeviceStagingTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.source = Path(self.temp.name) / 'source.pdf'
        self.source.write_bytes(PDF)
        self.digest = hashlib.sha256(PDF).hexdigest()

    def result(self, code=0, text=''):
        import subprocess
        return subprocess.CompletedProcess([], code, text, '')

    def test_existing_matching_device_file_is_reused_without_push(self):
        from unittest.mock import Mock
        runner=Mock(side_effect=[self.result(), self.result(text=self.digest+'  sample.pdf')])
        samples.stage_device_file('test-serial', self.source, '/safe/sample.pdf', runner)
        self.assertEqual(runner.call_count,2)
        self.assertFalse(any('push' in c.args[0] for c in runner.call_args_list))

    def test_existing_different_device_file_is_preserved_and_fails(self):
        from unittest.mock import Mock
        runner=Mock(side_effect=[self.result(), self.result(text='0'*64+'  sample.pdf')])
        with self.assertRaisesRegex(RuntimeError,'preserved without overwrite'):
            samples.stage_device_file('test-serial', self.source, '/safe/sample.pdf', runner)
        self.assertFalse(any('push' in c.args[0] or 'rm' in c.args[0] for c in runner.call_args_list))

    def test_new_device_file_uses_unique_staging_and_noclobber(self):
        calls=[]
        def runner(cmd, **kw):
            calls.append(cmd)
            if cmd[-4:] == ['shell','test','-e','/safe/sample.pdf']:
                return self.result(1)
            if 'sha256sum' in cmd:return self.result(text=self.digest+'  sample.pdf')
            return self.result()
        samples.stage_device_file('test-serial', self.source, '/safe/sample.pdf', runner)
        publish=[c[-1] for c in calls if c[-1].startswith('set -C;')]
        self.assertEqual(len(publish),1)
        self.assertIn('utility-input-',publish[0])
        removals=[c for c in calls if 'rm' in c]
        self.assertEqual(len(removals),1)
        self.assertIn('/.utility-input-',removals[0][-1])
        self.assertNotEqual(removals[0][-1],'/safe/sample.pdf')

if __name__=='__main__':unittest.main()
