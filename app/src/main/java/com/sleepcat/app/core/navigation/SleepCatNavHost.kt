package com.sleepcat.app.core.navigation

import android.app.Activity
import android.content.pm.ApplicationInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.*
import com.sleepcat.app.core.designsystem.prototype.*
import com.sleepcat.app.data.preferences.*
import com.sleepcat.app.data.prototype.*
import com.sleepcat.app.feature.settings.SettingsUiState
import com.sleepcat.app.feature.prototype.*

private val routes=listOf("welcome","goal","schedules","scheduleEditor","tasks","mode","permissions","home","tasksEdit","routine","outside","review","morning","reminderSettings","settings","appearance")
@Composable fun SleepCatNavHost(state:SettingsUiState,onPalette:(ThemePalette)->Unit,onMode:(ThemeMode)->Unit,onRetry:()->Unit) {
    val context=LocalContext.current
    val vm:PrototypeViewModel=viewModel(factory=viewModelFactory {initializer {PrototypeViewModel(PrototypeRepository(context))}})
    val lifecycle=LocalLifecycleOwner.current
    DisposableEffect(lifecycle) {val observer=LifecycleEventObserver {_,e->if(e==Lifecycle.Event.ON_RESUME)vm.refresh()};lifecycle.lifecycle.addObserver(observer);onDispose {lifecycle.lifecycle.removeObserver(observer)}}
    LaunchedEffect(vm,lifecycle) {
        lifecycle.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while(true) {
                vm.refresh()
                val now=java.time.ZonedDateTime.now()
                var boundary=now.toLocalDate().atTime(5,0).atZone(now.zone)
                if(!boundary.isAfter(now)) boundary=boundary.plusDays(1)
                kotlinx.coroutines.delay(java.time.Duration.between(now,boundary).toMillis().coerceAtLeast(1))
            }
        }
    }
    if(!vm.ready) {Box(Modifier.fillMaxSize().background(P.bg));return}
    val debug=(context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE)!=0
    val override=remember {if(debug)(context as? Activity)?.intent?.getStringExtra("prototype_page")?.takeIf {it in routes} else null}
    val start=remember {override?:vm.state.page.takeIf {it in routes&&it!="scheduleEditor"}?:if(vm.state.setup)"home" else "welcome"}
    val nav=rememberNavController();val entry by nav.currentBackStackEntryAsState();val route=entry?.destination?.route?:start
    var taskReturn by rememberSaveable {mutableStateOf("home")};var scheduleReturn by rememberSaveable {mutableStateOf("goal")};var previewOrigin by rememberSaveable {mutableStateOf("home")};var previewKind by rememberSaveable {mutableStateOf("light")};var recordIndex by rememberSaveable {mutableIntStateOf(vm.state.records.lastIndex)}
    var draft by rememberSaveable {mutableStateOf<Plan?>(null)}
    fun go(page:String) {if(page=="tasksEdit")taskReturn=if(route in listOf("settings","review"))route else "home";if(page in listOf("home","welcome")) {
            nav.navigate(page) {popUpTo(nav.graph.id) {inclusive=true};launchSingleTop=true}
        } else if(!nav.popBackStack(page,false)) {
            nav.navigate(page) {launchSingleTop=true}
        };vm.update {it.copy(page=page)}}
    fun edit(p:Plan?) {scheduleReturn=if(route=="goal")"goal" else "schedules";draft=p?:Plan("scheme${System.nanoTime()}","方案${vm.state.plans.size+1}",emptyList(),vm.state.target,vm.state.duration);go("scheduleEditor")}
    fun preview(kind:String) {previewOrigin=route;previewKind=kind;go("outside")}
    fun review(index:Int) {recordIndex=index;go("morning")}
    fun back() {when(route) {"home","welcome"->Unit;"goal"->go("welcome");"tasks"->go("goal");"mode"->go("tasks");"permissions"->go("mode");"tasksEdit"->go(taskReturn);"scheduleEditor"->go(scheduleReturn);"schedules","reminderSettings","appearance"->go("settings");"morning"->go("review");"outside"->go(previewOrigin);else->go("home")}}
    BackHandler(route!="home"&&route!="welcome") {back()}
    val hasNav=(vm.state.setup||override!=null)&&!(route=="scheduleEditor"&&scheduleReturn=="goal")&&route !in listOf("welcome","goal","tasks","mode","permissions","outside")
    Box(Modifier.fillMaxSize().background(if(route=="home")P.home else P.bg).imePadding()) {
        NavHost(nav,start,Modifier.fillMaxSize().padding(top=maxOf(35.dp,WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),bottom=if(hasNav&&route!="home")78.dp else 0.dp)) {
            routes.forEach { page->composable(page) {
                if(page=="home")NativeHome(vm,::go)
                else if(page=="appearance")com.sleepcat.app.feature.settings.SettingsScreen(state,onPalette,onMode,onRetry)
                else if(page=="outside")OutsideScreen(vm,previewKind,previewOrigin in listOf("mode","reminderSettings"),{go(previewOrigin)},::go)
                else Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start=21.dp,end=21.dp,top=12.dp,bottom=if(hasNav)32.dp else 34.dp)) {
                    when(page) {
                        "welcome"->WelcomeScreen(::go) {vm.update {it.copy(setup=true)};go("home")}
                        "goal","schedules"->ScheduleListScreen(vm,page=="goal",::go,::edit)
                        "scheduleEditor"->ScheduleEditorScreen(vm,draft?:Plan("debug-plan","方案${vm.state.plans.size+1}",emptyList(),vm.state.target,vm.state.duration)) {go(scheduleReturn)}
                        "tasks","tasksEdit"->TaskScreen(vm,page=="tasks",{if(page=="tasks")go("goal") else go(taskReturn)},::go)
                        "mode","permissions","reminderSettings"->ReminderScreen(vm,page,::go,::preview)
                        "review"->ReviewScreen(vm,::go,::review)
                        "morning"->MorningScreen(vm,recordIndex) {go("review")}
                        "settings"->SettingsScreen(vm,::go)
                        "routine"->RoutineScreen(vm,::go)
                    }
                }
            } }
        }
        if(hasNav)Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(78.dp).background(if(route=="home")P.home else P.paper).padding(bottom=10.dp),verticalAlignment=Alignment.CenterVertically) {
            val active=when(route) {"tasksEdit"->taskReturn;"routine"->"home";"morning"->"review";"schedules","scheduleEditor","reminderSettings","appearance"->"settings";else->route}
            listOf(Triple("home","home","首页"),Triple("review","chart","回顾"),Triple("settings","user","我的")).forEach { (id,icon,label)->val color=if(active==id)Color(0xFFC47448) else Color(0xFF97877D)
                Column(Modifier.weight(1f).fillMaxHeight().testTag("tab-$id").clickable {go(id)},verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally) {Glyph(icon,color=color);Gap(4);Txt(label,11,color,if(active==id)800 else 400)}
            }
        }
        vm.error?.let {message->Popup("本地数据",{}) {Sub(message);Btn("重试",Modifier.fillMaxWidth(),onClick=vm::retry)} }
    }
}
