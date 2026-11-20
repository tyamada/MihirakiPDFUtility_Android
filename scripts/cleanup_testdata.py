#!/usr/bin/env python3
"""Legacy destructive cleanup is disabled; no files are copied or deleted.

Inventory and per-file release/deployed-app dependency gates are required before
any approved manga cleanup. This is deliberately not a migration shortcut.
"""
import sys


def main():
    print('ERROR: legacy testdata cleanup is disabled. Inventory each PDF and verify deployed-app dependencies before any explicit deletion.', file=sys.stderr)
    return 2


if __name__ == '__main__':
    raise SystemExit(main())
