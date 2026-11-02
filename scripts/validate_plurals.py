#!/usr/bin/env python3
"""Check independent plural files and indexed printf argument parity."""
from pathlib import Path
import json,re,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
def arguments(text):
    return sorted((int(n),kind) for n,kind in re.findall(r'%(\d+)\$([a-zA-Z])',text))
def load(folder):
    result={}
    for path in folder.glob('*.xml'):
        for node in ET.parse(path).getroot().findall('plurals'):
            name=node.attrib['name']
            if name in result:raise ValueError(f'{folder.name}: duplicate plural {name}')
            items={}
            for item in node.findall('item'):
                q=item.attrib['quantity'];text=''.join(item.itertext())
                if q in items or not text.strip():raise ValueError(f'{folder.name}/{name}: duplicate or empty {q}')
                items[q]=text
            if 'other' not in items:raise ValueError(f'{folder.name}/{name}: missing other')
            result[name]=items
    return result
base=load(ROOT/'app/src/main/res/values');errors=[]
for folder in sorted((ROOT/'app/src/main/res').glob('values-*')):
    if not (folder/'strings.xml').exists():continue
    actual=load(folder)
    if actual.keys()!=base.keys():errors.append(f'{folder.name}: plural keys differ')
    for name,items in actual.items():
        if name not in base:continue
        expected=arguments(base[name]['other'])
        for q,text in items.items():
            if arguments(text)!=expected:errors.append(f'{folder.name}/{name}/{q}: format arguments differ')
print(json.dumps({'plural_keys':len(base),'errors':errors},ensure_ascii=False,indent=2))
raise SystemExit(bool(errors))
