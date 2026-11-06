import pathlib, subprocess, time, json
ROOT=pathlib.Path(__file__).resolve().parent
ADB=r'E:/Term3.1/SleepCat/.tools/android-sdk/platform-tools/adb.exe'
OUT=ROOT/'android-screenshots';OUT.mkdir(exist_ok=True)
def adb(*args):
    return subprocess.check_output([ADB]+list(args))
routes=['welcome','goal','tasks','mode','permissions','home','tasksEdit','routine','review','morning','reminderSettings','settings','schedules','scheduleEditor','outside']
for route in routes:
    adb('shell','am','force-stop','com.sleepcat.app')
    adb('shell','am','start','-n','com.sleepcat.app/.MainActivity','--es','prototype_page',route)
    time.sleep(2)
    adb('shell','uiautomator','dump','/sdcard/fidelity.xml')
    (OUT/(route+'.xml')).write_bytes(adb('exec-out','cat','/sdcard/fidelity.xml'))
    time.sleep(.5)
    (OUT/(route+'.png')).write_bytes(adb('exec-out','screencap','-p'))
    print(route,flush=True)
(OUT/'device.txt').write_bytes(adb('shell','wm','size')+adb('shell','wm','density')+adb('shell','getprop','ro.build.version.release'))
