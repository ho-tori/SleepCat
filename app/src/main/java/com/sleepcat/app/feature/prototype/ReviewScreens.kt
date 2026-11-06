package com.sleepcat.app.feature.prototype

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sleepcat.app.core.designsystem.prototype.*
import com.sleepcat.app.data.prototype.*
import kotlinx.coroutines.delay
import kotlin.math.*

@Composable fun ReviewScreen(vm:PrototypeViewModel,go:(String)->Unit,edit:(Int)->Unit) {
    val s=vm.state;var detail by remember { mutableStateOf<Int?>(null) }
    Head {go("home")};Title("回顾")
    Card {Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {Txt("昨晚",14,weight=700);Box(Modifier.background(Color(0xFFFAE7D6),RoundedCornerShape(9.dp)).padding(9.dp,4.dp)) {Txt("晨间反馈",11,Color(0xFFA96741))}};Gap(9);Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {Sub("大概放下手机的时间");Txt(clock(s.records.last().time),25,weight=800)};Gap(15);Btn("填写 / 修改反馈",Modifier.fillMaxWidth(),"secondary") {edit(s.records.lastIndex)} }
    val good=s.records.count {delayMinutes(it)<=0};val late=s.records.filter {delayMinutes(it)>0};val average=if(late.isEmpty())0 else late.map(::delayMinutes).average().roundToInt()
    Card(color=Color(0xFFFFFAF4)) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {Column {Txt("这一周",17,weight=700);Txt("放下手机目标 · ${clock(s.target)}",11,Color(0xFF947D70))};Column(horizontalAlignment=Alignment.End) {Row(verticalAlignment=Alignment.Bottom) {Txt("$good/7",25,weight=800);Txt(" 天",11,P.sub)};Txt("按时放下手机",10,Color(0xFF8E786A))} }
        Gap(12);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically) {Box(Modifier.size(9.dp).background(Color(0xFF80AA91),CircleShape));Txt(" 提前 / 按时    ",11,P.sub);Box(Modifier.size(9.dp).background(Color(0xFFD69B72),CircleShape));Txt(" 延后",11,P.sub)};Gap(11)
        Box(Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(17.dp))) {
            Column {Box(Modifier.fillMaxWidth().height(95.dp).background(Color(0xFFF1F5EF)));Box(Modifier.fillMaxWidth().height(95.dp).background(Color(0xFFFAF0E7)))}
            Txt("提前",11,Color(0xFF80A18B),750,Modifier.padding(12.dp,8.dp));Txt("延后",11,Color(0xFFC08A65),750,Modifier.align(Alignment.BottomStart).padding(12.dp,8.dp));Box(Modifier.align(Alignment.Center).fillMaxWidth().height(2.dp).background(Color(0xFFAF9F93)))
            val scale=maxOf(30,s.records.maxOf {abs(delayMinutes(it))})
            Row(Modifier.fillMaxSize(),horizontalArrangement=Arrangement.spacedBy(4.dp)) {s.records.forEachIndexed {i,r->val d=delayMinutes(r);val height=abs(d)/scale.toFloat()*190*.35f
                Box(Modifier.weight(1f).fillMaxHeight().clickable {detail=i}.semantics {contentDescription="查看周${r.day}记录"}) {if(d==0)Box(Modifier.align(Alignment.Center).size(12.dp).background(Color(0xFF80AA91),CircleShape)) else Box(Modifier.align(Alignment.TopCenter).offset(y=(if(d<0)95-height else 95f).dp).width(21.dp).height(height.dp).background(if(d<0)Color(0xFF80AA91) else Color(0xFFD69B72),RoundedCornerShape(7.dp)));Txt(if(d==0)"按时" else "${abs(d)}",11,if(d<=0)Color(0xFF648C72) else Color(0xFFB97A52),800,Modifier.align(Alignment.TopCenter).offset(y=(if(d<=0)95-height-20 else 95+height+3).dp))}
            } }
        }
        Gap(11);Row(horizontalArrangement=Arrangement.spacedBy(4.dp)) {s.records.forEachIndexed {i,r->Column(Modifier.weight(1f).clickable {detail=i},horizontalAlignment=Alignment.CenterHorizontally) {val early=delayMinutes(r)<=0;Box(Modifier.size(22.dp).background(if(early)Color(0xFFE4F0E6) else Color(0xFFF9E8D9),CircleShape),contentAlignment=Alignment.Center) {Txt(if(early)"✓" else "!",13,if(early)Color(0xFF5B8B6A) else Color(0xFFB67A53),850)};Gap(3);Txt("周${r.day}",11,Color(0xFF6F5C52),760);Txt(clock(r.time),10,Color(0xFF968174))} } };Gap(13);Box(Modifier.fillMaxWidth().height(1.dp).background(P.line));Gap(12);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {Txt("延迟日平均",12,P.sub);Txt("$average 分钟",15,weight=800)}
    }
    Card {Txt("拖延原因",14,weight=700);val reasons=late.flatMap {it.reasons}.groupingBy {it}.eachCount().entries.sortedByDescending {it.value}.take(3);if(reasons.isEmpty())Sub("暂时没有记录") else reasons.forEach {r->Row(Modifier.fillMaxWidth().padding(vertical=9.dp),horizontalArrangement=Arrangement.SpaceBetween) {Txt(r.key);Txt("${r.value} 天",weight=700)};Box(Modifier.fillMaxWidth().height(1.dp).background(P.line))} }
    if(s.suggestionWeek!=weekKey())Card(color=Color(0xFFFFF1E7),radius=22) {Txt("给下周的小建议",14,weight=700);Gap(10);Txt("把开始准备的提醒从 ${clock(s.prep)} 提前 15 分钟试试。",13);Gap(13);Btn("一键采用建议",Modifier.fillMaxWidth()) {vm.update {it.copy(prep=mod(it.prep-15),suggestionWeek=weekKey())}};Gap(9);Btn("自己调整",Modifier.fillMaxWidth(),"secondary") {vm.update {it.copy(suggestionWeek=weekKey())};go("tasksEdit")};Btn("稍后再说",Modifier.fillMaxWidth(),"link") {vm.update {it.copy(suggestionWeek=weekKey())}} }
    Gap(8);Txt("本地演示数据 · 非真实睡眠记录",11,P.sub)
    detail?.let {i->val r=s.records[i];val d=delayMinutes(r);Popup("周${r.day}记录",{detail=null}) {listOf("放下手机" to clock(r.time),"相对目标" to if(d<0)"提前 ${-d} 分钟" else if(d>0)"延后 $d 分钟" else "按时","原因" to r.reasons.joinToString("、").ifEmpty {"未填写"}).forEach { (a,b)->Row(Modifier.fillMaxWidth().padding(vertical=10.dp),horizontalArrangement=Arrangement.SpaceBetween) {Txt(a,14,P.sub);Txt(b,14,weight=700)} };Actions("返回","修改记录",{detail=null},{detail=null;edit(i)})} }
}
@Composable fun MorningScreen(vm:PrototypeViewModel,index:Int,back:()->Unit) {
    val record=vm.state.records[index.coerceIn(vm.state.records.indices)];var time by rememberSaveable(index) {mutableIntStateOf(record.time)};var reasons by rememberSaveable(index) {mutableStateOf(record.reasons)};var wheel by remember {mutableStateOf(false)}
    Head(back);Title(if(index==vm.state.records.lastIndex)"昨晚几点放下手机？" else "修改周${record.day}记录")
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(19.dp)).background(Color(0xFFFFF0E4)).padding(horizontal=18.dp,vertical=6.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {Txt("早安！",15,weight=750);Cat(Modifier.size(95.dp,115.dp))}
    Card {Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(Color.White).border(1.dp,P.line,RoundedCornerShape(15.dp)).clickable {wheel=true}.padding(14.dp),horizontalArrangement=Arrangement.SpaceBetween) {Txt(clock(time),19,weight=800);Glyph("chev")} }
    Card {Txt("如果晚了，主要在做什么？",14,weight=700);Gap(12);listOf(listOf("刷视频","聊天","打游戏","学习 / 工作"),listOf("不困","其他")).forEach {row->Row(horizontalArrangement=Arrangement.spacedBy(7.dp)) {row.forEach {r->Box(Modifier.clip(RoundedCornerShape(14.dp)).background(if(r in reasons)P.orange else Color.White).border(1.dp,P.line,RoundedCornerShape(14.dp)).clickable {reasons=if(r in reasons)reasons-r else reasons+r}.padding(11.dp,7.dp)) {Txt(r,12,if(r in reasons)Color.White else P.ink,750)} } };Gap(7)} }
    Btn("完成",Modifier.fillMaxWidth()) {vm.update {s->s.copy(records=s.records.mapIndexed {i,r->if(i==index)r.copy(time=time,reasons=reasons) else r})};back()};Btn("稍后再说",Modifier.fillMaxWidth(),"link",onClick=back)
    if(wheel)TimePopup("昨晚放下手机的时间",time,{wheel=false}) {time=it;wheel=false}
}
@Composable fun SettingsScreen(vm:PrototypeViewModel,go:(String)->Unit) {
    var reset by remember {mutableStateOf(false)}
    Head {go("home")};Title("我的设置")
    Card {listOf("schedules" to "所有定时","tasksEdit" to "睡前小清单","reminderSettings" to "提醒方式与权限").forEachIndexed {i,(page,label)->Row(Modifier.fillMaxWidth().clickable {go(page)}.padding(vertical=12.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {Txt(label,14,weight=700);Glyph("chev")};if(i<2)Box(Modifier.fillMaxWidth().height(1.dp).background(P.line))} }
    Card {Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {Column(Modifier.weight(1f)) {Txt("晚安搭子",14,weight=700);Txt("今晚也一起慢慢收尾",11,P.sub)};Cat(Modifier.size(95.dp,115.dp))} }
    Btn("重置演示数据",Modifier.fillMaxWidth(),"link") {reset=true}
    if(reset)Popup("重置当前原型中的所有演示设置？",{reset=false}) {Actions(confirm="重置",onCancel={reset=false},onConfirm={vm.reset();reset=false;go("welcome")})}
}
@Composable fun RoutineScreen(vm:PrototypeViewModel,go:(String)->Unit) {
    var confirm by remember {mutableStateOf(false)};var goodnight by remember {mutableStateOf(false)};var remaining by remember {mutableIntStateOf(3)}
    fun finish() {vm.update {it.copy(closedNight=nightKey())};remaining=3;goodnight=true}
    LaunchedEffect(goodnight) {if(goodnight) {repeat(3) {delay(1000);remaining--};goodnight=false;go("home")} }
    Head {go("home")};Title("开始睡前准备");Sub("做完一项，勾掉就好。")
    Card {vm.state.tonight().forEachIndexed {i,t->if(i>0)Box(Modifier.fillMaxWidth().height(1.dp).background(P.line));Row(Modifier.fillMaxWidth().clickable {vm.done(t.id)}.padding(vertical=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(11.dp)) {Box(Modifier.size(24.dp).background(if(t.id in vm.state.night.done)P.orange else Color.White,RoundedCornerShape(8.dp)).border(1.dp,P.line,RoundedCornerShape(8.dp)),contentAlignment=Alignment.Center) {if(t.id in vm.state.night.done)Txt("✓",15,Color.White,900)};Txt(t.icon,24);Column {Txt(t.name,14,weight=700);Txt("第 ${i+1} 项 · 约 ${t.minutes} 分钟",11,P.sub)}}} }
    Btn("我准备放下手机了",Modifier.fillMaxWidth()) {if(vm.state.tonight().any {it.id !in vm.state.night.done})confirm=true else finish()}
    if(confirm)Popup("清单还有 ${vm.state.tonight().count {it.id !in vm.state.night.done}} 项未勾选",{confirm=false}) {Sub("可以继续完成，也可以现在放下手机。");Actions("继续完成","现在放下手机",{confirm=false},{confirm=false;finish()})}
    if(goodnight)Popup("晚安 ☾",{goodnight=false;go("home")}) {Cat(Modifier.fillMaxWidth().height(145.dp),true);Sub("现在可以锁屏休息了");Gap(16);Txt("$remaining 秒后返回首页",12,P.sub);Actions("撤销","返回首页",{goodnight=false;vm.update {it.copy(closedNight="")}},{goodnight=false;go("home")})}
}
