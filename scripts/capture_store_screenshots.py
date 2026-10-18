#!/usr/bin/env python3
"""Reproducible store captures. Run --help; no Play Console upload is performed."""
import argparse
import fcntl
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import sys
import time
import uuid

ROOT = Path(__file__).resolve().parents[1]
PACKAGE = 'com.github.tyamada.MihirakiPDFViewer_Android'
RUNNER = PACKAGE + '.test/androidx.test.runner.AndroidJUnitRunner'
TEST = 'com.github.tyamada.mihirakipdfviewer_android.StoreScreenshotTest#captureStoreScreenshots'
CONFIG_PATH = Path(__file__).with_name('store_capture_config.json')
CONFIG = json.loads(CONFIG_PATH.read_text())
PROFILES = CONFIG['devices']
DEVICES = {name: name for name in PROFILES}
LANGUAGES = CONFIG['languages']
SCENES = list(dict.fromkeys(scene for profile in PROFILES.values() for scene in profile['scenes']))
OUT = ROOT / 'store_listing/screenshots'
WORK = ROOT / '.artifacts/store-capture'
SDK = Path(os.environ.get('ANDROID_SDK_ROOT') or os.environ.get('ANDROID_HOME') or Path.home() / 'Library/Android/sdk')
ADB = str(SDK / 'platform-tools/adb')
EMULATOR = str(SDK / 'emulator/emulator')


def run(argv, *, data=None, timeout=120, check=True):
    result = subprocess.run([str(x) for x in argv], input=data, capture_output=True, timeout=timeout, cwd=ROOT)
    if check and result.returncode:
        raise RuntimeError(f'Command failed ({result.returncode}): {argv}\n{result.stdout.decode(errors="replace")}\n{result.stderr.decode(errors="replace")}')
    return result


def same_locale(value, expected):
    primary = value.split(',')[0].lower().replace('zh-hans-cn', 'zh-cn')
    return primary == expected.lower()


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def orientation(avd, scene):
    return PROFILES[avd]['scenes'][scene]


def selected_scenes(avd, requested=None):
    supported = list(PROFILES[avd]['scenes'])
    if requested and any(scene not in supported for scene in requested):
        raise ValueError(f'Unsupported scene for {avd}: {requested}')
    return requested or supported


def fingerprint(apks):
    return hashlib.sha256((''.join(sha(p) for p in apks) + sha(Path(__file__)) + sha(CONFIG_PATH)).encode()).hexdigest()


def expected_size(avd, portrait):
    size = sorted(PROFILES[avd]['size'])
    return tuple(size if portrait else size[::-1])


def validate_image(path, expected_orientation, search=False, dimensions=None):
    from PIL import Image, ImageStat
    with Image.open(path) as raw:
        raw.load()
        image = raw.convert('RGB')
        if raw.format != 'PNG' or raw.mode != 'RGB' or min(image.size) < 1080 or max(image.size) > 3840 or (dimensions and image.size != tuple(dimensions)):
            raise RuntimeError('Invalid PNG/dimensions')
        if (image.height > image.width) != (expected_orientation == 'portrait'):
            raise RuntimeError('Image orientation mismatch')
        # Inspect the content area, avoiding status/navigation bars. White manga pages
        # are valid; a near-uniform blank image is not.
        body = image.crop((0, int(image.height * .12), image.width, int(image.height * .9)))
        gray = body.convert('L')
        hist = gray.histogram()
        pixels = body.width * body.height
        if sum(hist[:10]) / pixels > .99 or sum(hist[246:]) / pixels > .99 or ImageStat.Stat(gray).stddev[0] < 3:
            raise RuntimeError('Blank/black/white content image')
        if search:
            red = sum(1 for r, g, b in (body.get_flattened_data() if hasattr(body, 'get_flattened_data') else body.getdata()) if r > 180 and g < 110 and b < 110)
            if red < 8:
                raise RuntimeError('Search highlight missing in screenshot')



def can_resume(path, record, expected, compatible=()):
    try:
        info = json.loads(record.read_text())
        if not isinstance(info, dict):
            return False
        if info.get('fingerprint') != expected['fingerprint'] and info.get('fingerprint') not in compatible:
            return False
        if any(info.get(k) != v for k, v in expected.items() if k != 'fingerprint') or info.get('imageSha256') != sha(path):
            return False
        if expected['scene'] == '07_outline' and not info.get('outlineVerified'):
            return False
        if PROFILES.get(expected.get('avd'), {}).get('desktop') and expected['scene'] == '05_page_navigation' and not info.get('desktopInputVerified'):
            return False
        if PROFILES.get(expected.get('avd'), {}).get('desktop') and not info.get('maximized'):
            return False
        validate_image(path, 'portrait' if expected['portrait'] else 'landscape', expected['scene'] == '04_search', expected.get('dimensions'))
        return True
    except (OSError, ValueError, RuntimeError):
        return False


class Device:
    def __init__(self, avd):
        self.avd = avd
        self.serial = None
        self.process = None
        self.log = None
        self.original_display = None
        self.owned = None

    def adb(self, *args, **kwargs):
        return run([ADB, '-s', self.serial, *args], **kwargs)

    def text(self, *args, **kwargs):
        return self.adb(*args, **kwargs).stdout.decode().strip()

    def boot(self):
        deadline = time.monotonic() + 240
        while time.monotonic() < deadline:
            state = self.text('shell', 'getprop', 'sys.boot_completed', check=False, timeout=10)
            if state == '1':
                self.adb('shell', 'input', 'keyevent', 'KEYCODE_WAKEUP')
                self.adb('shell', 'wm', 'dismiss-keyguard')
                return
            time.sleep(2)
        raise RuntimeError('Emulator boot timeout')

    def start(self):
        run([ADB, 'start-server'])
        devices = run([ADB, 'devices']).stdout.decode().splitlines()[1:]
        for line in devices:
            parts = line.split()
            if len(parts) == 2 and parts[1] == 'device' and parts[0].startswith('emulator-'):
                names = run([ADB, '-s', parts[0], 'emu', 'avd', 'name'], check=False).stdout.decode().splitlines()
                # A just-shut-down emulator can remain briefly in `adb devices`
                # while its emulator-console name query returns an empty result.
                if not names or not names[0].strip():
                    continue
                name = names[0]
                if name == self.avd:
                    if self.owned is None:
                        self.owned = False
                    self.serial = parts[0]
                    self.boot()
                    return
        # Use an unused emulator port; leave unrelated devices running.
        used = {line.split()[0] for line in devices if line.strip()}
        port = next(p for p in range(5580, 5680, 2) if f'emulator-{p}' not in used)
        if self.owned is None:
            self.owned = True
        self.serial = f'emulator-{port}'
        self.log = (WORK / f'{self.avd}-emulator.log').open('ab')
        self.process = subprocess.Popen([EMULATOR, '-avd', self.avd, '-port', str(port), '-no-snapshot', '-no-boot-anim', '-no-audio'], stdout=self.log, stderr=subprocess.STDOUT, start_new_session=True)
        self.boot()

    def stop(self):
        # Only shut down an emulator launched by this process.
        if self.process and self.owned:
            self.adb('emu', 'kill', check=False)
            try:
                self.process.wait(timeout=20)
            except subprocess.TimeoutExpired:
                self.process.terminate()
        if self.log:
            self.log.close()

    def cold_restart(self):
        # Android 15 tablet images can retain an unwritable shared-storage mount
        # after adb reboot. A full emulator restart also reinitializes that mount.
        serial = self.serial
        self.adb('emu', 'kill')
        if self.process:
            self.process.wait(timeout=30)
        deadline = time.monotonic() + 30
        while serial in run([ADB, 'devices']).stdout.decode():
            if time.monotonic() > deadline:
                raise RuntimeError('Emulator shutdown timeout')
            time.sleep(1)
        if self.log:
            self.log.close()
        self.process = None
        self.log = None
        time.sleep(2)
        self.start()

    def locale(self, locale):
        current = self.text('shell', 'getprop', 'persist.sys.locale')
        setting = self.text('shell', 'settings', 'get', 'system', 'system_locales')
        print(f'  System locale: property={current}, settings={setting}, target={locale}', flush=True)
        if not same_locale(current, locale) or not same_locale(setting, locale):
            # These supplied Google APIs AVDs support adb root. Do not silently
            # continue on a production image that rejects changing the system locale.
            self.adb('root')
            self.adb('wait-for-device')
            self.adb('shell', 'setprop', 'persist.sys.locale', locale)
            self.adb('shell', 'CLASSPATH=/data/local/tmp/store-capture-tests.apk',
                     'app_process', '/system/bin',
                     'com.github.tyamada.mihirakipdfviewer_android.StoreLocaleSetter', locale)
            self.cold_restart()
        # Root is only needed for locale configuration. Use the shell UID for
        # shared-storage transfers; Android 15 FUSE can reject root file creation.
        self.adb('unroot')
        self.adb('wait-for-device')
        if not same_locale(self.text('shell', 'getprop', 'persist.sys.locale'), locale):
            raise RuntimeError('System locale failed after reboot')
        # Some API 35 tablet images update the persistent locale and app
        # configuration while the legacy system_locales setting remains stale.
        # StoreScreenshotTest checks the actual Activity locale on every scene.
        setting = self.text('shell', 'settings', 'get', 'system', 'system_locales')
        if not same_locale(setting, locale):
            print(f'  Locale setting reports {setting}; Activity locale will be asserted by the test', flush=True)

    def save_display(self):
        self.original_display = {}
        for kind in ('size', 'density'):
            value = self.text('shell', 'wm', kind)
            match = re.search(r'Override .*?:\s*(\S+)', value)
            self.original_display[kind] = match.group(1) if match else 'reset'
        self.original_display['rotation'] = {
            key: self.text('shell', 'settings', 'get', 'system', key)
            for key in ('accelerometer_rotation', 'user_rotation')}
        self.original_display['wm_rotation'] = self.text('shell', 'wm', 'user-rotation')

    def restore_display(self):
        if self.original_display is None:
            return
        for kind in ('size', 'density'):
            self.adb('shell', 'wm', kind, self.original_display[kind])
        for key, value in self.original_display['rotation'].items():
            if value == 'null':
                self.adb('shell', 'settings', 'delete', 'system', key)
            else:
                self.adb('shell', 'settings', 'put', 'system', key, value)
        previous = self.original_display['wm_rotation']
        if 'free' in previous:
            self.adb('shell', 'wm', 'user-rotation', 'free')
        else:
            match = re.search(r'(?:lock|locked)\s+(\d)', previous)
            self.adb('shell', 'wm', 'user-rotation', 'lock', match.group(1) if match else self.original_display['rotation']['user_rotation'])

    def prepare_display(self):
        profile = PROFILES[self.avd]
        if profile['override']:
            self.adb('shell', 'wm', 'size', 'x'.join(map(str, profile['size'])))
            self.adb('shell', 'wm', 'density', str(profile['density']))
        size = self.text('shell', 'wm', 'size')
        actual = tuple(map(int, re.findall(r'(\d+)x(\d+)', size)[-1]))
        density = int(re.findall(r'\d+', self.text('shell', 'wm', 'density'))[-1])
        if actual != tuple(profile['size']) or density != profile['density']:
            raise RuntimeError(f'Display profile mismatch: {actual}, {density}')

    def rotate(self, target):
        self.prepare_display()
        self.adb('shell', 'cmd', 'uimode', 'night', 'no')
        if PROFILES[self.avd]['desktop']:
            return
        self.adb('shell', 'am', 'force-stop', PACKAGE)
        size = self.text('shell', 'wm', 'size')
        w, h = map(int, re.findall(r'(\d+)x(\d+)', size)[-1])
        natural_portrait = h > w
        rotation = '0' if natural_portrait == (target == 'portrait') else '1'
        self.adb('shell', 'settings', 'put', 'system', 'accelerometer_rotation', '0')
        self.adb('shell', 'settings', 'put', 'system', 'user_rotation', rotation)
        self.adb('shell', 'wm', 'user-rotation', 'lock', rotation)
        self.adb('shell', 'cmd', 'uimode', 'night', 'no')
        # Actual app configuration and PNG dimensions are additionally asserted.
        time.sleep(1)

    def transfer(self, lang, path, digest):
        # Use app-private storage on every device. Desktop images can deny shell
        # writes to Downloads; the test consumes only this hash-verified copy.
        self.adb('shell', 'run-as', PACKAGE, 'mkdir', '-p', 'files/store_capture')
        app_file = f'files/store_capture/{lang}.pdf'
        staged = f'/data/local/tmp/store-capture-{digest}.pdf'
        self.adb('push', path, staged)
        if self.text('shell', 'sha256sum', staged).split()[0] != digest:
            raise RuntimeError('Staged PDF hash mismatch')
        self.adb('shell', 'run-as', PACKAGE, 'cp', staged, app_file)
        if self.text('shell', 'run-as', PACKAGE, 'sha256sum', app_file).split()[0] != digest:
            raise RuntimeError('App-private PDF hash mismatch')



def capture(device, lang, scene, digest, target, attempt):
    token = uuid.uuid4().hex
    device.adb('shell', 'am', 'force-stop', PACKAGE)
    args = {'class': TEST, 'captureLanguage': lang, 'captureScene': scene,
            'captureDesktop': str(PROFILES[device.avd]['desktop']).lower(),
            'captureLocale': LANGUAGES[lang], 'capturePageCount': str(CONFIG['pageCount']),
            'captureOutlineFirst': CONFIG['outlineFirst'][lang], 'captureVersionCode': str(CONFIG['versionCode']),
            'captureOrientation': target, 'capturePdfSha256': digest, 'captureId': token}
    command = ['shell', 'am', 'instrument', '-w', '-r']
    for key, value in args.items():
        command.extend(['-e', key, value])
    command.append(RUNNER)
    result = device.adb(*command, timeout=240)
    output = result.stdout.decode(errors='replace')
    (WORK / f'{device.avd}-{lang}-{scene}-{attempt}.log').write_text(output)
    if 'OK (1 test)' not in output or 'FAILURES!!!' in output or 'INSTRUMENTATION_FAILED' in output:
        raise RuntimeError(f'Instrumentation failed; see {device.avd}-{lang}-{scene}-{attempt}.log\n{output[-3500:]}')
    base = f'files/store_capture/{token}'
    metadata = json.loads(device.adb('exec-out', 'run-as', PACKAGE, 'cat', base + '.json').stdout)
    expected = {'captureId': token, 'scene': scene, 'language': lang, 'pdfSha256': digest, 'portrait': target == 'portrait', 'package': PACKAGE}
    if any(metadata.get(k) != v for k, v in expected.items()):
        raise RuntimeError('Capture metadata mismatch')
    temp = WORK / f'{token}.png'
    temp.write_bytes(device.adb('exec-out', 'run-as', PACKAGE, 'cat', base + '.png').stdout)
    from PIL import Image
    with Image.open(temp) as raw:
        if 'A' in raw.getbands() and raw.getchannel('A').getextrema() != (255, 255):
            raise RuntimeError('Transparent screenshot')
        rgb = raw.convert('RGB')
    rgb.save(temp)

    validate_image(temp, target, scene == '04_search', expected_size(device.avd, target == 'portrait'))
    if metadata.get('layout') != ('SINGLE' if target == 'portrait' else 'SPREAD'):
        raise RuntimeError('Effective layout mismatch')
    if PROFILES[device.avd]['desktop'] and scene == '05_page_navigation' and not metadata.get('desktopInputVerified'):
        raise RuntimeError('Desktop input not verified')
    if metadata.get('versionCode') != CONFIG['versionCode']:
        raise RuntimeError('App version mismatch')
    if scene == '07_outline' and not metadata.get('outlineVerified'):
        raise RuntimeError('Outline not verified')
    if PROFILES[device.avd]['desktop'] and not metadata.get('maximized'):
        raise RuntimeError('Desktop window not maximized')
    return temp, metadata


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--device', choices=DEVICES, nargs='+', default=list(DEVICES))
    parser.add_argument('--language', choices=LANGUAGES, nargs='+', default=list(LANGUAGES))
    parser.add_argument('--scene', choices=SCENES, nargs='+', default=None)
    parser.add_argument('--resume', action='store_true', help='Skip only verified outputs matching APKs, PDFs and runner code')
    parser.add_argument('--skip-build', action='store_true', help='Use already built APKs')
    parser.add_argument('--keep-device', action='store_true', help='Leave the selected emulator running after capture')
    parser.add_argument('--clear-app-data', action='store_true', help='Clear application state on the selected emulator before capture')
    parser.add_argument('--compatible-fingerprint', action='append', default=[], help='Allow verified records from an equivalent capture runner revision')
    parser.add_argument('--retries', type=int, default=3)
    options = parser.parse_args()
    selections = {avd: selected_scenes(avd, options.scene) for avd in options.device}
    # Required before build, launch, or any adb action, including on resume.
    pdfs = {lang: ROOT / f'testdata/tameshibu_episode1_2_{lang}.pdf' for lang in options.language}
    missing = [str(path) for path in pdfs.values() if not path.is_file()]
    if missing:
        raise RuntimeError('FATAL: source PDFs missing: ' + ', '.join(missing))
    avd_root = Path.home() / '.android/avd'
    for avd in options.device:
        ini = dict(line.split('=', 1) for line in (avd_root / f'{avd}.ini').read_text().splitlines() if '=' in line)
        profile = dict(line.split('=', 1) for line in (Path(ini['path']) / 'config.ini').read_text().splitlines() if '=' in line)
        if not (SDK / profile['image.sysdir.1']).is_dir():
            raise RuntimeError(f'System image missing: {avd}')
    from PIL import Image  # fail before device operations when QA dependency is missing
    WORK.mkdir(parents=True, exist_ok=True)
    if options.retries < 1:
        parser.error('--retries must be at least 1')
    if not options.skip_build:
        env = os.environ.copy()
        env.pop('ANDROID_PREFS_ROOT', None)
        subprocess.run(['./gradlew', 'assembleDebug', 'assembleDebugAndroidTest'], cwd=ROOT, env=env, check=True)
    apks = [ROOT / 'app/build/outputs/apk/debug/app-debug.apk', ROOT / 'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk']
    stamp = fingerprint(apks)
    available = run([EMULATOR, '-list-avds']).stdout.decode().splitlines()
    if any(avd not in available for avd in options.device):
        raise RuntimeError('Required AVD missing')
    pending = [f'{avd}/{lang}/{scene}' for avd in options.device for lang in options.language for scene in selections[avd]]
    status_path = WORK / 'run-status.json'
    def status(error=None):
        status_path.write_text(json.dumps(dict(pending=pending, error=error, complete=not pending), ensure_ascii=False, indent=2)+'\n')
    status()
    for avd in options.device:
        device = Device(avd)
        try:
            print(f'Starting {avd}', flush=True)
            device.start()
            device.save_display()
            for apk in apks:
                device.adb('install', '-r', '-t', apk, timeout=180)
            device.adb('push', apks[1], '/data/local/tmp/store-capture-tests.apk')
            if options.clear_app_data:
                device.adb('shell', 'pm', 'clear', PACKAGE)
            for lang in options.language:
                # Always verify even when all images are already complete.
                device.locale(LANGUAGES[lang])
                device.rotate(orientation(avd, selections[avd][0]))
                digest = sha(pdfs[lang])
                device.transfer(lang, pdfs[lang], digest)
                folder = OUT / DEVICES[avd]
                folder.mkdir(parents=True, exist_ok=True)
                for scene in selections[avd]:
                    target = orientation(avd, scene)
                    final = folder / f'{lang}_{scene}_{target}.png'
                    record = final.with_suffix('.json')
                    expected = dict(fingerprint=stamp, pdfSha256=digest, avd=avd,
                                    language=lang, scene=scene, portrait=target == 'portrait',
                                    versionCode=CONFIG['versionCode'], dimensions=list(expected_size(avd, target == 'portrait')))
                    if options.resume and can_resume(final, record, expected, options.compatible_fingerprint):
                        pending.remove(f'{avd}/{lang}/{scene}')
                        status()
                        print(f'  Verified / skip {final.name}', flush=True)
                        continue
                    for attempt in range(1, options.retries + 1):
                        print(f'  Capture {avd} {lang} {scene} {target} attempt {attempt}', flush=True)
                        try:
                            # Full preflight on every retry/resumption.
                            device.locale(LANGUAGES[lang])
                            device.rotate(target)
                            device.transfer(lang, pdfs[lang], digest)
                            temp, info = capture(device, lang, scene, digest, target, attempt)
                            info.update(expected)
                            info.update(serial=device.serial,
                                        imageSha256=sha(temp), checkedAt=time.strftime('%Y-%m-%dT%H:%M:%S%z'),
                                        validation='automated-pass; visual review pending')
                            if any(other != final and sha(other) == sha(temp) for other in folder.glob(f'{lang}_*.png')):
                                raise RuntimeError('Duplicate screenshot from another scene')
                            temp.replace(final)
                            record.write_text(json.dumps(info, ensure_ascii=False, indent=2) + '\n')
                            pending.remove(f'{avd}/{lang}/{scene}')
                            status()
                            print(f'  Saved {final.relative_to(ROOT)}', flush=True)
                            break
                        except (RuntimeError, subprocess.SubprocessError, ValueError) as error:
                            print(f'  FAILED: {error}', flush=True)
                            if attempt == options.retries:
                                raise
                            if 'was:<[a]ndroid>' in str(error):
                                print('  Restarting the dedicated emulator before retry', flush=True)
                                device.cold_restart()
        except BaseException as error:
            status(str(error))
            raise
        finally:
            try:
                device.restore_display()
            except Exception as restore_error:
                status(f"{json.loads(status_path.read_text()).get('error')}; restore failed: {restore_error}")
                raise RuntimeError(f"Display restoration failed; see {status_path}: {restore_error}") from restore_error
            finally:
                if not options.keep_device:
                    device.stop()
    print('All selected captures passed automated checks. Visual review is still required.', flush=True)


if __name__ == '__main__':
    try:
        WORK.mkdir(parents=True, exist_ok=True)
        with (WORK / 'runner.lock').open('w') as lock:
            try:
                fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
            except BlockingIOError:
                raise RuntimeError('Another capture runner is active; run devices sequentially')
            main()
    except (RuntimeError, subprocess.SubprocessError, ValueError, OSError) as error:
        print(f'FATAL: {error}', file=sys.stderr)
        sys.exit(1)
