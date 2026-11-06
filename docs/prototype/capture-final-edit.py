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
start('home');tap('展开清单');shot('home-expanded');tap('调整今晚清单');shot('home-edit')
