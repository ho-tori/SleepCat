package com.sleepcat.app

import androidx.test.platform.app.InstrumentationRegistry
import com.sleepcat.app.data.prototype.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class PrototypePersistenceTest {
    @Test fun interruptedSetupEditorReturnsToSetupList()=runBlocking {
        val repo=PrototypeRepository(InstrumentationRegistry.getInstrumentation().targetContext)
        try {
            repo.write(DemoData.fresh().copy(page="scheduleEditor",setup=false))
            assertEquals("goal",repo.read().page)
            repo.write(DemoData.fresh().copy(page="scheduleEditor",setup=true))
            assertEquals("schedules",repo.read().page)
        } finally {repo.write(DemoData.fresh())}
    }
    @Test fun localDemoRecordsAndTonightChangesRoundTrip()=runBlocking {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val repo=PrototypeRepository(context)
        val original=DemoData.fresh().copy(setup=true,page="home",prep=1350,night=Night(nightKey(),added=listOf(Task("tonight-test","临时事项","⭐",7)),done=listOf("bath"),completedAt=mapOf("bath" to "2026-11-06T23:00:00")),quick=QuickPlan(nightKey(),15,480))
        try {repo.write(original);assertEquals(original,PrototypeRepository(context).read())} finally {repo.write(DemoData.fresh())}
    }
}
