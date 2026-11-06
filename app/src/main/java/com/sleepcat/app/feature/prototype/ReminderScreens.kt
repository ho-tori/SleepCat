package com.sleepcat.app.feature.prototype

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.sleepcat.app.core.designsystem.prototype.*
import com.sleepcat.app.data.prototype.*

@Composable fun ModeChoices(vm:PrototypeViewModel,preview:(String)->Unit) {
    listOf(Triple("light","轻提醒","顶部弹窗提醒"),Triple("strong","全屏提醒","在重要时刻更明显地提醒"),Triple("mix","混合提醒","先轻提醒，多次延后后全屏提醒")).forEach { (id,label,desc)->
        val selected=vm.state.remind==id
        Row(Modifier.fillMaxWidth().padding(top=10.dp).clip(RoundedCornerShape(18.dp)).background(if(selected)Color(0xFFFFF2E8) else Color.White).border(1.dp,if(selected)Color(0xFFE9B28C) else P.line,RoundedCornerShape(18.dp)).clickable { vm.update { it.copy(remind=id) } }.padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(13.dp)) {
            ModeThumbnail(id)
            Column(Modifier.weight(1f)) { Txt(label+(if(id=="mix")"  推荐" else ""),15,weight=800);Txt(desc,11,P.sub);if(id!="mix")Box(Modifier.clickable { preview(id) }.padding(vertical=8.dp)) { Txt("查看效果 · ›",12,Color(0xFFAA5D32),750) } }
            Box(Modifier.size(20.dp).background(Color.White,CircleShape).border(2.dp,if(selected)P.orange else Color(0xFFDBC4B4),CircleShape),contentAlignment=Alignment.Center) { if(selected)Box(Modifier.size(10.dp).background(P.orange,CircleShape)) }
        }
    }
    Card { Row(verticalAlignment=Alignment.CenterVertically) { Txt("稍后提醒间隔",14,weight=700,modifier=Modifier.weight(1f));listOf(5,10,15).forEach { n->Box(Modifier.padding(start=5.dp).clip(RoundedCornerShape(10.dp)).background(if(vm.state.snooze==n)P.orange else Color.White).clickable { vm.update { it.copy(snooze=n) } }.padding(8.dp)) { Txt("$n 分",12,if(vm.state.snooze==n)Color.White else P.ink,750) } } } }
}
@Composable fun Permissions(vm:PrototypeViewModel, onboarding:Boolean=false) {
    Card {
        if(onboarding)Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
            Txt("已选择",14,weight=700)
            Box(Modifier.background(Color(0xFFFFEFE2),CircleShape).padding(9.dp,5.dp)) {Txt(mapOf("light" to "轻提醒","strong" to "全屏提醒","mix" to "混合提醒")[vm.state.remind]!!,11,Color(0xFF9D623D))}
        } else Txt("提醒权限",14,weight=700)
        Gap(14);Box(Modifier.fillMaxWidth().height(1.dp).background(P.line));Gap(14)
        listOf(Triple("notice","消息通知","用于在约定时间提醒"),Triple("overlay","悬浮／全屏提醒","用于全屏提醒模式")).forEach { (id,label,desc)->
            if(id=="overlay")Box(Modifier.fillMaxWidth().height(1.dp).background(P.line))
            val checked=if(id=="notice")vm.state.notice else vm.state.overlay
            Row(Modifier.fillMaxWidth().padding(vertical=12.dp),verticalAlignment=Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Txt(label,14,weight=700);Txt(desc,11,P.sub) };Box(Modifier.size(43.dp,27.dp).clip(CircleShape).background(if(checked)Color(0xFF6A9E85) else Color(0xFFD9CAC0)).clickable { vm.update { if(id=="notice")it.copy(notice=!checked) else it.copy(overlay=!checked) } }.semantics { role=Role.Switch;stateDescription=if(checked)"已开启" else "已关闭";contentDescription=label }) { Box(Modifier.padding(start=if(checked)19.dp else 3.dp,top=3.dp).size(21.dp).background(Color.White,CircleShape)) } }
        }
    }
}
fun missing(s:PrototypeState) = listOfNotNull(if(!s.notice)"通知权限" else null,if(s.remind!="light"&&!s.overlay)"悬浮／全屏提醒权限" else null)
@Composable fun ReminderScreen(vm:PrototypeViewModel,page:String,go:(String)->Unit,preview:(String)->Unit) {
    val mode=page=="mode";val permissions=page=="permissions";var attempted by remember { mutableStateOf(false) }
    LaunchedEffect(attempted) {if(attempted) {kotlinx.coroutines.delay(180);attempted=false}}
    val back=if(mode)"tasks" else if(permissions)"mode" else "settings"
    Head {go(back)};if(mode||permissions)Progress(if(mode)3 else 4)
    Title(if(mode)"希望怎么提醒你？" else if(permissions)"开启提醒权限" else "提醒方式与权限")
    if(!mode&&!permissions)Sub("选择提醒的样子，预览效果，并检查所需权限。")
    if(!permissions)ModeChoices(vm,preview)
    if(!mode) {
        Permissions(vm,permissions)
        if(missing(vm.state).isNotEmpty())Card(modifier=Modifier.offset(x=if(attempted)4.dp else 0.dp),color=Color(0xFFFFF0E9),radius=15) { Txt(if(permissions)"还差一步" else "当前提醒方式还需要权限",13,Color(0xFF99553D),700);Txt("当前模式需要："+missing(vm.state).joinToString("、"),13,Color(0xFF99553D));Actions("改用轻提醒","去开启",{vm.update { it.copy(remind="light") }},{vm.update { it.copy(notice=true,overlay=it.remind!="light"||it.overlay) }}) }
    }
    if(mode)Actions("返回","下一步",{go(back)},{go("permissions")})
    else { val complete={if(missing(vm.state).isNotEmpty())attempted=true else {if(permissions)vm.update { it.copy(setup=true) };go(if(permissions)"home" else "settings") }};if(permissions)Actions("返回","完成设置",{go(back)},complete) else Btn("完成",Modifier.fillMaxWidth(),onClick=complete) }
}
@Composable fun OutsideScreen(vm:PrototypeViewModel,kind:String,locked:Boolean,back:()->Unit,go:(String)->Unit) {
    var display by remember { mutableStateOf(kind) };var confirm by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(Color(0xFF121418))) {
        Column(Modifier.padding(20.dp)) { Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { Txt("正在浏览动态",14,Color.White,800);Txt("22:50",14,Color(0xFFB9B3B9)) };Gap(32);Box(Modifier.fillMaxWidth().height(270.dp).background(Brush.linearGradient(listOf(Color(0xFF4B3E53),Color(0xFF99837D),Color(0xFF453C50))),RoundedCornerShape(17.dp)),contentAlignment=Alignment.Center) { Column(Modifier.fillMaxHeight(),verticalArrangement=Arrangement.SpaceAround,horizontalAlignment=Alignment.CenterHorizontally) {Txt("☁︎",76,Color(0xFFF5D9CA));Txt("✦",40,Color(0xFFF5D9CA))} };repeat(2) { Gap(15);Box(Modifier.fillMaxWidth(if(it==0)1f else .55f).height(12.dp).background(Color(0xFF2D2D34),CircleShape)) };Gap(20);Txt("♡ 喜欢                  ◇ 评论                  ↗ 分享",12,Color(0xFFAEABB4));Gap(20);Box(Modifier.fillMaxWidth().height(90.dp).background(Color(0xFF2D2D34),RoundedCornerShape(16.dp))) }
        Box(Modifier.align(Alignment.BottomEnd).padding(end=16.dp,bottom=22.dp).clip(CircleShape).background(Color(0x22FFFFFF)).border(1.dp,Color(0x42FFFFFF),CircleShape).clickable(onClick=back).padding(13.dp,9.dp)) {Txt(if(locked)"返回设置" else "返回首页",12,Color.White)}
        if(display.isEmpty()&&vm.state.reminderCount>0&&vm.state.skipNight!=nightKey())Btn("模拟下一次提醒",Modifier.align(Alignment.BottomCenter).padding(bottom=83.dp),"secondary") { display=if(vm.state.remind!="light"&&vm.state.reminderCount>=2)"strong" else "light" }
        val postpone={vm.update { it.copy(reminderCount=it.reminderCount+1) };display=""}
        if(display=="light")Column(Modifier.padding(top=21.dp,start=12.dp,end=12.dp).fillMaxWidth().clip(RoundedCornerShape(19.dp)).background(Color(0xFFFFF8F0)).border(1.dp,Color(0xFFF9DED0),RoundedCornerShape(19.dp)).padding(13.dp)) { Row(verticalAlignment=Alignment.CenterVertically) { Cat(Modifier.size(35.dp));Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)) { Txt("SleepCat",14,weight=700);Txt("该开始睡前准备啦",12,Color(0xFF77665D)) };Txt("现在",11,P.sub) };Gap(11);Row(horizontalArrangement=Arrangement.spacedBy(5.dp)) { PreviewButton("开始准备",Modifier.weight(1f),true,locked) { go("routine") };PreviewButton("延后 ${vm.state.snooze} 分",Modifier.weight(1f),false,locked,onClick=postpone);PreviewButton("今晚跳过",Modifier.weight(1f),false,locked) { confirm=true } } }
        if(display=="strong")Column(Modifier.fillMaxSize().background(Color(0xFF252429)).padding(horizontal=20.dp,vertical=25.dp),horizontalAlignment=Alignment.CenterHorizontally) { Gap(23);Txt("SleepCat · 该休息啦",11,Color(0xFFCEBBAF),800);Gap(11);Txt("喂！",37,Color(0xFFFFF4E9),810,line=1.2f);Txt("不许再刷啦！",37,Color(0xFFFFC593),810,line=1.2f);Cat(Modifier.weight(1f).fillMaxWidth().heightIn(max=300.dp));Btn("现在开始准备",Modifier.fillMaxWidth(),"secondary",!locked) {go("routine")};Gap(9);Btn("再等 ${vm.state.snooze} 分钟",Modifier.fillMaxWidth(),"quiet",!locked,onClick=postpone);Gap(9);Btn("今晚不再提醒",Modifier.fillMaxWidth(),"quiet",!locked) {confirm=true};Btn("退出预览",Modifier.fillMaxWidth(),"link",onClick=back) }
        if(confirm)Popup("今晚不再提醒？",{confirm=false}) { Sub("今晚的提醒会停止，明天可以继续。");Actions("返回","今晚跳过",{confirm=false},{vm.update {it.copy(skipNight=nightKey())};confirm=false;display=""}) }
    }
}

/** Inert in settings preview, exactly as the HTML disabled controls. */
@Composable private fun PreviewButton(text:String,modifier:Modifier,primary:Boolean,locked:Boolean,onClick:()->Unit) {
    Box(modifier.height(38.dp).alpha(if(locked).5f else 1f).clip(RoundedCornerShape(9.dp)).background(if(primary)P.ink else Color(0xFFF1E6DC)).clickable(enabled=!locked,onClick=onClick),contentAlignment=Alignment.Center) {Txt(text,11,if(primary)Color.White else Color(0xFF725243),800)}
}

@Composable private fun ModeThumbnail(kind:String) {
    Box(Modifier.size(81.dp,108.dp).clip(RoundedCornerShape(13.dp)).background(Color(0xFF292933))) {
        Box(Modifier.padding(12.dp).fillMaxWidth().height(74.dp).background(Color(0xFF45454D),RoundedCornerShape(8.dp)))
        if(kind=="light")Row(Modifier.padding(top=12.dp,start=5.dp,end=5.dp).fillMaxWidth().height(34.dp).background(Color(0xFFFAEEE3),RoundedCornerShape(7.dp)).padding(4.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(4.dp)) {Txt("🐱",17);Txt("该准备睡啦",8,weight=800)}
        if(kind=="strong")Box(Modifier.fillMaxSize().background(Color(0xFFF7CEAA)),contentAlignment=Alignment.Center) {
            Txt("该收\n手机啦！",23,Color(0xFF493024),950,Modifier.width(81.dp),align=androidx.compose.ui.text.style.TextAlign.Center,line=1.05f)
            Cat(Modifier.align(Alignment.BottomEnd).padding(end=7.dp,bottom=6.dp).size(46.dp,49.dp))
        }
        if(kind=="mix") {
            Box(Modifier.padding(start=9.dp,end=9.dp,top=9.dp,bottom=8.dp).fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF55505B),Color(0xFF35333C))),RoundedCornerShape(8.dp)))
            Row(Modifier.padding(start=5.dp,end=5.dp,top=14.dp).fillMaxWidth().height(34.dp).background(Color(0xFFFFF4E9),RoundedCornerShape(8.dp)).padding(4.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                Cat(Modifier.size(25.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFF8DECA)))
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {Box(Modifier.fillMaxWidth().height(3.dp).background(Color(0xFFBDA391),CircleShape));Box(Modifier.fillMaxWidth(.7f).height(3.dp).background(Color(0xFFD9C6B7),CircleShape))}
            }
            Box(Modifier.align(Alignment.BottomEnd).padding(end=7.dp,bottom=9.dp).size(55.dp,54.dp).background(Brush.linearGradient(listOf(Color(0xFFF6CFAF),Color(0xFFECA975))),RoundedCornerShape(11.dp)).border(2.dp,Color(0xFFF9DDC4),RoundedCornerShape(11.dp)),contentAlignment=Alignment.Center) {
                Txt("☾",29,Color(0xFFFFF5E8));Box(Modifier.align(Alignment.TopEnd).padding(10.dp).size(4.dp).background(Color(0xFFFFF1DD),CircleShape))
            }
        }
    }
}
