package com.sleepcat.app.feature.review

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sleepcat.app.core.designsystem.component.*

@Composable
fun ReviewScreen() {
    SleepCatPage {
        PageHeading("每周回顾", "慢慢找到适合自己的睡前节奏。")
        SleepCatCard(emphasized = true) {
            Text("演示数据 · 非真实记录", style = MaterialTheme.typography.titleMedium)
            Text("以下仅说明周视图的呈现方式。当前尚未记录你的放下手机时间。", style = MaterialTheme.typography.bodyMedium)
        }
        SleepCatCard {
            Text("一周的睡前节奏", style = MaterialTheme.typography.titleLarge)
            Text("演示目标 00:00 · 负值为提前，正值为晚于目标", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            val days = listOf("周一" to "提前 18 分钟", "周二" to "晚 18 分钟", "周三" to "提前 7 分钟", "周四" to "晚 27 分钟", "周五" to "提前 12 分钟", "周六" to "晚 16 分钟", "周日" to "提前 5 分钟")
            days.forEach { (day, difference) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(day)
                    Text(difference, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        Text("不追求每一天完美，只是多了解自己一点。", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
