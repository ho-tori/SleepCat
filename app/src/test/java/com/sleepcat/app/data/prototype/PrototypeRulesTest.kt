package com.sleepcat.app.data.prototype

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class PrototypeRulesTest {
    @Test fun nightChangesAtFiveRatherThanMidnight() {
        assertEquals("2026-11-02", nightKey(LocalDateTime.parse("2026-11-03T04:59:00")))
        assertEquals("2026-11-03", nightKey(LocalDateTime.parse("2026-11-03T05:00:00")))
    }
    @Test fun assigningDayMovesOwnershipOnlyOnCommit() {
        val first = Plan("a", "工作日", listOf(1,2), 1380, 480)
        val next = Plan("b", "周二", listOf(2), 0, 540)
        val original = listOf(first)
        val saved = commitPlan(original, next)
        assertEquals(listOf(1,2), original[0].days)
        assertEquals(listOf(1), saved[0].days)
        assertEquals(listOf(2), saved[1].days)
    }
    @Test fun temporaryTargetExpiresWithNight() {
        val state = DemoData.fresh().copy(plans=listOf(Plan("a","周二",listOf(2),1380,480)), quick=QuickPlan("2026-11-03",30,420))
        assertEquals(30, state.effective("2026-11-03").start)
        assertEquals(0, state.effective("2026-11-04").start)
    }
    @Test fun completionOrderIsStableAndUndoRestoresPlanOrder() {
        val state = DemoData.fresh().copy(night=Night("2026-11-03",done=listOf("skincare")))
        assertEquals(listOf("skincare","bath"),state.tonight().map { it.id })
        assertEquals(listOf("bath","skincare"),state.copy(night=state.night.copy(done=emptyList())).tonight().map { it.id })
    }
    @Test fun timeFormattingWrapsAcrossMidnight() {
        assertEquals("23:55", clock(-5))
        assertEquals("08:00", clock(1920))
        assertEquals(5, snapDuration(0))
        assertEquals(1435, snapDuration(1500))
    }
    @Test fun feedbackWheelRoundsMinutesLikeThePrototype() {
        assertEquals(4, wheelMinuteIndex(18))
        assertEquals(12, wheelMinuteIndex(59))
        assertEquals(0, wheelMinuteIndex(1))
    }
}
