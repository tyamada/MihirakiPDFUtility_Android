#!/usr/bin/env python3
"""Legacy Viewer smoke runner: intentionally unavailable for Android Utility.

It used an unrelated application/test class and 28 repository-local PDFs.
Use Utility's reviewed instrumentation suite with externally prepared inputs;
this command must fail, never silently skip or install an unrelated package.
"""

def main():
    raise RuntimeError('Legacy sample smoke runner targets a different application; disabled for Utility. No tests were run.')

if __name__ == '__main__':
    main()
