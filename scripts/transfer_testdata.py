#!/usr/bin/env python3
"""Transfer verified selected fixed-release PDFs and explicit regression inputs.

Prepare manga separately with sample_pdf_inputs.py --download. No repository
PDF downloads, no device directory clearing, and no silent missing-file skips.
"""
import argparse
from pathlib import Path
import subprocess
from sample_pdf_inputs import catalog, resolve_samples, stage_device_file

ROOT = Path(__file__).resolve().parents[1]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--serial', required=True)
    parser.add_argument('--input-dir', type=Path)
    parser.add_argument('--sample-id', action='append', help='Explicit samples; default is all catalog samples')
    parser.add_argument('--test-input-dir', type=Path, default=ROOT / 'testdata')
    parser.add_argument('--regression-file', action='append', default=[])
    args = parser.parse_args()
    entries = catalog()
    ids = args.sample_id or list(entries)
    samples = resolve_samples(ids, args.input_dir)
    sources = [(path, entries[i]['local_filename'], 'Documents/MihirakiSamplePdfs') for i, path in samples.items()]
    for name in args.regression_file:
        if Path(name).name != name or name.startswith('tameshibu'):
            parser.error('Regression filenames must be simple non-manga names')
        path = args.test_input_dir / name
        if not path.is_file() or path.stat().st_size == 0:
            parser.error('Required regression input missing: ' + str(path))
        sources.append((path, name, 'testdata'))
    # All inputs must be verified before even listing devices.
    devices = subprocess.check_output(['adb', 'devices'], text=True)
    if args.serial + '\tdevice' not in devices.splitlines():
        parser.error('Selected device is not connected/authorized')
    base = '/sdcard/Android/data/com.takumayamada22.pdfutility/files/'
    for path, name, folder in sources:
        stage_device_file(args.serial, path, base + folder + '/' + name)
    print(f'Transferred all {len(sources)} verified/preflighted inputs; no device files deleted')


if __name__ == '__main__':
    main()
