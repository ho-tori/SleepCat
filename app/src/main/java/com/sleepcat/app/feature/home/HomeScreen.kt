package com.sleepcat.app.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.sleepcat.app.R
import com.sleepcat.app.core.designsystem.component.*

@Composable
fun HomeScreen(onOpenTasks: () -> Unit) {
    var showPreparationInfo by rememberSaveable { mutableStateOf(false) }
    SleepCatPage {
        PageHeading("给今天一个温柔的收尾", "不必着急，从一件小事开始。")
        SleepCatCard(emphasized = true) {
            Text("目标放下手机", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("00:00", style = MaterialTheme.typography.displayMedium)
                Image(painterResource(R.drawable.ic_cat), contentDescription = null, modifier = Modifier.size(80.dp))
            }
            Text("演示目标 · 23:00 开始准备", style = MaterialTheme.typography.bodyMedium)
        }
        SleepCatCard {
            Text("今晚的小清单", style = MaterialTheme.typography.titleLarge)
            Text("洗澡、刷牙护肤、整理明天的东西\n3 件小事，给自己 40 分钟。", style = MaterialTheme.typography.bodyLarge)
            Text("样例清单 · 暂不保存完成记录", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SleepCatButton("查看睡前清单", onOpenTasks)
        }
        SleepCatButton("开始准备", { showPreparationInfo = true })
        Text("今晚也记得，给自己留一点安静的时间。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (showPreparationInfo) {
        AlertDialog(
            onDismissRequest = { showPreparationInfo = false },
            title = { Text("准备流程尚未开放") },
            text = { Text("P0 仅展示应用框架。你可以先查看睡前清单；此操作不会启动计时、通知或系统提醒。") },
            confirmButton = { TextButton(onClick = { showPreparationInfo = false }) { Text("知道了") } },
        )
    }
}
