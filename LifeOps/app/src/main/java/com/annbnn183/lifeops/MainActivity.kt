package com.annbnn183.lifeops

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class LifeRule(val title: String, val detail: String)
private data class DailyAction(val title: String, val category: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LifeOpsApp() }
    }
}

@Composable
private fun LifeOpsApp() {
    MaterialTheme {
        var tab by remember { mutableStateOf(0) }
        Scaffold(bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, icon = {}, label = { Text("今天") })
                NavigationBarItem(selected = tab == 1, onClick = { tab = 1 }, icon = {}, label = { Text("规则") })
                NavigationBarItem(selected = tab == 2, onClick = { tab = 2 }, icon = {}, label = { Text("复盘") })
            }
        }) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when (tab) { 0 -> TodayScreen(); 1 -> RulesScreen(); else -> ReviewScreen() }
            }
        }
    }
}

@Composable
private fun TodayScreen() {
    var done by remember { mutableStateOf(setOf<Int>()) }
    val actions = listOf(DailyAction("完成一件能推动现实进展的事", "行动"), DailyAction("先查现成方案，再决定是否自己做", "原则"), DailyAction("遵守已经设定的规则", "执行"))
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("LifeOps", style = MaterialTheme.typography.headlineLarge) }
        item { Text("今天先做重要的事。", style = MaterialTheme.typography.titleMedium) }
        item { Text("今日执行 ${done.size}/${actions.size}", style = MaterialTheme.typography.titleSmall) }
        items(actions.indices.toList()) { i ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) { Text(actions[i].category, style = MaterialTheme.typography.labelMedium); Text(actions[i].title, style = MaterialTheme.typography.bodyLarge) }
                    Checkbox(checked = i in done, onCheckedChange = { checked -> done = if (checked) done + i else done - i })
                }
            }
        }
        item { Text("初始界面骨架：保存、规则管理及复盘功能待实现。", style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun RulesScreen() {
    val rules = listOf(LifeRule("先行动，再优化", "研究不能无限延长；达到足够信息就执行小步验证。"), LifeRule("固定复盘日修改规则", "平时执行规则，修改时记录理由和版本。"), LifeRule("先寻找成熟方案", "优先使用现成、验证过的方案，避免重复造轮子。"), LifeRule("冲动决定先暂停", "重要决定先经过预设检查，不临时改写标准。"))
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("我的规则", style = MaterialTheme.typography.headlineLarge) }
        items(rules) { rule -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(rule.title, style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(6.dp)); Text(rule.detail) } } }
        item { Text("新增、编辑、版本记录和规则检查将在后续实现。") }
    }
}

@Composable
private fun ReviewScreen() {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("复盘", style = MaterialTheme.typography.headlineLarge)
        Text("复盘不是责备自己，而是检查系统。")
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) {
            Text("每周复盘", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(8.dp)); Text("• 哪些规则得到执行？\n• 哪些规则被违反？\n• 违反规则的原因是什么？\n• 下周只改进哪一件事？")
        } }
        Text("历史数据、执行率和规则修改记录待实现。")
    }
}