package com.sleepcat.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationThemeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun tabsTaskBackAndPlaceholderWork() {
        compose.onNodeWithText("查看睡前清单").performScrollTo().performClick()
        compose.onNodeWithText("刷牙护肤").assertIsDisplayed()
        compose.onNodeWithContentDescription("返回首页").performClick()
        compose.onNodeWithText("开始准备").performScrollTo().performClick()
        compose.onNodeWithText("准备流程尚未开放").assertIsDisplayed()
        compose.onNodeWithText("知道了").performClick()
        repeat(2) { compose.onNodeWithText("回顾").performClick() }
        compose.onNodeWithText("演示数据 · 非真实记录").assertIsDisplayed()
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("查看睡前清单").assertExists()
    }

    @Test fun themeOptionsRemainIndependentAndSurviveActivityRecreation() {
        compose.onNodeWithTag("tab-settings").performClick()
        for (palette in listOf("暖橘", "薄荷绿", "雾蓝")) {
            compose.onNodeWithText(palette).performScrollTo().performClick()
            for (mode in listOf("跟随系统", "浅色", "深色")) {
                compose.onNodeWithText(mode).performScrollTo().performClick()
                compose.waitUntil(5000) {
                    compose.onAllNodesWithText("当前：$palette · $mode").fetchSemanticsNodes().isNotEmpty()
                }
            }
        }
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("当前：雾蓝 · 深色").assertExists()
        compose.onNodeWithText("首页").performClick()
        compose.onNodeWithText("查看睡前清单").performScrollTo().performClick()
        compose.onNodeWithText("睡前清单").assertIsDisplayed()
        compose.onNodeWithContentDescription("返回首页").performClick()
        compose.onNodeWithTag("tab-settings").performClick()
        compose.onNodeWithText("暖橘").performScrollTo().performClick()
        compose.onNodeWithText("跟随系统").performScrollTo().performClick()
    }
}
