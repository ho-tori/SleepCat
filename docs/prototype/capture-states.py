import pathlib, subprocess, time, re, xml.etree.ElementTree as ET, json
ROOT=pathlib.Path(__file__).resolve().parent
ADB=r'E:/Term3.1/SleepCat/.tools/android-sdk/platform-tools/adb.exe'
OUT=ROOT/'android-screenshots'
checks=[]
def adb(*args):return subprocess.check_output([ADB]+list(args))
def dump():
 adb('shell','uiautomator','dump','/sdcard/fidelity.xml')
 return ET.fromstring(adb('exec-out','cat','/sdcard/fidelity.xml'))
def find(value):
 for n in dump().iter('node'):
  if n.get('text')==value or value in n.get('content-desc',''):return n
 raise AssertionError('Missing '+value)
def tap(value):
 n=find(value);x1,y1,x2,y2=map(int,re.findall(r'\d+',n.get('bounds')))
 adb('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2));time.sleep(.8)
def start(route):
 adb('shell','am','force-stop','com.sleepcat.app');adb('shell','am','start','-n','com.sleepcat.app/.MainActivity','--es','prototype_page',route);time.sleep(2);dump()
def shot(name):
 dump();time.sleep(.4);(OUT/(name+'.png')).write_bytes(adb('exec-out','screencap','-p'));print(name,flush=True)
start('home');tap('展开清单');shot('home-expanded')
tap('调整今晚清单');shot('home-edit');tap('取消清单修改');tap('收起清单')
tap('调整就寝与起床时间');shot('home-ring')
n=find('24小时双端睡眠环');before=n.get('content-desc');x1,y1,x2,y2=map(int,re.findall(r'\d+',n.get('bounds')))
x=(x1+x2)//2;y=(y1+y2)//2
adb('shell','input','swipe',str(x),str(y1+23),str(x2-23),str(y),'900');time.sleep(1)
after=find('24小时双端睡眠环').get('content-desc');assert before!=after,(before,after)
checks.append({'check':'sleep ring start-handle drag','before':before,'after':after})
tap('确认并选择应用范围');shot('home-scope');tap('仅更改今晚的目标')
start('home');shot('home-saved-plan');checks.append({'check':'quick plan saved then process restarted','result':'home-saved-plan.png'})
start('reminderSettings');nodes=[n for n in dump().iter('node') if n.get('text')=='查看效果 · ›'];n=nodes[1];x1,y1,x2,y2=map(int,re.findall(r'\d+',n.get('bounds')));adb('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2));time.sleep(1);shot('outside-strong');tap('退出预览')
start('outside');shot('outside-light');tap('延后 10 分');find('模拟下一次提醒');tap('模拟下一次提醒');tap('延后 10 分');tap('模拟下一次提醒');find('现在开始准备');checks.append({'check':'two postponements escalate mixed reminder','result':'passed'})
tap('今晚不再提醒');shot('outside-confirm');tap('今晚跳过');checks.append({'check':'skip tonight confirmation','result':'passed'})
start('routine');tap('我准备放下手机了');shot('routine-confirm');tap('现在放下手机');time.sleep(4);find('晚安');checks.append({'check':'routine incomplete confirmation then countdown home','result':'passed'})
(OUT/'interaction-checks.json').write_text(json.dumps(checks,ensure_ascii=False,indent=2),encoding='utf-8')
