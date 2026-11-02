#!/usr/bin/env python3
"""Validate string keys, duplicates and format argument compatibility across locales."""
from pathlib import Path
import xml.etree.ElementTree as ET
import re,json
root=Path(__file__).resolve().parents[1]
def load(path):
    nodes=[x for x in ET.parse(path).getroot() if x.tag=='string']
    keys=[x.attrib['name'] for x in nodes]
    assert len(keys)==len(set(keys)), f'Duplicate keys: {path}'
    result = {x.attrib['name']: ''.join(x.itertext()) for x in nodes}
    assert all(value.strip() for value in result.values()), f'Empty strings: {path}'
    return result
def args(value):
    result=[]; position=0
    for m in re.finditer(r'%(?:(\d+)\$)?[\-#+ 0,(]*\d*(?:\.\d+)?([bBhHsScCdoxXeEfgGaAtTn%])',value):
        if m[2]=='%': continue
        position+=1;result.append((int(m[1] or position),m[2]))
    return sorted(result)
base=load(root/'app/src/main/res/values/strings.xml')
rows=[]
for f in sorted((root/'app/src/main/res').glob('values-*/strings.xml')):
    strings=load(f)
    missing=sorted(set(base)-set(strings))
    mismatch=[k for k in base.keys() & strings.keys() if args(base[k])!=args(strings[k])]
    rows.append({'locale':f.parent.name,'keys':len(strings),'missing':missing,'format_mismatch':sorted(mismatch)})
print(json.dumps(rows,ensure_ascii=False,indent=2))
raise SystemExit(0 if all(not r['missing'] and not r['format_mismatch'] for r in rows) else 1)
