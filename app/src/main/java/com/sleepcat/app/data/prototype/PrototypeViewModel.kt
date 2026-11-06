package com.sleepcat.app.data.prototype

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class PrototypeViewModel(private val repository: PrototypeRepository): ViewModel() {
    var state by mutableStateOf(DemoData.fresh()); private set
    var ready by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set
    private val writes=Channel<PrototypeState>(Channel.CONFLATED)
    init {
        viewModelScope.launch { try { state=repository.read() } catch(e: Exception) { error="无法读取本地演示数据，请重试。" }; ready=true }
        viewModelScope.launch { for(value in writes) try { repository.write(value);error=null } catch(e: Exception) { error="更改未保存，请重试。" } }
    }
    fun update(change: (PrototypeState)->PrototypeState) { state=change(state.refreshed());writes.trySend(state) }
    fun refresh() { val next=state.refreshed();if(next!=state) update { next } }
    fun retry() { if(error?.startsWith("无法读取")==true) viewModelScope.launch { try { state=repository.read();error=null } catch(_:Exception) {} } else writes.trySend(state) }
    fun done(id: String, complete: Boolean = id !in state.night.done) = update { s ->
        if(s.tonight().none { it.id==id }) s else s.copy(night=s.night.copy(done=if(complete) (s.night.done+id).distinct() else s.night.done-id,completedAt=if(complete) s.night.completedAt+(id to LocalDateTime.now().toString()) else s.night.completedAt-id))
    }
    fun completeAll() = update { s -> val ids=s.tonight().map { it.id };s.copy(night=s.night.copy(done=(s.night.done+ids).distinct(),completedAt=s.night.completedAt+ids.filter { it !in s.night.done }.associateWith { LocalDateTime.now().toString() })) }
    fun savePlan(p: Plan) = update { s -> val plans=commitPlan(s.plans,p);s.copy(plans=plans,target=if(plans.size==1)p.start else s.target,duration=if(plans.size==1)p.duration else s.duration) }
    fun saveQuick(start: Int, duration: Int, allDays: Boolean) = update { s ->
        val key=nightKey();val active=s.scheme(key)
        if(!allDays) s.copy(quick=QuickPlan(key,start,duration)) else s.copy(quick=null,plans=commitPlan(s.plans,active?.copy(start=start,duration=duration)?:Plan("scheme${System.nanoTime()}","今天",listOf(java.time.LocalDate.parse(key).dayOfWeek.value%7),start,duration)))
    }
    fun reset() = update { DemoData.fresh() }
}
