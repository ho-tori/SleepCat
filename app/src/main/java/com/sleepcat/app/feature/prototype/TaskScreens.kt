package com.sleepcat.app.feature.prototype

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.sleepcat.app.core.designsystem.prototype.*
import com.sleepcat.app.data.prototype.*

@Composable fun PrepRow(value:Int,onboarding:Boolean=false,onClick:()->Unit) { Card { Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) { Txt(if(onboarding)"开始准备的提醒" else "开始准备提醒",14,weight=700,modifier=Modifier.weight(1f));Row(Modifier.width(122.dp).clip(RoundedCornerShape(15.dp)).background(Color.White).border(1.dp,P.line,RoundedCornerShape(15.dp)).clickable(onClick=onClick).padding(11.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) { Txt(clock(value),19,weight=800);Glyph("chev") } } } }
@Composable fun TaskScreen(vm:PrototypeViewModel,onboarding:Boolean,back:()->Unit,go:(String)->Unit) {
    var time by remember { mutableStateOf(false) };var edit by remember { mutableStateOf<Task?>(null) };var new by remember { mutableStateOf(false) }
    Head(back);if(onboarding)Progress(2);Title(if(onboarding)"睡前，做点什么？" else "睡前小清单")
    if(!onboarding)Sub("设置日常事项与开始准备的提醒时间。")
    PrepRow(vm.state.prep,onboarding) { time=true };Gap(8)
    val choices=vm.state.tasks.sortedBy { t->DemoData.tasks.indexOfFirst { it.id==t.id }.let { if(it<0)99 else it } }
    choices.chunked(2).forEach { row->Row(horizontalArrangement=Arrangement.spacedBy(9.dp)) { row.forEach { t->
        Column(Modifier.weight(1f).heightIn(min=125.dp).clip(RoundedCornerShape(16.dp)).background(if(t.selected)Color(0xFFFFF0E3) else Color(0xFFFFFDF9)).border(1.dp,if(t.selected)Color(0xFFE8AA83) else P.line,RoundedCornerShape(16.dp)).padding(12.dp)) {
            Column(Modifier.fillMaxWidth().clickable { vm.update { s->s.copy(tasks=s.tasks.map { if(it.id==t.id)it.copy(selected=!it.selected) else it },night=if(t.selected)s.night.copy(done=s.night.done-t.id,completedAt=s.night.completedAt-t.id) else s.night) } }.semantics { selected=t.selected }) { Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { Txt(t.icon,23);Txt(if(t.selected)"✓" else "○",16,Color(0xFFBA7046)) };Gap(7);Txt(t.name,13,weight=750) }
            Row(Modifier.fillMaxWidth().heightIn(min=40.dp).clickable { edit=t },verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(7.dp)) { Txt("约 ${t.minutes} 分钟",11,P.sub);Txt(if(t.id.startsWith("custom"))"编辑" else "编辑时间",11,Color(0xFFAB6E49),750) }
        }
    };if(row.size==1)Spacer(Modifier.weight(1f)) };Gap(9) }
    Btn("＋ 自定义事项",Modifier.fillMaxWidth(),"secondary") { new=true }
    Card { Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { Txt("安排顺序",15,weight=800);Txt("拖动右侧手柄",11,P.sub) }
        vm.state.daily().forEachIndexed { index,t->Row(Modifier.fillMaxWidth().heightIn(min=58.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) { Txt("${index+1}",11,P.sub);Txt(t.icon,22);Txt(t.name,14,weight=700,modifier=Modifier.weight(1f));DragHandle(t.id,"拖动${t.name}排序",58f) { direction->
            vm.update { s->val list=s.tasks.toMutableList();val current=list.indexOfFirst { it.id==t.id };val selected=s.daily();val other=selected.getOrNull(selected.indexOfFirst { it.id==t.id }+direction);if(other!=null) { val target=list.indexOfFirst { it.id==other.id };java.util.Collections.swap(list,current,target) };s.copy(tasks=list) }
        } };Box(Modifier.fillMaxWidth().height(1.dp).background(P.line)) }
        if(vm.state.daily().isEmpty())Sub("先选一两件每天会做的事吧。")
    }
    if(onboarding)Actions("返回","下一步",back,{go("mode")}) else Btn("完成",Modifier.fillMaxWidth(),onClick=back)
    if(time)TimePopup("开始准备时间",vm.state.prep,{time=false}) { v->vm.update { it.copy(prep=v) };time=false }
    if(new||edit!=null)TaskPopup(edit,{new=false;edit=null},{t->vm.update { s->s.copy(tasks=if(s.tasks.any { it.id==t.id })s.tasks.map { if(it.id==t.id)t else it } else s.tasks+t) };new=false;edit=null},{id->vm.update { s->s.copy(tasks=s.tasks.filterNot { it.id==id },night=s.night.copy(done=s.night.done-id)) };edit=null})
}
@Composable fun DragHandle(id:String,label:String,rowHeight:Float,icon:String="drag",onMove:(Int)->Unit) {
    val density=LocalDensity.current.density;val move by rememberUpdatedState(onMove)
    Box(Modifier.size(44.dp).semantics { contentDescription=label;customActions=listOf(CustomAccessibilityAction("上移") { move(-1);true },CustomAccessibilityAction("下移") { move(1);true }) }.pointerInput(id) { var delta=0f;detectDragGestures(onDragStart={delta=0f},onDrag={ c,d->c.consume();delta+=d.y;if(kotlin.math.abs(delta)>rowHeight*density*.6f) { move(if(delta>0)1 else -1);delta=0f } }) },contentAlignment=Alignment.Center) { Glyph(icon,color=Color(0xFFA98A71)) }
}
@Composable fun TaskPopup(task:Task?,dismiss:()->Unit,save:(Task)->Unit,delete:(String)->Unit) {
    val default=task!=null&&!task.id.startsWith("custom")
    var name by remember { mutableStateOf(task?.name?:"") };var minutes by remember { mutableStateOf((task?.minutes?:10).toString()) };var icon by remember { mutableStateOf(task?.icon?:"🎒") };var error by remember { mutableStateOf("") }
    Popup(if(default)"${task!!.name}用时" else if(task==null)"新增自定义事项" else "编辑事项",dismiss) {
        if(!default) { Txt("名称",12,P.sub,750);Field(name,{name=it.take(18)},"例如：整理明天的东西");Gap(15);Txt("图标",12,P.sub,750);Gap(7)
            listOf("🚿","🪥","🧴","📝","🎒","📚","🧘","🫖","🎧","🧺","⭐","🌙").chunked(6).forEach { row->Row(horizontalArrangement=Arrangement.spacedBy(7.dp)) { row.forEach { x->Box(Modifier.weight(1f).height(39.dp).clip(RoundedCornerShape(10.dp)).background(if(x==icon)P.pale else Color.White).border(1.dp,if(x==icon)P.orange else P.line,RoundedCornerShape(10.dp)).clickable { icon=x },contentAlignment=Alignment.Center) { Txt(x,19) } } };Gap(7) }
        }
        Gap(15);Txt("预计用时",12,P.sub,750);Gap(7);Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) { Field(minutes,{minutes=it},modifier=Modifier.width(100.dp),numeric=true);Txt("分钟") }
        if(error.isNotEmpty())Sub(error)
        Actions(onCancel=dismiss,onConfirm={val m=minutes.toIntOrNull();if(name.isBlank())error="请填写事项名称。" else if(m==null||m !in 1..120)error="预计用时请填写 1～120 分钟" else save(Task(task?.id?:"custom${System.nanoTime()}",name.trim(),icon,m,task?.selected?:true))})
        if(task!=null&&!default)Btn("删除事项",Modifier.fillMaxWidth(),"link") { delete(task.id) }
    }
}
