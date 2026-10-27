package com.sleepcat.app.feature.tasks

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.sleepcat.app.core.designsystem.component.*

private data class DemoTask(val title: String, val minutes: Int, val note: String)
private val demoTasks = listOf(
    DemoTask("洗澡", 20, "让身体慢慢放松下来"),
    DemoTask("刷牙护肤", 10, "照顾好今天的自己"),
    DemoTask("整理明天的东西", 10, "给明天留一点从容"),
)

@Composable
fun TaskListScreen() {
    SleepCatPage {
        PageHeading("睡前清单", "样例清单 · 3 项 / 预计 40 分钟")
        demoTasks.forEachIndexed { index, task ->
            SleepCatCard {
                Text("0${index + 1}  /  ${task.minutes} 分钟", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                Text(task.title, style = MaterialTheme.typography.titleLarge)
                Text(task.note, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text("当前为只读演示。新增、编辑、排序和每日完成记录将在后续版本开放。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
