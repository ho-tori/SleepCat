package com.sleepcat.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationThemeTest {
    val compose=createAndroidComposeRule<MainActivity>()
    @get:Rule val rules: org.junit.rules.TestRule = org.junit.rules.RuleChain.outerRule(
        object : org.junit.rules.ExternalResource() {
            override fun before() {
                kotlinx.coroutines.runBlocking {
                    com.sleepcat.app.data.prototype.PrototypeRepository(
                        androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
                    ).write(com.sleepcat.app.data.prototype.DemoData.fresh())
                }
            }
        }
    ).around(compose)
    private fun enter() {
        compose.waitUntil(10000) {compose.onAllNodesWithText("直接体验").fetchSemanticsNodes().isNotEmpty()||compose.onAllNodesWithTag("tab-home").fetchSemanticsNodes().isNotEmpty()}
        if(compose.onAllNodesWithTag("tab-home").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithTag("tab-settings").performClick()
            compose.onNodeWithText("重置演示数据").performScrollTo().performClick()
            compose.onNodeWithText("重置",substring=false).performClick()
        }
        compose.onNodeWithText("直接体验").performScrollTo().performClick()
        compose.onNodeWithTag("tab-home").performClick()
    }
    @Test fun nativeTabsAndEditableDailyTasks() {
        enter();compose.onNodeWithTag("tab-settings").performClick()
        compose.onNodeWithText("睡前小清单").performClick()
        compose.onNodeWithText("＋ 自定义事项").performScrollTo().performClick()
        compose.onNodeWithText("新增自定义事项").assertIsDisplayed()
        compose.onNodeWithText("取消").performClick()
        compose.onNodeWithContentDescription("返回").performClick()
        compose.onNodeWithTag("tab-review").performClick()
        compose.onNodeWithText("填写 / 修改反馈").performClick()
        compose.onNodeWithText("刷视频").performScrollTo().performClick()
        compose.onNodeWithText("完成").performScrollTo().performClick()
        compose.onNodeWithText("晨间反馈").assertIsDisplayed()
    }
    @Test fun tonightDraftCancelAndRecreation() {
        enter();compose.onNodeWithContentDescription("调整今晚清单").performClick()
        compose.onNodeWithText("调整今晚清单").assertIsDisplayed()
        compose.onNodeWithContentDescription("取消清单修改").performClick()
        compose.onNodeWithText("GET READY FOR BED").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("GET READY FOR BED").assertIsDisplayed()
    }
    @Test fun scheduleDraftSurvivesActivityRecreationWithoutSaving() {
        enter()
        compose.onNodeWithTag("tab-settings").performClick()
        compose.onNodeWithText("所有定时").performClick()
        compose.onNodeWithText("＋ 新增方案").performScrollTo().performClick()
        compose.onAllNodes(hasSetTextAction())[0].performTextReplacement("DraftCheck")
        compose.onNodeWithText("一",useUnmergedTree=true).performClick()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("DraftCheck").assertExists()
        compose.onNodeWithText("取消").performScrollTo().performClick()
        compose.onNodeWithText("DraftCheck").assertDoesNotExist()
    }
    @Test fun tonightEditingSurvivesRecreationAndCancelDiscardsDraft() {
        enter()
        compose.onNodeWithContentDescription("调整今晚清单").performClick()
        compose.onNodeWithText("＋ 添加事项").performClick()
        compose.onAllNodes(hasSetTextAction())[0].performTextReplacement("TemporaryCheck")
        compose.onNodeWithText("添加",substring=false).performScrollTo().performClick()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("TemporaryCheck").assertExists()
        compose.onNodeWithContentDescription("取消清单修改").performClick()
        compose.onNodeWithContentDescription("展开清单").performClick()
        compose.onNodeWithText("TemporaryCheck").assertDoesNotExist()
    }

    @Test fun customTaskValidatesAndPersistsAcrossRecreation() {
        enter()
        compose.onNodeWithTag("tab-settings").performClick()
        compose.onNodeWithText("睡前小清单").performClick()
        compose.onNodeWithText("＋ 自定义事项").performScrollTo().performClick()
        compose.onAllNodes(hasSetTextAction())[0].performTextReplacement("PersistCheck")
        compose.onAllNodes(hasSetTextAction())[1].performTextReplacement("0")
        compose.onNodeWithText("保存").performClick()
        compose.onNodeWithText("预计用时请填写 1～120 分钟").assertExists()
        compose.onAllNodes(hasSetTextAction())[1].performTextReplacement("12")
        compose.onNodeWithText("保存").performClick()
        compose.activityRule.scenario.recreate()
        compose.onAllNodesWithText("PersistCheck")[0].assertExists()
    }

    @Test fun scheduleSaveAndDeleteAreRealMutations() {
        enter()
        compose.onNodeWithTag("tab-settings").performClick()
        compose.onNodeWithText("所有定时").performClick()
        compose.onNodeWithText("＋ 新增方案").performScrollTo().performClick()
        compose.onAllNodes(hasSetTextAction())[0].performTextReplacement("SavedPlanCheck")
        compose.onNodeWithText("日",useUnmergedTree=true).performClick()
        compose.onNodeWithText("保存方案").performScrollTo().performClick()
        compose.waitForIdle()
        check(compose.onAllNodesWithText("SavedPlanCheck").fetchSemanticsNodes().size==1) {compose.onRoot(useUnmergedTree=true).printToString()}
        compose.onNodeWithText("编辑").performClick()
        compose.onNodeWithText("删除方案").performScrollTo().performClick()
        compose.onAllNodesWithText("删除方案").filter(hasAnyAncestor(isDialog()))[0].performClick()
        compose.onNodeWithText("SavedPlanCheck").assertDoesNotExist()
    }

    @Test fun settingsReminderPreviewCanExitWithoutChangingTasks() {
        enter()
        compose.onNodeWithTag("tab-settings").performClick()
        compose.onNodeWithText("提醒方式与权限").performClick()
        compose.onAllNodesWithText("查看效果 · ›")[0].performClick()
        compose.onNodeWithText("开始准备").assertIsNotEnabled()
        compose.onNodeWithText("返回设置").performClick()
        compose.onNodeWithText("提醒方式与权限").assertExists()
    }
}
