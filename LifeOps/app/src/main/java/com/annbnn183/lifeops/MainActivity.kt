package com.annbnn183.lifeops

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class ActionItem(val id: String, val title: String)
private data class RuleItem(val id: String, val title: String, val detail: String)
private data class LogItem(val date: String, val text: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { LifeOpsApp(this) } }
}
private fun dayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
private fun prefs(c: Context) = c.getSharedPreferences("lifeops", Context.MODE_PRIVATE)
private fun loadActions(c: Context): List<ActionItem> { return try { val j=JSONArray(prefs(c).getString("actions","[]")); (0 until j.length()).map { ActionItem(j.getJSONObject(it).getString("id"),j.getJSONObject(it).getString("title")) } } catch(_:Exception){ emptyList() } }
private fun saveActions(c:Context,items:List<ActionItem>) { val j=JSONArray();items.forEach { j.put(JSONObject().put("id",it.id).put("title",it.title)) };prefs(c).edit().putString("actions",j.toString()).apply() }
private fun loadDone(c:Context):Set<String> { val j=JSONArray(prefs(c).getString("done_"+dayKey(),"[]"));return (0 until j.length()).map { j.getString(it) }.toSet() }
private fun saveDone(c:Context,s:Set<String>){val j=JSONArray();s.forEach{j.put(it)};prefs(c).edit().putString("done_"+dayKey(),j.toString()).apply()}
private fun loadRules(c:Context):List<RuleItem>{val raw=prefs(c).getString("rules","");if(raw.isNullOrBlank()) return listOf(RuleItem("r1","先行动，再优化","研究不能无限延长，达到足够信息就小步验证。"),RuleItem("r2","固定复盘日修改规则","平时执行规则，修改时记录理由。"),RuleItem("r3","先寻找成熟方案","优先复用经过验证的实现。"),RuleItem("r4","冲动决定先暂停","重要决定先通过预设检查。"));return try{val j=JSONArray(raw);(0 until j.length()).map{val o=j.getJSONObject(it);RuleItem(o.getString("id"),o.getString("title"),o.getString("detail"))}}catch(_:Exception){emptyList()}}
private fun saveRules(c:Context,r:List<RuleItem>){val j=JSONArray();r.forEach{j.put(JSONObject().put("id",it.id).put("title",it.title).put("detail",it.detail))};prefs(c).edit().putString("rules",j.toString()).apply()}
private fun addLog(c:Context,msg:String){val j=JSONArray(prefs(c).getString("log","[]"));j.put(JSONObject().put("date",dayKey()).put("text",msg));prefs(c).edit().putString("log",j.toString()).apply()}
private fun logs(c:Context):List<LogItem>{return try{val j=JSONArray(prefs(c).getString("log","[]"));(0 until j.length()).map{LogItem(j.getJSONObject(it).getString("date"),j.getJSONObject(it).getString("text"))}.reversed()}catch(_:Exception){emptyList()}}
private fun usagePermission(c:Context):Boolean { val appOps=c.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager;return appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(),c.packageName)==AppOpsManager.MODE_ALLOWED }
private fun usageMinutes(c:Context):Long { if(!usagePermission(c))return -1; val us=c.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager;val start=SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).parse(dayKey())?.time ?: return 0;return us.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,start,System.currentTimeMillis()).sumOf{it.totalTimeInForeground}/60000 }

@Composable
private fun LifeOpsApp(context:Context) {
    var tab by remember{mutableIntStateOf(0)}
    var actions by remember{mutableStateOf(loadActions(context))}
    var done by remember{mutableStateOf(loadDone(context))}
    var rules by remember{mutableStateOf(loadRules(context))}
    var logItems by remember{mutableStateOf(logs(context))}
    var newAction by remember{mutableStateOf("")}
    var usage by remember{mutableLongStateOf(usageMinutes(context))}
    MaterialTheme { Scaffold(bottomBar={NavigationBar{NavigationBarItem(tab==0,{tab=0},icon={},label={Text("今天")});NavigationBarItem(tab==1,{tab=1},icon={},label={Text("规则")});NavigationBarItem(tab==2,{tab=2},icon={},label={Text("记录")})}}){pad->
        Box(Modifier.padding(pad).fillMaxSize()){when(tab){
        0->LazyColumn(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
            item{Text("LifeOps",style=MaterialTheme.typography.headlineLarge);Text(dayKey());Text("完成 ${done.size}/${actions.size}")}
            item{Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text("手机使用自动记录",style=MaterialTheme.typography.titleMedium);Text(if(usage>=0)"今日前台使用约 $usage 分钟" else "需要使用情况访问权限");Button(onClick={if(!usagePermission(context))context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) else usage=usageMinutes(context)}){Text(if(usage>=0)"刷新统计" else "授权并开启自动统计")}}}}
            item{OutlinedTextField(newAction,{newAction=it},label={Text("添加今日行动")},modifier=Modifier.fillMaxWidth());Button(onClick={if(newAction.trim().isNotEmpty()){val item=ActionItem(System.currentTimeMillis().toString(),newAction.trim());actions=actions+item;saveActions(context,actions);addLog(context,"添加行动：${item.title}");logItems=logs(context);newAction=""}}){Text("添加")}}
            items(actions){item->Card(Modifier.fillMaxWidth()){Row(Modifier.padding(12.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(item.title,Modifier.weight(1f));Checkbox(done.contains(item.id),{v->done=if(v)done+item.id else done-item.id;saveDone(context,done);addLog(context,(if(v)"完成" else "取消完成")+"：${item.title}");logItems=logs(context)})}}}
            item{Text("完成勾选会自动保存并记录时间。",style=MaterialTheme.typography.bodySmall)}
        }
        1->LazyColumn(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
            item{Text("规则",style=MaterialTheme.typography.headlineLarge)}
            items(rules){r->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text(r.title,style=MaterialTheme.typography.titleMedium);Text(r.detail)}}}
            item{Button(onClick={val r=RuleItem(System.currentTimeMillis().toString(),"新规则","请编辑此规则内容。");rules=rules+r;saveRules(context,rules);addLog(context,"创建规则：${r.title}");logItems=logs(context)}){Text("新增规则")}}
            item{Text("规则列表已保存在本机。编辑功能将后续完善。")}
        }
        else->LazyColumn(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
            item{Text("执行记录",style=MaterialTheme.typography.headlineLarge);Text("自动记录行动新增、完成/取消及规则创建。")}
            items(logItems){l->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){Text(l.date,style=MaterialTheme.typography.labelMedium);Text(l.text)}}}
            item{Button(onClick={addLog(context,"用户手动记录复盘");logItems=logs(context)}){Text("记录一次复盘")}}
        }
        }}
    }}
}
