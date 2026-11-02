#!/usr/bin/env python3
"""Validate local assets, fragments and language destinations of public HTML."""
from html.parser import HTMLParser
import json
from pathlib import Path
from urllib.parse import unquote, urlsplit

class Page(HTMLParser):
    def __init__(self, text):
        super().__init__(); self.links=[]; self.ids=set(); self.errors=[]; self.lang=None
        self.feed(text)
    def handle_starttag(self, tag, attrs):
        a=dict(attrs)
        if tag=='html': self.lang=a.get('lang')
        if a.get('id'):
            if a['id'] in self.ids: self.errors.append('duplicate id: '+a['id'])
            self.ids.add(a['id'])
        for key in ('href','src'):
            if a.get(key): self.links.append(a[key])
        if tag=='option' and a.get('value'): self.links.append(a['value'])
        if tag=='img' and 'alt' not in a: self.errors.append('image without alt')
        if tag=='select' and not (a.get('aria-label') or a.get('class')=='language-picker'):
            self.errors.append('language selector needs a label')

def validate(root):
    docs=root/'docs'; pages={p:Page(p.read_text()) for p in docs.glob('*.html')}; errors=[]; links=0
    for path,page in pages.items():
        errors.extend(f'{path.name}: {e}' for e in page.errors)
        if not page.lang: errors.append(f'{path.name}: missing html lang')
        for link in page.links:
            u=urlsplit(link)
            if u.scheme or u.netloc: continue
            links+=1
            target=(path.parent/unquote(u.path)).resolve() if u.path else path.resolve()
            if not target.is_file(): errors.append(f'{path.name}: missing {link}')
            elif u.fragment and target.suffix=='.html':
                parsed=pages.get(target) or Page(target.read_text())
                if unquote(u.fragment) not in parsed.ids: errors.append(f'{path.name}: missing fragment {link}')
    return {'pages':len(pages),'local_references':links,'errors':errors,'external_links_checked':False}

if __name__=='__main__':
    result=validate(Path(__file__).resolve().parent.parent)
    print(json.dumps(result,ensure_ascii=False,indent=2))
    raise SystemExit(1 if result['errors'] else 0)
