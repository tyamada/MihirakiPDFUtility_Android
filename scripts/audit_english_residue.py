#!/usr/bin/env python3
"""Audit exact English residue and empty UI strings using explicit reviewed exceptions."""
from pathlib import Path
import json,re,xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
audit=json.loads((root/'docs/ENGLISH_RESIDUE_AUDIT_v6.json').read_text())
def norm(s): return re.sub(r'\s+',' ',s.replace(r'\"','"').replace(r"\'","'")).strip()
def load(f): return {x.attrib['name']:''.join(x.itertext()) for x in ET.parse(f).getroot() if x.tag=='string'}
base=load(root/'app/src/main/res/values/strings.xml')
allowed={(r['resource_folder'],r['key']):(norm(r['source']),r['reason']) for r in audit['retained']}
rows=[]
for f in sorted((root/'app/src/main/res').glob('values-*/strings.xml')):
    strings=load(f); pending=[]; empty=[]
    for k,source in base.items():
        value=strings.get(k,'')
        if not value.strip(): empty.append(k)
        elif norm(value)==norm(source) and allowed.get((f.parent.name,k),('',))[0]!=norm(source): pending.append(k)
    rows.append({'locale':f.parent.name,'unclassified_english':pending,'empty':empty})
print(json.dumps(rows,ensure_ascii=False,indent=2))
raise SystemExit(0 if rows and all(not r['unclassified_english'] and not r['empty'] for r in rows) else 1)
