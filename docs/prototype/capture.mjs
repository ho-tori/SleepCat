import fs from 'node:fs/promises';
import {pathToFileURL} from 'node:url';
import path from 'node:path';
const root=path.resolve(import.meta.dirname,'../..');
const tabs=await (await fetch('http://127.0.0.1:9337/json')).json();
const ws=new WebSocket(tabs.find(t=>t.type==='page').webSocketDebuggerUrl);
await new Promise(r=>ws.addEventListener('open',r,{once:true}));
let serial=0;const pending=new Map();
ws.addEventListener('message',e=>{const m=JSON.parse(e.data);if(pending.has(m.id)){const [ok,fail]=pending.get(m.id);pending.delete(m.id);m.error?fail(m.error):ok(m.result)}});
const call=(method,params={})=>new Promise((ok,fail)=>{const id=++serial;pending.set(id,[ok,fail]);ws.send(JSON.stringify({id,method,params}))});
const run=async expression=>{const r=await call('Runtime.evaluate',{expression,awaitPromise:true,returnByValue:true});if(r.exceptionDetails)throw Error(JSON.stringify(r.exceptionDetails));return r.result.value};
await call('Page.enable');
await call('Emulation.setDeviceMetricsOverride',{width:393,height:852,deviceScaleFactor:1,mobile:true});
await call('Page.navigate',{url:pathToFileURL(path.join(root,'docs/prototype/reference.html')).href+'?embedded=1'});
for(let i=0;i<50;i++){if(await run('typeof go === "function"'))break;await new Promise(r=>setTimeout(r,100));}
const manifest=[];
async function capture(name,expression){
 await run(expression);
 await run('document.fonts.ready.then(()=>Promise.all([...document.images].map(i=>i.decode().catch(()=>{})))).then(()=>new Promise(r=>setTimeout(r,800)))');
 const geometry=await run(`({page:s.page,phone:(()=>{const r=document.querySelector('#phone').getBoundingClientRect();return {width:r.width,height:r.height}})(),scrollHeight:document.querySelector('#screen').scrollHeight,elements:[...document.querySelectorAll('h1,h2,.paw-card,.home-clock-card,.nav,.welcome-stage')].map(e=>{const r=e.getBoundingClientRect(),c=getComputedStyle(e);return {tag:e.className,text:e.innerText,x:r.x,y:r.y,width:r.width,height:r.height,color:c.color,background:c.backgroundColor,font:c.font,lineHeight:c.lineHeight}})})`);
 const shot=await call('Page.captureScreenshot',{format:'png'});
 await fs.writeFile(path.join(root,'docs/prototype/screenshots',name+'.png'),Buffer.from(shot.data,'base64'));
 manifest.push({name,...geometry});
}
for(const page of ['welcome','goal','tasks','mode','permissions','home','tasksEdit','routine','review','morning','reminderSettings','settings','schedules'])
 await capture(page,`s=fresh();s.setup=${!['welcome','goal','tasks','mode','permissions'].includes(page)};go('${page}')`);
await capture('scheduleEditor',`s=fresh();go('goal');openSchedule(null)`);
await capture('outside-light',`s=fresh();s.setup=true;go('outside')`);
await capture('outside-strong',`enterPreview('strong')`);
await capture('home-expanded',`go('home');document.querySelector('[data-paw-action="expand"]').click()`);
await capture('home-edit',`document.querySelector('[data-paw-action="edit"]').click()`);
await capture('home-ring',`document.querySelector('[data-paw-action="cancel-edit"]').click();window.SleepCatQuickPlan.open()`);
await fs.writeFile(path.join(root,'docs/prototype/screenshots/manifest.json'),JSON.stringify(manifest,null,2));
console.log('Captured '+manifest.length+' reference states at 393 × 852 CSS px.');
await call('Browser.close');ws.close();
