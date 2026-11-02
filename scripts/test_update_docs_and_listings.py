#!/usr/bin/env python3
"""Prevent release-document hooks from relabeling historical versions."""
import importlib.util
from pathlib import Path
import tempfile
import unittest
spec=importlib.util.spec_from_file_location('docs_sync',Path(__file__).with_name('update_docs_and_listings.py'))
m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)

class DocumentHistoryTests(unittest.TestCase):
    def fixture(self, root):
        p=root/'app/build.gradle.kts';p.parent.mkdir(parents=True);p.write_text('versionName = "2.1.4"\nversionCode = 44')
        for lang in ['values','values-ja']:
            p=root/'app/src/main/res'/lang/'strings.xml';p.parent.mkdir(parents=True)
            p.write_text('<resources>'+''.join(f'<string name="{key}">Help</string>' for key in ['help_item_open_save','help_item_page_ops','help_item_security','help_item_support'])+'</resources>')
        p=root/'README.md';p.write_text('v1.4.0 (Code 22)\nv2.1.3 (コード42)\n'+m.START+'2.1.4 (43)'+m.END)
        return p
    def test_default_does_not_write_even_when_update_pending(self):
        with tempfile.TemporaryDirectory() as t:
            root=Path(t);p=self.fixture(root);before=p.read_bytes()
            self.assertEqual(m.sync_documents(root),['README.md']);self.assertEqual(p.read_bytes(),before)
    def test_explicit_write_preserves_history_and_other_code_mentions(self):
        with tempfile.TemporaryDirectory() as t:
            root=Path(t);p=self.fixture(root);m.sync_documents(root,write=True)
            self.assertIn('v1.4.0 (Code 22)',p.read_text());self.assertIn('v2.1.3 (コード42)',p.read_text())
            self.assertIn(m.START+'2.1.4 (44)'+m.END,p.read_text())
            self.assertEqual(m.sync_documents(root),[])
    def test_unmarked_history_is_unchanged(self):
        text='v1.4.0 Code 22, v2.1.3 コード42'
        self.assertEqual(m.current_version_text(text,'2.1.4','44'),text)
    def test_missing_version_never_falls_back(self):
        with tempfile.TemporaryDirectory() as t:
            root=Path(t);self.fixture(root);(root/'app/build.gradle.kts').write_text('versionCode = 44')
            with self.assertRaises(ValueError):m.get_gradle_version(root)
    def test_bad_marker_or_missing_help_fails(self):
        with self.assertRaises(ValueError):m.current_version_text(m.START+'old','2.1.4','44')
        with tempfile.TemporaryDirectory() as t:
            root=Path(t);self.fixture(root);(root/'app/src/main/res/values/strings.xml').write_text('<resources/>')
            with self.assertRaises(ValueError):m.sync_documents(root)

if __name__=='__main__':unittest.main()
