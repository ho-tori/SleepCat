package com.sleepcat.app.core.designsystem.prototype

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.*
import com.sleepcat.app.data.prototype.*
import kotlin.math.*

@Composable fun SleepDial(start: Int, duration: Int, modifier: Modifier=Modifier, change:(Int,Int)->Unit) {
    val latest by rememberUpdatedState(start to duration)
    val callback by rememberUpdatedState(change)
    fun point(minute: Float,r:Float=135f):Offset { val a=minute/1440f*2*PI-PI/2;return Offset(160+cos(a).toFloat()*r,160+sin(a).toFloat()*r) }
    Canvas(modifier.size(290.dp).semantics { contentDescription="24小时双端睡眠环，就寝${clock(start)}，起床${clock(start+duration)}" }.pointerInput(Unit) {
        var kind="";var last=0f;var delta=0f;var initial=0 to 480
        fun minute(p:Offset)=((atan2(p.y-size.height/2f,p.x-size.width/2f)+PI.toFloat()/2)/(2*PI.toFloat())*1440+1440)%1440
        detectDragGestures(onDragStart={ p->
            val scale=size.width/320f;val a=point(latest.first.toFloat())*scale;val b=point((latest.first+latest.second).toFloat())*scale
            val fromCenter=(p-Offset(size.width/2f,size.height/2f)).getDistance()/scale
            kind=when { (p-a).getDistance()<30*scale->"start";(p-b).getDistance()<30*scale->"end";fromCenter in 112f..158f->"body";else->"" }
            initial=latest;last=minute(p);delta=0f
        },onDrag={ c,_->if(kind.isNotEmpty()) { c.consume();val next=minute(c.position);var d=next-last;if(d>720)d-=1440;if(d< -720)d+=1440;delta+=d;last=next
            when(kind) { "body"->callback(mod(((initial.first+delta)/5).roundToInt()*5),initial.second);"start"->{ val length=snapDuration((initial.second-delta).roundToInt());callback(mod(initial.first+initial.second-length),length) };else->callback(initial.first,snapDuration((initial.second+delta).roundToInt())) }
        } })
    }) {
        scale(size.width/320,size.height/320,pivot=Offset.Zero) {
            drawCircle(Color(0xFFEEE4DA),135f,Offset(160f,160f),style=Stroke(42f))
            repeat(96) { i-> val t=i/95f;drawArc(lerp(Color(0xFFD58A58),Color(0xFFF6DABB),t),start/4f-90+duration/4f*i/96,(duration/4f/96+1).coerceAtMost(duration/4f),false,Offset(25f,25f),Size(270f,270f),style=Stroke(42f)) }
            repeat(144) { i->drawLine(Color(0xFFC6B8AC),point(i*10f,if(i%6==0)104f else 108f),point(i*10f,113f),if(i%6==0)1.2f else .7f) }
            val paint=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { textAlign=android.graphics.Paint.Align.CENTER }
            for(h in 0..22 step 2) { val p=point(h*60f,92f);paint.color=(if(h%6==0)P.ink else Color(0xFF998578)).toArgb();paint.textSize=if(h%6==0)17f else 16f;drawContext.canvas.nativeCanvas.drawText("$h",p.x,p.y+5,paint) }
            fun glyph(name:String,center:Offset,side:Float,color:Color) {
                translate(center.x-side/2,center.y-side/2) {
                    scale(side/24,side/24,pivot=Offset.Zero) {
                        drawPath(PathParser().parsePathString(paths.getValue(name)).toPath(),color,style=Stroke(1.7f,cap=StrokeCap.Round,join=StrokeJoin.Round))
                        if(name=="sun")drawCircle(color,4f,Offset(12f,12f),style=Stroke(1.7f))
                        if(name=="alarm")drawCircle(color,7f,Offset(12f,13f),style=Stroke(1.7f))
                    }
                }
            }
            glyph("moon",Offset(160f,102f),20f,Color(0xFFC48D64))
            glyph("sun",Offset(160f,223f),20f,Color(0xFFC48D64))
            listOf(start to false,(start+duration) to true).forEach { (m,end)->
                val p=point(m.toFloat())
                drawCircle(if(end)Color(0xFFF6DABB) else Color(0xFFD58A58),21f,p)
                glyph(if(end)"alarm" else "bed",p,20f,Color(0xFF69432D))
            }
        }
    }
}
@Composable fun DialPanel(start:Int,duration:Int,onChange:(Int,Int)->Unit) {
    Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly) { listOf("就寝" to start,"起床" to start+duration).forEach { (label,time)->Column(horizontalAlignment=Alignment.CenterHorizontally) { Row(verticalAlignment=Alignment.CenterVertically) {Glyph(if(label=="就寝")"moon" else "sun",Modifier.size(12.dp),P.sub);Txt(label,12,P.sub)};Txt(clock(time),31,weight=750) } } }
        Gap(5);SleepDial(start,duration,change=onChange);Gap(5);Txt(durationText(duration),21,weight=750)
    }
}
