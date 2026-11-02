#!/usr/bin/env python3
"""Validate local store metadata fields; does not certify translation quality."""
from pathlib import Path
import json
root = Path(__file__).resolve().parents[1]
rows = []
for path in sorted((root / "store_listing").glob("metadata_*.txt")):
    lines = path.read_text().splitlines()
    assert len(lines) >= 8, f"Invalid metadata: {path.name}"
    fields = {"title": lines[1], "short": lines[4], "full": "\n".join(lines[7:]).strip()}
    lengths = {key: len(value) for key, value in fields.items()}
    valid = all(0 < lengths[key] <= limit for key, limit in {"title": 30, "short": 80, "full": 4000}.items())
    rows.append({"file": path.name, **lengths, "valid": valid})
print(json.dumps(rows, ensure_ascii=False, indent=2))
raise SystemExit(0 if rows and all(row["valid"] for row in rows) else 1)
