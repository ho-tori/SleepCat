package com.sleepcat.app.data.prototype

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

private val Context.prototypeStore by preferencesDataStore("prototype_demo")
/** Versioned, separate local demo data; never mixes with system permission or sensor data. */
class PrototypeRepository(context: Context) {
    private val store = context.applicationContext.prototypeStore
    private val key = stringPreferencesKey("state_v1")
    suspend fun read(): PrototypeState = store.data.first()[key]?.let(::decode) ?: DemoData.fresh()
    suspend fun write(state: PrototypeState) { store.edit { it[key] = encode(state) } }
    private fun task(t: Task) = JSONObject().put("id",t.id).put("name",t.name).put("icon",t.icon).put("minutes",t.minutes).put("selected",t.selected)
    private fun plan(p: Plan) = JSONObject().put("id",p.id).put("name",p.name).put("days",JSONArray(p.days)).put("start",p.start).put("duration",p.duration)
    private fun readTask(j: JSONObject) = Task(j.getString("id"),j.getString("name"),j.getString("icon"),j.getInt("minutes"),j.optBoolean("selected",true))
    private fun JSONArray.objects() = (0 until length()).map { getJSONObject(it) }
    private fun JSONArray.strings() = (0 until length()).map { getString(it) }
    private fun JSONArray.ints() = (0 until length()).map { getInt(it) }
    private fun encode(s: PrototypeState): String = JSONObject().apply {
        put("version",1);put("setup",s.setup);put("page",s.page);put("tasks",JSONArray(s.tasks.map(::task)));put("plans",JSONArray(s.plans.map(::plan)))
        put("target",s.target);put("duration",s.duration);put("prep",s.prep);put("remind",s.remind);put("snooze",s.snooze);put("notice",s.notice);put("overlay",s.overlay)
        put("night",JSONObject().put("key",s.night.key).put("added",JSONArray(s.night.added.map(::task))).put("removed",JSONArray(s.night.removed)).put("order",JSONArray(s.night.order)).put("done",JSONArray(s.night.done)).put("completedAt",JSONObject(s.night.completedAt)))
        s.quick?.let { put("quick",JSONObject().put("key",it.key).put("start",it.start).put("duration",it.duration)) }
        put("closedNight",s.closedNight);put("suggestionWeek",s.suggestionWeek);put("reminderCount",s.reminderCount);put("skipNight",s.skipNight)
        put("records",JSONArray(s.records.map { JSONObject().put("day",it.day).put("time",it.time).put("target",it.target).put("reasons",JSONArray(it.reasons)).put("snoozes",it.snoozes) }))
    }.toString()
    private fun decode(raw: String): PrototypeState {
        val j=JSONObject(raw);val n=j.getJSONObject("night");val at=n.optJSONObject("completedAt")?:JSONObject();val q=j.optJSONObject("quick")
        return PrototypeState(setup=j.getBoolean("setup"),page=j.optString("page","home").let { when(it) { "scheduleEditor" -> if(j.getBoolean("setup")) "schedules" else "goal"; "outside" -> if(j.getBoolean("setup")) "reminderSettings" else "mode"; else -> it } },tasks=j.getJSONArray("tasks").objects().map(::readTask),plans=j.getJSONArray("plans").objects().map { Plan(it.getString("id"),it.getString("name"),it.getJSONArray("days").ints(),it.getInt("start"),it.getInt("duration")) },target=j.getInt("target"),duration=j.getInt("duration"),prep=j.getInt("prep"),remind=j.getString("remind"),snooze=j.getInt("snooze"),notice=j.getBoolean("notice"),overlay=j.getBoolean("overlay"),night=Night(n.getString("key"),n.getJSONArray("added").objects().map(::readTask),n.getJSONArray("removed").strings(),n.getJSONArray("order").strings(),n.getJSONArray("done").strings(),at.keys().asSequence().associateWith { at.getString(it) }),quick=q?.let { QuickPlan(it.getString("key"),it.getInt("start"),it.getInt("duration")) },closedNight=j.optString("closedNight"),records=j.getJSONArray("records").objects().map { Record(it.getString("day"),it.getInt("time"),it.getJSONArray("reasons").strings(),it.getInt("target"),it.getInt("snoozes")) },suggestionWeek=j.optString("suggestionWeek"),reminderCount=j.optInt("reminderCount"),skipNight=j.optString("skipNight")).refreshed()
    }
}
