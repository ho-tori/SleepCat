package com.sleepcat.app.core.designsystem.prototype

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import com.sleepcat.app.R

object P {
    val bg=Color(0xFFFAF6F0);val paper=Color(0xFFFFFCF7);val home=Color(0xFFF9E9D8)
    val ink=Color(0xFF392C29);val sub=Color(0xFF8A7770);val line=Color(0xFFEEE4DB)
    val orange=Color(0xFFDE8651);val pale=Color(0xFFFCE8D9);val green=Color(0xFF438C72)
}
@Composable fun Txt(text: String, size: Int=14, color: Color=P.ink, weight: Int=400, modifier: Modifier=Modifier, align: TextAlign?=null, line: Float=1.46f) {
    val tracking=when {size==48->-2f;size==44->-1.6f;size==30->-.8f;size==9&&text.any {it in 'A'..'Z'}->1.35f;size==11&&text=="PUT THE PHONE DOWN"->2.1f;else->0f}
    Text(text,modifier,color,fontSize=size.sp,fontWeight=FontWeight(weight),lineHeight=(size*line).sp,letterSpacing=tracking.sp,textAlign=align,style=TextStyle(platformStyle=PlatformTextStyle(includeFontPadding=false),lineHeightStyle=LineHeightStyle(LineHeightStyle.Alignment.Center,LineHeightStyle.Trim.Both)))
}
@Composable fun Gap(h: Int=10) = Spacer(Modifier.height(h.dp))
@Composable fun Title(text: String) { Gap(19);Txt(text,30,weight=810,line=1.28f);Gap(10) }
@Composable fun Sub(text: String, modifier: Modifier=Modifier) = Txt(text,13,P.sub,modifier=modifier,line=1.55f)
@Composable fun Card(modifier: Modifier=Modifier, color: Color=P.paper, radius: Int=20, content: @Composable ColumnScope.()->Unit) {
    Column(modifier.fillMaxWidth().padding(vertical=6.dp).clip(RoundedCornerShape(radius.dp)).background(color).border(1.dp,P.line,RoundedCornerShape(radius.dp)).padding(17.dp),content=content)
}
@Composable fun Btn(text: String, modifier: Modifier=Modifier, kind: String="primary", enabled: Boolean=true, onClick:()->Unit) {
    val bg=when { !enabled->Color(0xFFEEE6DC);kind=="primary"->P.ink;kind=="orange"->P.orange;kind=="link"->Color.Transparent;kind=="schedule"->Color(0xFFFFF3E9);kind=="quiet"->Color(0xFFF5EDE6);else->Color(0xFFFFFDF9) }
    val fg=when { !enabled->Color(0xFFA18B76);kind=="primary"||kind=="orange"->Color.White;kind=="link"->Color(0xFFB66B42);kind=="schedule"->Color(0xFFAC653E);else->P.ink }
    val radius=if(kind=="schedule")18.dp else 14.dp
    Box(modifier.defaultMinSize(minHeight=if(kind=="link")40.dp else if(kind=="schedule")52.dp else 48.dp).clip(RoundedCornerShape(radius)).background(bg).then(if(kind=="secondary"||kind=="schedule") Modifier.border(1.dp,if(kind=="schedule")Color(0xFFE8C6AD) else Color(0xFFE8DBD0),RoundedCornerShape(radius)) else Modifier).clickable(enabled=enabled,role=Role.Button,onClick=onClick).padding(horizontal=15.dp,vertical=if(kind=="link")8.dp else 12.dp),contentAlignment=Alignment.Center) { Txt(text,14,fg,750,align=TextAlign.Center) }
}
fun Modifier.dashedBorder(color:Color,radius:Int)=drawBehind {
    drawRoundRect(color,cornerRadius=androidx.compose.ui.geometry.CornerRadius(radius.dp.toPx()),style=Stroke(1.dp.toPx(),pathEffect=PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(),3.dp.toPx()))))
}
@Composable fun Actions(cancel: String="取消", confirm: String="保存", onCancel:()->Unit, onConfirm:()->Unit) { Gap(16);Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) { Btn(cancel,Modifier.weight(1f),"secondary",onClick=onCancel);Btn(confirm,Modifier.weight(1f),onClick=onConfirm) } }
@Composable fun Cat(modifier: Modifier=Modifier, welcome: Boolean=false) {
    val original=ImageBitmap.imageResource(if(welcome)R.drawable.welcome_cat_img else R.drawable.cat_img)
    val density=androidx.compose.ui.platform.LocalDensity.current
    BoxWithConstraints(modifier) {
        val width=with(density) {maxWidth.roundToPx()}.coerceAtLeast(1)
        val height=with(density) {maxHeight.roundToPx()}.coerceAtLeast(1)
        val bitmap=remember(original,width,height) {
            var bitmap=original.asAndroidBitmap()
            // Downsample in stages so tiny preview line art retains antialiasing.
            while(bitmap.width>width*2 && bitmap.height>height*2) {
                bitmap=android.graphics.Bitmap.createScaledBitmap(bitmap,bitmap.width/2,bitmap.height/2,true)
            }
            bitmap.asImageBitmap()
        }
        Image(bitmap,null,Modifier.fillMaxSize(),filterQuality=FilterQuality.High)
    }
}
internal val paths=mapOf("back" to "m15 18-6-6 6-6","home" to "m3 10 9-7 9 7v10a1 1 0 0 1-1 1h-5v-7H9v7H4a1 1 0 0 1-1-1z","chart" to "M4 20V11m8 9V4m8 16v-7","user" to "M4 21a8 8 0 0 1 16 0","check" to "m5 12 4 4 10-10","chev" to "m9 18 6-6-6-6","down" to "m7 10 5 5 5-5","pen" to "m15 5 4 4M4 20l5-1 11-11a2.8 2.8 0 0 0-4-4L5 15Z","cross" to "m6 6 12 12M18 6 6 18","grip" to "M5 7h14M5 12h14M5 17h14","drag" to "M9 5h.01m0 7h.01m0 7h.01M15 5h.01m0 7h.01m0 7h.01","clock" to "M12 7v5l3 2","bed" to "M3 18V7m18 11V9H3m0 6h18M7 9V6h10v3","alarm" to "m4 3-3 3m19-3 3 3M12 9v4l3 2M6 20l-1 2m13-2 1 2","sun" to "M12 2v2m0 16v2M2 12h2m16 0h2M5 5l1.5 1.5m11 11L19 19M5 19l1.5-1.5m11-11L19 5","moon" to "M20.8 13.2A8.5 8.5 0 0 1 10.8 3.2 8.5 8.5 0 1 0 20.8 13.2Z")
@Composable fun Glyph(name: String, modifier: Modifier=Modifier.size(19.dp), color: Color=P.ink) {
    val path=remember(name) { PathParser().parsePathString(paths[name]?:paths.getValue("check")).toPath() }
    Canvas(modifier) { scale(size.width/24,size.height/24,pivot=androidx.compose.ui.geometry.Offset.Zero) {
        drawPath(path,color,style=Stroke(1.8f,cap=StrokeCap.Round,join=StrokeJoin.Round))
        if(name=="sun")drawCircle(color,4f,androidx.compose.ui.geometry.Offset(12f,12f),style=Stroke(1.8f))
        if(name=="user")drawCircle(color,4f,androidx.compose.ui.geometry.Offset(12f,8f),style=Stroke(1.8f))
        if(name=="clock"||name=="alarm")drawCircle(color,if(name=="clock")8.5f else 7f,androidx.compose.ui.geometry.Offset(12f,if(name=="clock")12f else 13f),style=Stroke(1.8f))
    } }
}
@Composable fun IconBtn(name: String, label: String, modifier: Modifier=Modifier, onClick:()->Unit) { Box(modifier.size(36.dp).clip(RoundedCornerShape(13.dp)).background(P.paper).border(1.dp,P.line,RoundedCornerShape(13.dp)).clickable(onClick=onClick).semantics { contentDescription=label },contentAlignment=Alignment.Center) { Glyph(name) } }
@Composable fun Head(back:()->Unit) { Row(Modifier.fillMaxWidth().height(36.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) { IconBtn("back","返回",onClick=back);Brand() } }
@Composable fun Brand(large: Boolean=false) { Row(Modifier.then(if(large)Modifier.height(42.dp) else Modifier),verticalAlignment=Alignment.CenterVertically) { Box(Modifier.size(if(large)11.dp else 9.dp).background(P.orange,CircleShape));Spacer(Modifier.width(8.dp));Txt("SleepCat",if(large)27 else 16,weight=800) } }
@Composable fun Progress(step: Int) { Gap(16);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { Txt("初始设置",11,P.sub);Txt("$step / 4",11,P.sub) };Gap(9);Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) { repeat(4) { Box(Modifier.weight(1f).height(4.dp).background(if(it<step)P.orange else Color(0xFFEAE0D5),CircleShape)) } };Gap(3) }
@Composable fun Field(value: String, change:(String)->Unit, placeholder: String="", modifier: Modifier=Modifier, numeric: Boolean=false) {
    BasicTextField(value,change,modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(Color.White).border(1.dp,Color(0xFFE5D7CB),RoundedCornerShape(13.dp)).padding(14.dp),textStyle=TextStyle(color=P.ink,fontSize=14.sp),singleLine=true,keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=if(numeric)androidx.compose.ui.text.input.KeyboardType.Number else androidx.compose.ui.text.input.KeyboardType.Text),decorationBox={ inner->Box { if(value.isEmpty())Txt(placeholder,14,P.sub);inner() } })
}
@Composable fun Popup(title: String, dismiss:()->Unit, content:@Composable ColumnScope.()->Unit) {
    Dialog(dismiss,properties=DialogProperties(usePlatformDefaultWidth=false)) { Column(Modifier.fillMaxWidth().padding(17.dp).heightIn(max=740.dp).clip(RoundedCornerShape(23.dp)).background(P.paper).verticalScroll(rememberScrollState()).padding(20.dp)) { Txt(title,22,weight=700,line=1.3f);Gap(10);content() } }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable fun Wheel(value: Int, change:(Int)->Unit) {
    val h=rememberLazyListState(24+value/60);val m=rememberLazyListState(12+com.sleepcat.app.data.prototype.wheelMinuteIndex(value))
    val scope=rememberCoroutineScope()
    LaunchedEffect(h,m) { snapshotFlow { h.firstVisibleItemIndex to m.firstVisibleItemIndex }.collect { (a,b)->change(a%24*60+b%12*5) } }
    Box(Modifier.fillMaxWidth().height(165.dp),contentAlignment=Alignment.Center) {
        Box(Modifier.width(204.dp).height(43.dp).background(Color(0xFFF4EAE1),RoundedCornerShape(14.dp)))
        Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(7.dp)) {
            listOf(h to 24,m to 12).forEachIndexed { col,(state,count)->
                if(col==1)Txt(":",22,weight=800)
                LazyColumn(Modifier.width(85.dp).height(164.dp),state=state,contentPadding=PaddingValues(vertical=60.5.dp),flingBehavior=rememberSnapFlingBehavior(state)) {
                    items(count*3) { i-> val selected=state.firstVisibleItemIndex==i;Box(Modifier.height(43.dp).fillMaxWidth().clickable { scope.launch { state.animateScrollToItem(i) } },contentAlignment=Alignment.Center) { Txt("%02d".format(i%count*(if(col==1)5 else 1)),if(selected)26 else 20,if(selected)P.ink else Color(0xFFBEB1A6),if(selected)820 else 650) } }
                }
            }
        }
    }
}
@Composable fun TimePopup(title: String, initial: Int, dismiss:()->Unit, save:(Int)->Unit) { var time by remember { mutableIntStateOf(initial) };Popup(title,dismiss) { Wheel(initial) { time=it };Actions(onCancel=dismiss,onConfirm={save(time)}) } }
