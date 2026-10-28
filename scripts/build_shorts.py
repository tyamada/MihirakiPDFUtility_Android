#!/usr/bin/env python3
"""Record real Android UI; compose portrait Shorts with complete macOS narration."""
import argparse, json, re, shutil, subprocess, time, xml.etree.ElementTree as ET
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
ROOT = Path(__file__).resolve().parents[1]
RAW = ROOT / 'shorts_raw'
OUT = ROOT / 'shorts_output'
PKG = 'com.takumayamada22.pdfutility'
SPECS = {'1': {'vo': 'Meet Mihiraki PDF Utility—the dedicated editor designed specifically for scanned books and manga. Preview your layout in authentic two-page spreads while you edit. Getting started is completely effortless.', 'texts': [[0.0, 5.0, 'Mihiraki PDF Utility: Manga PDF Editor'], [5.0, 15.0, 'Authentic Two-Page Spread Preview']]}, '2': {'vo': 'Import your scanned files in a snap. The streamlined interface keeps powerful tools right within reach. Access essential edit actions instantly, making adjustments quick and painless even on mobile.', 'texts': [[0.0, 6.0, 'Instant File Import'], [6.0, 15.0, 'Fast & Intuitive Menu Controls']]}, '3': {'vo': 'Put your pages in order. Rotate a page to change its orientation. Insert a blank page to shift the left and right spread positions, then preview the new layout. Remove unwanted pages to keep your PDF tidy.', 'texts': [[0, 5, '1. Rotate Page Orientation'], [5, 14, '2. Insert Blank & Align Spreads'], [14, 20, '3. Delete Unwanted Pages']]}, '4': {'vo': 'Switch between a comprehensive grid view to manage whole chapters and spread preview to inspect every detail. You can even customize your toolbar to keep your most-used editing features exactly where you want them.', 'texts': [[0.0, 6.5, 'Grid View vs Spread Preview'], [6.5, 15.0, 'Fully Customizable Toolbar']]}, '5': {'vo': 'Export your newly organized PDF instantly with zero quality loss. Best of all, all processing happens entirely on your device—no cloud uploads, no account required, complete privacy. Download Mihiraki PDF Utility on Google Play today!', 'texts': [[0.0, 6.5, 'Instant Lossless Export'], [6.5, 12.0, '100% Offline & Private'], [12.0, 17.0, 'Get It on Google Play!']]}}
DEVICE = None

def run(args, **kw):
    return subprocess.run(list(map(str,args)), check=True, **kw)
def adb(*args):
    return subprocess.check_output(['adb','-s',DEVICE,*map(str,args)], text=True, timeout=40)
def shell(*args): return adb('shell',*args)
def tap(x,y): shell('input','tap',x,y)
def nodes():
    shell('uiautomator','dump','/sdcard/shorts_ui.xml')
    return list(ET.fromstring(shell('cat','/sdcard/shorts_ui.xml')).iter('node'))
def find(label, tree):
    for n in tree:
        a=n.attrib
        if (label in (a.get('text'),a.get('content-desc')) or a.get('content-desc') == label + ', selected') and a.get('enabled')=='true':
            x,y,r,b=map(int,re.findall(r'\d+',a['bounds']))
            if r>x and b>y:return ((x+r)//2,(y+b)//2)
def click(label, scroll=False):
    for attempt in range(7 if scroll else 1):
        p=find(label,nodes())
        if p:
            tap(*p); return
        shell('input','swipe',850,2150,850,850,300)
    raise RuntimeError(f'UI element missing: {label}')
def menu(label):
    click('Settings'); click(label,scroll=True); time.sleep(.4)
def snapshot(name):
    (RAW/f'{name}.xml').write_text(ET.tostring(ET.fromstring('<hierarchy/>'),encoding='unicode'))
    tree=nodes()
    root=ET.Element('hierarchy')
    for n in tree: ET.SubElement(root,'node',n.attrib)
    ET.ElementTree(root).write(RAW/f'{name}.xml')
    with (RAW/f'{name}.png').open('wb') as f:run(['adb','-s',DEVICE,'exec-out','screencap','-p'],stdout=f)
    return tree

def open_pdf():
    click('Open'); tree=nodes()
    if not find('shorts_sample.pdf',tree):
        click('Show roots'); click('Downloads')
    click('shorts_sample.pdf'); time.sleep(1)
    tree=nodes()
    if not find('Page 8',tree): raise RuntimeError('Sample PDF did not load with 8 pages')

def prepare():
    # Restart only the app process. Never clear its data or alter the source PDF.
    shell('am','force-stop',PKG)
    shell('am','start','-n',PKG+'/.MainActivity'); time.sleep(1)
    tree=nodes()
    if find('Skip',tree):click('Skip')
    open_pdf()
    tree=nodes(); p=find('Page 2',tree)
    if p and p[1]>1000: menu('Toggle View')

def actions(scene, mark):
    """All labels correspond to Compose content descriptions / resource text.
    Coordinates used by input tap are obtained from the current UIAutomator bounds.
    """
    if scene==1:
        mark(0);time.sleep(2.5);menu('Toggle View');mark(1)
        tree=snapshot('scene_01_spread')
        assert find('Page 2',tree)[1] == find('Page 3',tree)[1], 'Spread pair not aligned'
        time.sleep(8)
    elif scene==2:
        mark(0);open_pdf();time.sleep(2);mark(1);click('Page 1');click('Settings')
        snapshot('scene_02_menu');time.sleep(3);shell('input','keyevent',4)
    elif scene==3:
        mark(0);click('Page 1');click('Rotate Right')
        snapshot('scene_03_rotated');time.sleep(2)
        mark(1);menu('Insert Blank');time.sleep(1)
        tree=snapshot('scene_03_blank')
        if not find('Page 9',tree): raise RuntimeError('Blank insertion did not produce 9 pages')
        click('Page 1')  # Clear the selected original page before previewing.
        menu('Toggle View')
        tree=snapshot('scene_03_blank_spread')
        assert find('Page 2',tree)[1] == find('Page 3',tree)[1], 'Spread pair not aligned'
        time.sleep(4)
        mark(2);menu('Toggle View');click('Page 2');click('Delete');time.sleep(1)
        click('Delete');time.sleep(1);tree=snapshot('scene_03_deleted')
        if find('Page 9',tree) or not find('Page 8',tree):raise RuntimeError('Delete did not leave exactly 8 pages')
        time.sleep(3)
    elif scene==4:
        mark(0);menu('Toggle View');time.sleep(1);menu('Toggle View')
        mark(1);menu('Options')
        click('Icon Only (Default)',scroll=True)
        # Options dialog contains a scrollable visibility checklist below display style.
        shell('input','swipe',550,1900,550,900,400)
        tree=snapshot('scene_04_customization')
        boxes=[n for n in tree if n.attrib.get('checkable')=='true' and n.attrib.get('class')=='android.widget.CheckBox']
        if not boxes:raise RuntimeError('Toolbar visibility checkboxes not found')
        a=boxes[0].attrib;x,y,r,b=map(int,re.findall(r'\d+',a['bounds']));tap((x+r)//2,(y+b)//2)
        time.sleep(2)
        # Cancel preserves the existing toolbar preferences after demonstrating changes.
        click('Cancel');time.sleep(2)
    elif scene==5:
        mark(0);click('Save');time.sleep(1);mark(1)
        tree=nodes()
        fields=[n for n in tree if n.attrib.get('class')=='android.widget.EditText']
        if not fields:raise RuntimeError('Android save dialog missing')
        a=fields[0].attrib
        x,y,r,b=map(int,re.findall(r'\d+',a['bounds']));tap((x+r)//2,(y+b)//2)
        shell('input','keycombination',113,29)
        shell('input','text',f'shorts_export_{int(time.time())}.pdf')
        click('SAVE');time.sleep(3)
        tree=snapshot('scene_05_saved')
        if any(n.attrib.get('package')=='com.google.android.documentsui' for n in tree):raise RuntimeError('Save dialog still open')
        # Confirm actual saved PDF bytes instead of treating a dismissed dialog as success.
        listing=shell('ls','-t','/sdcard/Download')
        name=next((s for s in listing.splitlines() if s.startswith('shorts_export_')),None)
        if not name:raise RuntimeError('Exported PDF not found in Downloads')
        for _ in range(30):
            if int(shell('stat','-c','%s','/sdcard/Download/'+name).strip())>100:break
            time.sleep(1)
        run(['adb','-s',DEVICE,'pull','/sdcard/Download/'+name,RAW/f'scene_05_export.pdf'],stdout=subprocess.DEVNULL)
        if (RAW/'scene_05_export.pdf').read_bytes()[:5]!=b'%PDF-':raise RuntimeError('Invalid exported PDF')
        mark(2);time.sleep(5)

def probe(path):
    return json.loads(subprocess.check_output(['ffprobe','-v','error','-show_streams','-show_format','-of','json',str(path)],text=True))
def compose(scene, raw, duration, marks):
    spec=SPECS[str(scene)]
    voice=RAW/f'scene_{scene:02d}_voice.aiff'
    run(['say','-v','Samantha','-r','175','-o',voice,spec['vo']])
    voice_duration=float(probe(voice)['format']['duration'])
    source_duration=float(probe(raw)['format']['duration'])
    source_marks=dict(marks)
    duration=max(voice_duration+.5,[15,15,20,15,17][scene-1])
    marks={i:entry[0] for i,entry in enumerate(spec['texts'])}
    font=ImageFont.truetype('/System/Library/Fonts/Supplemental/Arial Bold.ttf',44)
    inputs=['ffmpeg','-y','-hide_banner','-loglevel','error','-i',raw,'-i',voice]
    count=len(spec['texts'])
    filters=['[0:v]split='+str(count)+''.join(f'[src{i}]' for i in range(count))]
    for i in range(count):
        begin=min(source_marks[i],source_duration-.1)
        end=max(begin+.05,min(source_marks.get(i+1,source_duration),source_duration))
        length=marks.get(i+1,duration)-marks[i]
        ratio=length/(end-begin)
        filters.append(f'[src{i}]trim=start={begin}:end={end},setpts=(PTS-STARTPTS)*{ratio},fps=30,tpad=stop_mode=clone:stop_duration={length},trim=duration={length},setpts=PTS-STARTPTS[seg{i}]')
    filters.append(''.join(f'[seg{i}]' for i in range(count))+f'concat=n={count}:v=1:a=0,scale=856:1920:force_original_aspect_ratio=decrease,pad=1080:1920:(ow-iw)/2:0:color=0x101827[v0]')
    # Keep captions in the bottom band, outside the captured phone display.
    for i,(_,_,caption) in enumerate(spec['texts']):
        im=Image.new('RGBA',(1080,190),(16,24,39,255));d=ImageDraw.Draw(im)
        words=caption.split();lines=[];line=''
        for w in words:
            candidate=(line+' '+w).strip()
            if d.textlength(candidate,font=font)>1000:lines.append(line);line=w
            else:line=candidate
        lines.append(line)
        for j,line in enumerate(lines):d.text((540,52+j*56),line,anchor='mm',font=font,fill='white')
        png=RAW/f'scene_{scene:02d}_caption_{i}.png';im.save(png)
        inputs+=['-loop','1','-i',png]
        start=marks.get(i,0);end=marks.get(i+1,duration)
        filters.append(f'[v{i}][{i+2}:v]overlay=0:1730:enable=\'gte(t,{start})*lt(t,{end})\'[v{i+1}]')
    final=OUT/f'scene_{scene:02d}_final.mp4'
    cmd=inputs+['-filter_complex',';'.join(filters),'-map',f'[v{len(spec["texts"])}]','-map','1:a','-af','apad','-t',str(duration),'-c:v','libx264','-preset','fast','-crf','20','-pix_fmt','yuv420p','-c:a','aac','-ar','44100','-ac','2','-b:a','192k','-movflags','+faststart',final]
    run(cmd)
    info=probe(final);video=next(s for s in info['streams'] if s['codec_type']=='video');audio=next(s for s in info['streams'] if s['codec_type']=='audio')
    assert (video['width'],video['height'])==(1080,1920) and audio['codec_name']=='aac'
    assert abs(float(video['duration'])-duration)<.1, 'Incomplete video timeline'
    report={'scene':scene,'file':str(final),'duration':float(info['format']['duration']),'narration_duration':voice_duration,'source_duration':source_duration,'source_caption_starts':source_marks,'caption_starts':marks,'bytes':final.stat().st_size,'video':video['codec_name'],'audio':audio['codec_name']}
    (OUT/f'scene_{scene:02d}_verification.json').write_text(json.dumps(report,indent=2))
    print(json.dumps(report),flush=True)

def main():
    global DEVICE
    p=argparse.ArgumentParser();p.add_argument('--scene',choices=['all','1','2','3','4','5'],default='all');a=p.parse_args()
    for tool in ('adb','ffmpeg','ffprobe','say'):
        if not shutil.which(tool):raise RuntimeError(f'Missing tool: {tool}')
    devices=[l.split()[0] for l in subprocess.check_output(['adb','devices'],text=True).splitlines()[1:] if '\tdevice' in l]
    if len(devices)!=1:raise RuntimeError('Exactly one online device is required')
    DEVICE=devices[0];RAW.mkdir(exist_ok=True);OUT.mkdir(exist_ok=True)
    run(['adb','-s',DEVICE,'push',ROOT/'docs/sample/tameshibu_episode1_2_en.pdf','/sdcard/Download/shorts_sample.pdf'],stdout=subprocess.DEVNULL)
    old=shell('settings','get','system','show_touches').strip()
    shell('settings','put','system','show_touches','1')
    try:
        for scene in (range(1,6) if a.scene=='all' else [int(a.scene)]):
            prepare()
            final=OUT/f'scene_{scene:02d}_final.mp4'
            if final.exists():
                backup=RAW/'previous_outputs';backup.mkdir(exist_ok=True)
                shutil.copy2(final,backup/f'{final.stem}_{time.time_ns()}.mp4')
            raw=RAW/f'scene_{scene:02d}_raw.mp4';remote=f'/sdcard/shorts_scene_{scene}.mp4'
            rec=subprocess.Popen(['adb','-s',DEVICE,'shell','screenrecord','--bit-rate','10000000','--time-limit','150',remote])
            time.sleep(.7);start=time.monotonic();marks={}
            def mark(i):marks[i]=round(time.monotonic()-start,3)
            try:actions(scene,mark)
            finally:
                shell('pkill','-INT','screenrecord');rec.wait(timeout=15)
            duration=time.monotonic()-start
            run(['adb','-s',DEVICE,'pull',remote,raw],stdout=subprocess.DEVNULL)
            shell('rm',remote)
            compose(scene,raw,duration,marks)
    finally:shell('settings','put','system','show_touches',old)
if __name__=='__main__':main()
