package com.sleepcat.app.feature.prototype

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.sleepcat.app.core.designsystem.prototype.*
import com.sleepcat.app.data.prototype.*
import kotlinx.coroutines.delay
import java.time.LocalDateTime

@Composable fun Paw(modifier:Modifier=Modifier.size(34.dp),lit:Boolean=false) {
    val path=remember { PathParser().parsePathString("M8.5 22c0-3.2 2.1-4.2 4.1-6.3 1.7-1.8 5.1-1.8 6.8 0 2 2.1 4.1 3.1 4.1 6.3 0 3.5-2.5 5.2-5.2 4.2-1.5-.5-3.1-.5-4.6 0-2.7 1-5.2-.7-5.2-4.2Z").toPath() }
    Canvas(modifier) { val c=if(lit)Color(0xFFBB744D) else Color(0xFFDFC9B2);scale(size.width/32,size.height/32,pivot=Offset.Zero) { drawPath(path,c);listOf(floatArrayOf(6.8f,12f,3.1f,4.1f,-22f),floatArrayOf(12.7f,7.4f,3f,4f,-8f),floatArrayOf(19.3f,7.4f,3f,4f,8f),floatArrayOf(25.2f,12f,3.1f,4.1f,22f)).forEach { a->rotate(a[4],Offset(a[0],a[1])) { drawOval(c,Offset(a[0]-a[2],a[1]-a[3]),Size(a[2]*2,a[3]*2)) } } } }
}
@Composable private fun Trail(list:List<Task>,done:List<String>,mini:Boolean,click:(String)->Unit={}) {
    val next=list.indexOfFirst {it.id !in done}
    val start=if(list.size>5)(if(next<0)list.size-5 else next-1).coerceIn(0,list.size-5) else 0
    val shown=if(mini)list.drop(start).take(5) else list
    val all=shown.isNotEmpty()&&shown.all { it.id in done }
    val height=if(mini)190 else shown.size*98+85
    BoxWithConstraints(Modifier.then(if(mini)Modifier.width(100.dp) else Modifier.fillMaxWidth()).height(height.dp)) {
        val width=maxWidth
        Canvas(Modifier.fillMaxSize()) {
            val nodes=shown.indices.map { i->if(mini)Offset((if(i%2==0)35f else 78f)/118*size.width,(12f+i*(if(shown.size>1)68f/(shown.size-1) else 0f))/148*size.height) else Offset((if(i%2==0).15f else .85f)*size.width,(36+i*98).dp.toPx()) }
            nodes.forEachIndexed { i,p->val end=nodes.getOrNull(i+1)?:Offset(size.width*.5f,if(mini)111f/148*size.height else (shown.size*98+28).dp.toPx());val d=if(mini)(end.y-p.y)*.65f else 59.dp.toPx();val path=Path().apply { moveTo(p.x,p.y);cubicTo(p.x,p.y+d,end.x,end.y-d,end.x,end.y) };drawPath(path,if(shown[i].id in done)Color(0xFFBB744D) else Color(0xFFE1CAB3),style=Stroke(1.6.dp.toPx(),cap=StrokeCap.Round,pathEffect=PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(),6.dp.toPx())))) }
        }
        shown.forEachIndexed { index,t->key(t.id) {
            val doneTask=t.id in done
            if(mini) { val x=width*((if(index%2==0)35f else 78f)/118)-9.dp;val y=(12f+index*(if(shown.size>1)68f/(shown.size-1) else 0f))*height/148f-9f
                Paw(Modifier.offset(x,y.dp).size(18.dp).background(P.home,CircleShape),doneTask)
            } else {
                val y by animateDpAsState((index*98).dp,tween(680),label="completed task order")
                Row(Modifier.offset {IntOffset(0,y.roundToPx())}.fillMaxWidth().height(98.dp).clickable {click(t.id)}.semantics { contentDescription=if(doneTask)"取消完成${t.name}" else "完成${t.name}" },verticalAlignment=Alignment.Top) {
                    val right=index%2==1
                    if(!right)Box(Modifier.width(width*.29f).padding(top=14.dp),contentAlignment=Alignment.Center) { Paw(Modifier.size(45.dp).background(P.home,CircleShape).padding(5.dp),doneTask) }
                    Column(Modifier.weight(1f).padding(top=13.dp).background(P.home),horizontalAlignment=if(right)Alignment.End else Alignment.Start) { Txt(t.name,18,if(doneTask)Color(0xFF927056) else P.ink,500);Gap(4);Txt("${t.minutes} 分钟",12,Color(0xFFA38C77)) }
                    if(right)Box(Modifier.width(width*.29f).padding(top=14.dp),contentAlignment=Alignment.Center) { Paw(Modifier.size(45.dp).background(P.home,CircleShape).padding(5.dp),doneTask) }
                }
            }
        } }
        Box(Modifier.align(Alignment.BottomCenter).size(if(mini)72.dp else 98.dp,if(mini)65.dp else 85.dp).alpha(if(all)1f else .3f)) { Cat(Modifier.fillMaxSize(),true) }
    }
}
@Composable fun NativeHome(vm:PrototypeViewModel,go:(String)->Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) };var editing by rememberSaveable { mutableStateOf<Night?>(null) };var prep by rememberSaveable { mutableStateOf(false) };var plan by rememberSaveable { mutableStateOf<QuickPlan?>(null) };var scope by rememberSaveable { mutableStateOf(false) };var undo by remember { mutableStateOf<Task?>(null) }
    val scroll=rememberScrollState()
    LaunchedEffect(plan!=null) {if(plan!=null) {delay(80);scroll.animateScrollTo(scroll.maxValue)}}
    val s=vm.state;val list=s.tonight();val all=list.isNotEmpty()&&list.all { it.id in s.night.done };val current=list.find {it.id !in s.night.done}
    LaunchedEffect(undo) { if(undo!=null) {delay(4500);undo=null} }
    var activeNight by rememberSaveable { mutableStateOf(s.night.key) }
    LaunchedEffect(s.night.key) { if(activeNight!=s.night.key) { expanded=false;editing=null;undo=null;plan=null;scope=false;activeNight=s.night.key } }
    BackHandler(scope||plan!=null||editing!=null||expanded) { when {scope->scope=false;plan!=null->plan=null;editing!=null->editing=null;else->expanded=false} }
    BoxWithConstraints(Modifier.fillMaxSize().background(P.home)) {
        val viewportHeight=maxHeight.value;val available=maxOf(494f,viewportHeight-78-24);val listHeight=available*1.3f/2.3f;val clockHeight=available-listHeight
        Column(Modifier.fillMaxSize().verticalScroll(scroll).padding(top=12.dp,bottom=90.dp)) {
            if(s.closedNight==nightKey())Row(Modifier.fillMaxWidth().padding(20.dp),verticalAlignment=Alignment.CenterVertically) { Cat(Modifier.size(66.dp,72.dp),true);Column(Modifier.weight(1f)) {Txt("晚安",24,weight=800);Txt("今晚先安心休息",11,P.sub)};Btn("撤销",kind="link") {vm.update {it.copy(closedNight="")}} }
            else if(editing!=null)NightEditor(s,editing!!,{editing=it},{editing=null},{vm.update {if(it.night.key==editing!!.key) it.copy(night=editing!!) else it};editing=null},{editing=null;go("tasksEdit")})
            else Column(Modifier.fillMaxWidth().then(if(expanded)Modifier else Modifier.height(listHeight.dp))) {
                Row(Modifier.fillMaxWidth().padding(start=20.dp,end=20.dp,top=14.dp).height(44.dp),verticalAlignment=Alignment.CenterVertically) { Txt("GET READY FOR BED",9,Color(0xFFAC7652),800,Modifier.weight(1f));Box(Modifier.size(44.dp).clickable {expanded=!expanded}.semantics {contentDescription=if(expanded)"收起清单" else "展开清单"},contentAlignment=Alignment.Center) {Glyph("down",Modifier.size(18.dp).rotate(if(expanded)180f else 0f),Color(0xFF987055))};RoundAction("pen","调整今晚清单") {editing=s.night.copy(order=s.tonight(completedFirst=false).map {it.id})} }
                if(expanded)Box(Modifier.padding(horizontal=20.dp,vertical=17.dp)) { if(list.isEmpty())Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally) {Cat(Modifier.size(98.dp,85.dp),true);Sub("暂无事项")} else Trail(list,s.night.done,false) {vm.done(it)} }
                else Row(Modifier.weight(1f).fillMaxWidth().padding(start=20.dp,end=20.dp,top=9.dp,bottom=14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        Column(Modifier.clickable {prep=true}.semantics {contentDescription="修改开始睡前准备时间"}) {Txt("开始睡前准备",12,Color(0xFF7E6252));Txt(clock(s.prep),48,weight=850,line=1.08f)}
                        Gap((listHeight-326).toInt().coerceIn(18,78))
                        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {Box(Modifier.size(48.dp).clip(CircleShape).clickable(enabled=current!=null) {current?.let {vm.done(it.id,true);undo=it}}.semantics {contentDescription=current?.let {"完成${it.name}"}?:"全部完成"},contentAlignment=Alignment.Center) {Box(Modifier.size(45.dp).background(Color(0xFFFAECDC),CircleShape),contentAlignment=Alignment.Center) {if(all)Glyph("clock",color=Color(0xFFBB744D)) else Paw()} };Column {if(all) {Txt("最后完成",12,Color(0xFF9B846E));Txt(s.night.completedAt.values.maxOrNull()?.let {runCatching {LocalDateTime.parse(it).toLocalTime().toString().take(5)}.getOrNull()}?:"--:--",18,weight=500)} else {Txt(current?.name?:"暂无事项",18,weight=500);if(current!=null)Txt("${current.minutes} 分钟",12,Color(0xFF9B846E))}} }
                    }
                    Trail(list,s.night.done,true)
                }
                Box(Modifier.padding(start=20.dp,end=20.dp,bottom=20.dp)) { Box(Modifier.fillMaxWidth().height(48.dp).clip(CircleShape).background(if(all||list.isEmpty())Color(0xFFEEE6DC) else Color(0xFF48362D)).clickable(enabled=!all&&list.isNotEmpty()) {vm.completeAll()},contentAlignment=Alignment.Center) {Txt(if(all)"已全部完成" else "一键完成",14,if(all||list.isEmpty())Color(0xFFA18B76) else Color(0xFFFFF9F0),500)} }
            }
            if(plan!=null)Column(Modifier.fillMaxWidth().animateContentSize(tween(640)).padding(16.dp)) {
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {RoundAction("cross","取消修改") {plan=null};RoundAction("check","确认并选择应用范围") {if(plan!=s.effective())scope=true} };Gap(12);DialPanel(plan!!.start,plan!!.duration) {a,b->plan=plan!!.copy(start=a,duration=b)};Gap(8);if(plan!!.duration<s.duration)Txt("比睡眠目标少 ${durationText(s.duration-plan!!.duration)}",12,Color(0xFFB46B43))
            } else Box(Modifier.fillMaxWidth().height((if(editing!=null||expanded||s.closedNight==nightKey())204f else clockHeight).dp).clip(RectangleShape).clickable {plan=s.effective()}.semantics {contentDescription="调整就寝与起床时间"}) {
                Box(Modifier.align(Alignment.TopEnd).offset(x=4.dp,y=(-32).dp).size(110.dp).background(Color(0xB8FFF3E6),CircleShape))
                Txt("✦",14,Color(0xFFD9A56A),modifier=Modifier.align(Alignment.TopEnd).padding(top=24.dp,end=154.dp));Txt("✦",12,Color(0xFFD9A56A),modifier=Modifier.align(Alignment.TopEnd).padding(top=64.dp,end=118.dp));Txt("☾",16,Color(0xFFD9A56A),modifier=Modifier.align(Alignment.TopEnd).padding(top=18.dp,end=18.dp))
                Column(Modifier.padding(start=21.dp,top=30.dp)) {Txt("PUT THE PHONE DOWN",9,Color(0xFFAC7652),800);Gap(25);Txt("目标放下手机",12,Color(0xFF7E6252));Txt(clock(s.effective().start),48,weight=850,line=1.08f)}
                Cat(Modifier.align(Alignment.BottomEnd).offset(y=12.dp).size((viewportHeight*.24f).coerceIn(150f,200f).dp))
            }
        }
        undo?.let { t->Row(Modifier.align(Alignment.BottomCenter).padding(start=20.dp,end=20.dp,bottom=94.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFF48362D)).padding(start=16.dp),verticalAlignment=Alignment.CenterVertically) {Txt("${t.name}已完成",14,Color(0xFFFFF9F0),modifier=Modifier.weight(1f));Btn("撤销",kind="link") {vm.done(t.id,false);undo=null}} }
        if(prep)TimePopup("开始准备时间",s.prep,{prep=false}) {v->vm.update {it.copy(prep=v)};prep=false}
        if(scope)Popup("选择应用范围",{scope=false}) {val active=s.scheme();Sub(if(active==null)"今晚没有启用的方案。要只更改今晚，还是为今天新增定时？" else "你要将此更改应用到「${active.name}」的每一天（${daysLabel(active.days)}）吗？");Gap(14);Btn("仅更改今晚的目标",Modifier.fillMaxWidth(),"quiet") {vm.saveQuick(plan!!.start,plan!!.duration,false);scope=false;plan=null};Gap(8);Btn(if(active==null)"新增今天的定时" else "更改此方案",Modifier.fillMaxWidth(),"quiet") {vm.saveQuick(plan!!.start,plan!!.duration,true);scope=false;plan=null} }
    }
}
@Composable private fun RoundAction(icon:String,label:String,click:()->Unit) { Box(Modifier.size(44.dp).clip(CircleShape).then(if(icon in listOf("check","cross"))Modifier.background(if(icon=="check")P.orange else Color(0xFFF3EAE2),CircleShape) else Modifier.border(1.dp,Color(0xFFEADBCD),CircleShape)).clickable(onClick=click).semantics {contentDescription=label},contentAlignment=Alignment.Center) {Glyph(icon,Modifier.size(18.dp),if(icon=="check")Color.White else Color(0xFF987055))} }
@Composable private fun NightEditor(s:PrototypeState,draft:Night,change:(Night)->Unit,cancel:()->Unit,save:()->Unit,daily:()->Unit) {
    var adding by remember { mutableStateOf(false) };var name by remember { mutableStateOf("") };var minutes by remember { mutableStateOf("10") };var error by remember { mutableStateOf("") };var revealed by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxWidth().padding(start=20.dp,end=20.dp,top=14.dp,bottom=20.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {RoundAction("cross","取消清单修改",cancel);Txt("调整今晚清单",16,weight=500);RoundAction("check","保存今晚清单",save)};Gap(18)
        val items=s.tonight(draft,false)
        items.forEach {t->key(t.id) {
            Box(Modifier.fillMaxWidth().height(79.dp).clip(RectangleShape)) {
                if(revealed==t.id)Box(Modifier.align(Alignment.CenterEnd).width(64.dp).fillMaxHeight().background(Color(0xFFEFD5C5)).clickable {change(draft.copy(removed=draft.removed+t.id));revealed=null}.semantics {contentDescription="删除${t.name}"},contentAlignment=Alignment.Center) {Glyph("cross")}
                Row(Modifier.offset(x=if(revealed==t.id)(-64).dp else 0.dp).fillMaxSize().background(P.home).pointerInput(t.id) { var distance=0f;detectHorizontalDragGestures(onDragStart={distance=0f},onHorizontalDrag={c,d->c.consume();distance+=d},onDragEnd={revealed=if(distance< -28)t.id else null}) },verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(11.dp)) {Paw(Modifier.size(24.dp),true);Column(Modifier.weight(1f)) {Txt(t.name,18,weight=500);Txt("${t.minutes} 分钟",12,Color(0xFFA58B76))};DragHandle(t.id,"拖动调整${t.name}顺序",79f,icon="grip") { direction->val ids=items.map {it.id}.toMutableList();val index=ids.indexOf(t.id);val other=index+direction;if(other in ids.indices) {java.util.Collections.swap(ids,index,other);change(draft.copy(order=ids))}} }
                Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(1.dp).background(Color(0xFFEADBCB)))
            }
        } }
        if(items.isEmpty())Sub("暂无事项")
        if(adding)Card {Txt("事项",12,P.sub);Field(name,{name=it.take(18)},"输入事项名称");Gap(12);Txt("用时",12,P.sub);Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {Field(minutes,{minutes=it},modifier=Modifier.width(65.dp),numeric=true);Txt("分钟");Spacer(Modifier.weight(1f));Btn("添加",kind="quiet") {val m=minutes.toIntOrNull();if(name.isBlank()||m==null||m !in 1..120)error="填写事项名称和 1～120 分钟的用时。" else {val t=Task("tonight${System.nanoTime()}",name.trim(),"⭐",m);change(draft.copy(added=draft.added+t,order=items.map {it.id}+t.id));adding=false;name="";error=""}} };if(error.isNotEmpty())Sub(error)}
        else {Gap(17);Box(Modifier.fillMaxWidth().height(44.dp).dashedBorder(Color(0xFFD5BDA6),99).clickable {adding=true},contentAlignment=Alignment.Center) {Txt("＋ 添加事项",14,Color(0xFFAD7951),500)}}
        Gap(14);Box(Modifier.fillMaxWidth().height(44.dp).clickable(onClick=daily),contentAlignment=Alignment.Center) {Txt("编辑日常清单",12,Color(0xFF9F704D))}
    }
}
