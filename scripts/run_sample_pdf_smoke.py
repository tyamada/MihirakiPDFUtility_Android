"""Build APKs first, then run catalog smoke tests on the dedicated Smartphone_6.3 AVD."""
from pathlib import Path
import sys, re
sys.dont_write_bytecode = True
root=Path(__file__).resolve().parents[1]
sys.path.insert(0,str(root/'scripts'))
import capture_store_screenshots as c
out=root/'.artifacts/sample-validation-v33'
out.mkdir(parents=True,exist_ok=True)
c.WORK.mkdir(parents=True,exist_ok=True)
d=c.Device('Smartphone_6.3')
try:
 print('Starting dedicated emulator',flush=True)
 d.start()
 print('Device: '+d.serial,flush=True)
 for apk in ['app/build/outputs/apk/debug/app-debug.apk','app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk']:
  d.adb('install','-r','-t',root/apk,timeout=180)
 d.adb('shell','run-as',c.PACKAGE,'mkdir','-p','files/sample_smoke')
 catalog=(root/'app/src/main/java/com/github/tyamada/mihirakipdfviewer_android/data/SamplePdfCatalog.kt').read_text()
 for name in re.findall(r'fileName = "([^"]+)"',catalog):
  source=root/'testdata'/name
  staged='/data/local/tmp/sample-smoke.pdf'
  d.adb('push',source,staged)
  target='files/sample_smoke/'+name
  d.adb('shell','run-as',c.PACKAGE,'cp',staged,target)
  assert d.text('shell','run-as',c.PACKAGE,'sha256sum',target).split()[0]==c.sha(source)
 print('All 28 PDFs staged and hash verified',flush=True)
 result=d.adb('shell','am','instrument','-w','-r','-e','class','com.github.tyamada.mihirakipdfviewer_android.SamplePdfSmokeTest','-e','sampleSmoke','true',c.RUNNER,check=False,timeout=900)
 log=result.stdout.decode(errors='replace')+'\n'+result.stderr.decode(errors='replace')
 (out/'instrumentation.log').write_text(log)
 print(log,flush=True)
 assert 'OK (3 tests)' in log and 'FAILURES!!!' not in log, 'Smoke tests failed'
 print('PASS: version 33 sample smoke',flush=True)
finally:
 d.stop()
