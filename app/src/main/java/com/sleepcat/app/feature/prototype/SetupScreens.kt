package com.sleepcat.app.feature.prototype

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import com.sleepcat.app.core.designsystem.prototype.*
import com.sleepcat.app.data.prototype.*

@Composable fun WelcomeScreen(go:(String)->Unit,skip:()->Unit) {
    Brand(true)
    Column(Modifier.heightIn(min=678.dp)) {
        Gap(52);Txt("PUT THE PHONE DOWN",11,Color(0xFFAA765A),800);Gap(15)
        Box(Modifier.fillMaxWidth().height(107.dp)) {Txt("你的夜晚",44,weight=820,line=1.18f);Txt("不止这一屏",44,Color(0xFFC97848),820,Modifier.offset(y=52.dp),line=1.18f)}
        Gap(28)
        Box(Modifier.fillMaxWidth().height(305.dp).clip(RoundedCornerShape(topStart=145.dp,topEnd=145.dp,bottomStart=34.dp,bottomEnd=34.dp)).background(Brush.radialGradient(listOf(Color(0xFFFFF9F1),Color(0xFFFAEEE1),Color(0xFFF7E6D6)))),contentAlignment=Alignment.BottomCenter) {
            Box(Modifier.align(Alignment.TopEnd).padding(top=46.dp,end=39.dp).size(113.dp).background(Color(0xB3FFF9EF),CircleShape));Cat(Modifier.width(325.dp).height(276.dp).padding(bottom=5.dp),true)
        }
        Spacer(Modifier.weight(1f));Gap(19);Btn("设置我的睡前计划",Modifier.fillMaxWidth().heightIn(min=54.dp)) { go("goal") };Btn("直接体验",Modifier.fillMaxWidth(),"link",onClick=skip)
    }
}
@Composable fun ScheduleListScreen(vm:PrototypeViewModel,onboarding:Boolean,go:(String)->Unit,edit:(Plan?)->Unit) {
    Head { go(if(onboarding)"welcome" else "settings") };if(onboarding)Progress(1)
    Title(if(onboarding)"设置睡眠定时" else "所有定时")
    Sub(if(onboarding)"可以给工作日、周末或任意一天设置不同的就寝与起床时间。" else "每一天只会启用一个方案。你可以随时新增或修改。");Gap(if(onboarding)19 else 0)
    if(onboarding)Txt("所有定时",15,weight=800)
    if(vm.state.plans.isEmpty()) {
        Column(Modifier.fillMaxWidth().padding(vertical=18.dp).clip(RoundedCornerShape(26.dp)).background(Color(0xFFFFFCF8)).dashedBorder(Color(0xFFDFCDBE),26).padding(horizontal=22.dp,vertical=31.dp),horizontalAlignment=Alignment.CenterHorizontally) { Box(Modifier.size(58.dp).background(Color(0xFFFBE9DB),CircleShape),contentAlignment=Alignment.Center) { Txt("☾",28,Color(0xFFC97949)) };Gap(13);Txt("还没有定时方案",17,weight=700);Gap(7);Txt("按不同日子安排适合自己的睡眠时间。",12,P.sub,align=TextAlign.Center) }
    } else vm.state.plans.forEach { p->Card(radius=25) { Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) { Txt(p.name,17,weight=750,modifier=Modifier.weight(1f));Btn("编辑",kind="quiet") { edit(p) } };Txt(daysLabel(p.days),12,Color(0xFFA27053),700);Gap(14);Box(Modifier.fillMaxWidth().height(1.dp).background(P.line));Gap(13);Row { Column(Modifier.weight(1f)) { Txt("就寝 · 放下手机",11,P.sub);Txt(clock(p.start),25,weight=800) };Column(Modifier.weight(1f)) { Txt("起床",11,P.sub);Txt(clock(p.start+p.duration),25,weight=800) } } } }
    Btn("＋ 新增方案",Modifier.fillMaxWidth(),"schedule") { edit(null) }
    if(onboarding)Actions("返回","下一步",{go("welcome")},{go("tasks")})
}
@Composable fun ScheduleEditorScreen(vm:PrototypeViewModel,initial:Plan,back:()->Unit) {
    var name by rememberSaveable(initial.id) { mutableStateOf(initial.name) };var days by rememberSaveable(initial.id) { mutableStateOf(initial.days) };var start by rememberSaveable(initial.id) { mutableIntStateOf(initial.start) };var duration by rememberSaveable(initial.id) { mutableIntStateOf(initial.duration) }
    var error by remember { mutableStateOf("") };var conflict by remember { mutableStateOf<Pair<Int,String>?>(null) };var deleting by remember { mutableStateOf(false) }
    val existing=vm.state.plans.any { it.id==initial.id }
    Head(back);Title(if(existing)"编辑定时" else "添加定时");Txt("拖动圆环两端设置就寝与起床时间，拖动弧线可整体移动。",12,P.sub)
    Gap(17);Txt("方案名称",12,P.sub,750);Gap(8);Field(name,{name=it.take(16)},"例如：工作日")
    Gap(17);Txt("应用到星期",12,P.sub,750);Gap(8)
    Row(horizontalArrangement=Arrangement.spacedBy(5.dp)) { weekdays.forEach { (label,day)->Column(Modifier.weight(1f),horizontalAlignment=Alignment.CenterHorizontally) {
        Box(Modifier.aspectRatio(1f).clip(CircleShape).background(if(day in days)P.orange else P.paper).border(1.dp,P.line,CircleShape).clickable {
            if(day in days)days=days-day else { val owner=vm.state.plans.find { it.id!=initial.id&&day in it.days };if(owner!=null)conflict=day to owner.name else days=days+day }
        },contentAlignment=Alignment.Center) { Txt(label,14,if(day in days)Color.White else P.ink,750) };Gap(6);Box(Modifier.size(5.dp).background(if(vm.state.plans.any { day in it.days })P.orange else Color.Transparent,CircleShape))
    } } }
    if(error.isNotEmpty())Txt(error,11,Color(0xFFB46B43))
    Gap(16);Column(Modifier.clip(RoundedCornerShape(26.dp)).background(Brush.linearGradient(listOf(Color(0xFFF8E0C9),Color(0xFFFFF4E8),P.paper))).padding(12.dp)) { DialPanel(start,duration) { a,b->start=a;duration=b } }
    Actions(confirm="保存方案",onCancel=back,onConfirm={if(name.isBlank())error="请填写方案名称。" else if(days.isEmpty())error="请至少选择一天。" else {vm.savePlan(initial.copy(name=name.trim(),days=days,start=start,duration=duration));back()} })
    if(existing)Btn("删除方案",Modifier.fillMaxWidth(),"link") { deleting=true }
    conflict?.let { (day,owner)->Popup("这一天已有定时",{conflict=null}) { Sub("${daysLabel(listOf(day))}已属于「$owner」。你要在当前方案中更改这一天的定时吗？");Actions(confirm="更改",onCancel={conflict=null},onConfirm={days=days+day;conflict=null}) } }
    if(deleting)Popup("删除「$name」？",{deleting=false}) { Sub("这个方案的启用日期将变为空白，可以再分配给其他方案。");Actions(confirm="删除方案",onCancel={deleting=false},onConfirm={vm.update { it.copy(plans=it.plans.filterNot { p->p.id==initial.id }) };back()}) }
}
