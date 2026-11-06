package com.sleepcat.app.data.prototype

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek
import kotlin.math.roundToInt

data class Task(val id: String, val name: String, val icon: String, val minutes: Int, val selected: Boolean = true) : java.io.Serializable
data class Plan(val id: String, val name: String, val days: List<Int>, val start: Int, val duration: Int) : java.io.Serializable
data class QuickPlan(val key: String, val start: Int, val duration: Int) : java.io.Serializable
data class Night(val key: String, val added: List<Task> = emptyList(), val removed: List<String> = emptyList(), val order: List<String> = emptyList(), val done: List<String> = emptyList(), val completedAt: Map<String,String> = emptyMap()) : java.io.Serializable
data class Record(val day: String, val time: Int, val reasons: List<String> = emptyList(), val target: Int = 0, val snoozes: Int = 0)
data class PrototypeState(
    val setup: Boolean = false, val page: String = "welcome", val tasks: List<Task> = DemoData.tasks,
    val plans: List<Plan> = emptyList(), val target: Int = 0, val duration: Int = 480, val prep: Int = 1380,
    val remind: String = "mix", val snooze: Int = 10, val notice: Boolean = true, val overlay: Boolean = false,
    val night: Night = Night(nightKey()), val quick: QuickPlan? = null, val closedNight: String = "",
    val records: List<Record> = DemoData.records, val suggestionWeek: String = "", val reminderCount: Int = 0, val skipNight: String = ""
) {
    fun daily() = tasks.filter { it.selected }
    fun tonight(n: Night = night, completedFirst: Boolean = true): List<Task> {
        val order = if(n.order.isEmpty()) tasks.map { it.id } + n.added.map { it.id } else n.order
        val list = (daily() + n.added).filter { it.id !in n.removed }.sortedBy { order.indexOf(it.id).let { i -> if(i<0) Int.MAX_VALUE else i } }
        return if(completedFirst) list.filter { it.id in n.done }.sortedBy { n.done.indexOf(it.id) } + list.filter { it.id !in n.done } else list
    }
    fun scheme(key: String = nightKey()) = plans.find { LocalDate.parse(key).dayOfWeek.value % 7 in it.days }
    fun effective(key: String = nightKey()): QuickPlan = quick?.takeIf { it.key == key } ?: scheme(key)?.let { QuickPlan(key,it.start,it.duration) } ?: QuickPlan(key,target,duration)
    fun refreshed(key: String = nightKey()) = if(night.key==key) this else copy(night=Night(key), reminderCount=0, skipNight="")
}
fun nightKey(now: LocalDateTime = LocalDateTime.now()): String = (if(now.hour<5) now.minusDays(1) else now).toLocalDate().toString()
fun weekKey() = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString()
fun mod(value: Int) = ((value % 1440) + 1440) % 1440
fun clock(value: Int) = "%02d:%02d".format(mod(value)/60, mod(value)%60)
fun snapDuration(value: Int) = ((value/5.0).roundToInt()*5).coerceIn(5,1435)
fun durationText(value: Int) = listOfNotNull((value/60).takeIf { it>0 }?.let { "$it 小时" }, (value%60).takeIf { it>0 }?.let { "$it 分钟" }).joinToString(" ")
fun delayMinutes(record: Record): Int {
    fun extended(v: Int) = if(v<720) v+1440 else v
    return extended(record.time)-extended(record.target)
}
fun commitPlan(plans: List<Plan>, plan: Plan): List<Plan> = plans.map { if(it.id==plan.id) plan else it.copy(days=it.days-plan.days.toSet()) }.let { if(plans.none { p->p.id==plan.id }) it+plan else it }
val weekdays = listOf("一" to 1,"二" to 2,"三" to 3,"四" to 4,"五" to 5,"六" to 6,"日" to 0)
fun daysLabel(days: List<Int>) = weekdays.filter { it.second in days }.joinToString("、") { "周${it.first}" }.ifEmpty { "未设置启用日期" }
/** Explicitly local prototype fixtures. These are not measured sleep records. */
object DemoData {
    val tasks = listOf(Task("bath","洗澡","🚿",20),Task("skincare","刷牙护肤","🪥",10),Task("pack","整理明天的东西","🎒",10,false),Task("stretch","拉伸","🧘",5,false),Task("read","整理书桌","📚",8,false))
    val records = listOf(Record("一",1422),Record("二",18,listOf("刷视频"),snoozes=1),Record("三",1433),Record("四",27,listOf("聊天"),snoozes=2),Record("五",1428),Record("六",16,listOf("刷视频"),snoozes=1),Record("日",1435))
    fun fresh() = PrototypeState()
}

/** JavaScript Math.round minute selection, including the repeated wheel wrap. */
fun wheelMinuteIndex(value: Int) = (mod(value) % 60 / 5.0).roundToInt()
