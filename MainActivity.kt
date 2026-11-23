package com.example.devicemonitor

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { SettingsScreen(this) } }
}


@Composable
private fun SettingsScreen(activity: MainActivity) {
    val prefs = activity.getSharedPreferences("widget_settings", 0)
    var model by remember { mutableStateOf(prefs.getBoolean("show_model", true)) }
    var soc by remember { mutableStateOf(prefs.getBoolean("show_soc", true)) }
    var cpu by remember { mutableStateOf(prefs.getBoolean("show_cpu", true)) }
    var ram by remember { mutableStateOf(prefs.getBoolean("show_ram", true)) }
    var battery by remember { mutableStateOf(prefs.getBoolean("show_battery", true)) }
    var batteryTemp by remember { mutableStateOf(prefs.getBoolean("show_battery_temp", true)) }
    var charge by remember { mutableStateOf(prefs.getBoolean("show_charge", true)) }
    var storage by remember { mutableStateOf(prefs.getBoolean("show_storage", true)) }
    var androidVer by remember { mutableStateOf(prefs.getBoolean("show_android", false)) }
    var cpuTemp by remember { mutableStateOf(prefs.getBoolean("show_cpu_temp", false)) }
    var bg by remember { mutableIntStateOf(prefs.getInt("bg", 0xFFE6F2E9.toInt())) }
    var fg by remember { mutableIntStateOf(prefs.getInt("fg", 0xFF343640.toInt())) }
    var opacity by remember { mutableFloatStateOf(prefs.getInt("opacity", 100).toFloat()) }
    val backgrounds = listOf(0xFFE6F2E9.toInt(),0xFFF5EBDD.toInt(),0xFFE7DDF7.toInt(),0xFFF8DDE3.toInt(),0xFFDCEBFA.toInt(),0xFF343744.toInt())
    val foregrounds = listOf(0xFF343640.toInt(),0xFF555761.toInt(),0xFF858791.toInt(),0xFFFFFFFF.toInt(),0xFF397D5A.toInt(),0xFF20242B.toInt())
    fun saveAndRefresh() {
        prefs.edit().putBoolean("show_model",model).putBoolean("show_soc",soc).putBoolean("show_cpu",cpu).putBoolean("show_ram",ram)
            .putBoolean("show_battery",battery).putBoolean("show_battery_temp",batteryTemp).putBoolean("show_charge",charge)
            .putBoolean("show_storage",storage).putBoolean("show_android",androidVer).putBoolean("show_cpu_temp",cpuTemp)
            .putInt("bg",bg).putInt("fg",fg).putInt("opacity",opacity.toInt()).putInt("accent",accent).putInt("interval",interval).putBoolean("monitoring",monitoring).apply()
        val mgr=AppWidgetManager.getInstance(activity); val ids=mgr.getAppWidgetIds(ComponentName(activity,MonitorWidgetProvider::class.java)); ids.forEach{MonitorWidgetProvider.updateOne(activity,mgr,it)}
    }
    MaterialTheme(colorScheme=lightColorScheme(primary=Color(0xFF39805D),background=Color(0xFFF7F7F3))) {
        Column(Modifier.fillMaxSize().background(Color(0xFFF7F7F3)).verticalScroll(rememberScrollState()).padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
            Text("Device Monitor",fontSize=27.sp,color=Color(0xFF343640)); Text("ウィジェットの表示とデザインを設定",color=Color(0xFF666A72))
            Card(shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=Color(bg))) { Column(Modifier.padding(16.dp)) {
                Text(if(model) android.os.Build.MODEL else "Device Monitor",color=Color(fg),fontSize=19.sp)
                if(soc) Text(android.os.Build.SOC_MODEL.ifBlank { android.os.Build.HARDWARE },color=Color(fg))
                Spacer(Modifier.height(8.dp)); Text("プレビュー  ·  選択した表示項目がウィジェットに反映されます",color=Color(fg))
            } }
            Card { Column(Modifier.padding(14.dp)) {
                Text("表示項目",style=MaterialTheme.typography.titleLarge,color=Color(0xFF39805D)); Text("ウィジェットに表示する情報を選択",color=Color.Gray)
                ToggleRow("機種名",model){model=it;saveAndRefresh()}; ToggleRow("SoC名",soc){soc=it;saveAndRefresh()}; ToggleRow("CPU使用率",cpu){cpu=it;saveAndRefresh()}; ToggleRow("RAM使用量",ram){ram=it;saveAndRefresh()}; ToggleRow("バッテリー残量",battery){battery=it;saveAndRefresh()}; ToggleRow("バッテリー温度",batteryTemp){batteryTemp=it;saveAndRefresh()}; ToggleRow("充電状態",charge){charge=it;saveAndRefresh()}; ToggleRow("ストレージ使用量",storage){storage=it;saveAndRefresh()}; ToggleRow("CPU温度（取得可能な端末のみ）",cpuTemp){cpuTemp=it;saveAndRefresh()}; ToggleRow("Androidバージョン",androidVer){androidVer=it;saveAndRefresh()}
            } }
            Card { Column(Modifier.padding(14.dp)) { Text("背景カラー",style=MaterialTheme.typography.titleMedium); ColorRow(backgrounds,bg){bg=it;saveAndRefresh()}; Spacer(Modifier.height(8.dp)); Text("文字カラー",style=MaterialTheme.typography.titleMedium); ColorRow(foregrounds,fg){fg=it;saveAndRefresh()}
                Text("アクセントカラー",style=MaterialTheme.typography.titleMedium); ColorRow(accents,accent){accent=it;saveAndRefresh()}
                Text("背景の透明度  ${opacity.toInt()}%",style=MaterialTheme.typography.titleMedium); Slider(value=opacity,onValueChange={opacity=it;saveAndRefresh()},valueRange=20f..100f)
            } }
            Card { Column(Modifier.padding(14.dp)) { Text("監視設定",style=MaterialTheme.typography.titleMedium); ToggleRow("バックグラウンド監視",monitoring){monitoring=it;saveAndRefresh(); val i=Intent(activity,MonitorService::class.java); if(it) { if(android.os.Build.VERSION.SDK_INT>=26) activity.startForegroundService(i) else activity.startService(i) } else activity.stopService(i) }; Text("更新間隔：${interval}秒",style=MaterialTheme.typography.bodyMedium); Slider(value=interval.toFloat(),onValueChange={interval=it.toInt().coerceIn(5,60);saveAndRefresh()},valueRange=5f..60f,steps=10); Text("短い間隔ほどバッテリー消費が増えます。ウィジェット更新はAndroid側の制限を受けます。",color=Color.Gray,fontSize=12.sp) } }
            Text("※ CPU温度など、一部情報は端末メーカーの仕様により取得できない場合があります。",color=Color.Gray,fontSize=13.sp)
            Text("ウィジェットをホーム画面に追加すると設定が反映されます。",color=Color.Gray,fontSize=13.sp)
        }
    }
}
@Composable private fun ToggleRow(label:String,checked:Boolean,onChange:(Boolean)->Unit){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment= androidx.compose.ui.Alignment.CenterVertically){Text(label,modifier=Modifier.weight(1f).padding(vertical=9.dp));Switch(checked=checked,onCheckedChange=onChange)} }
@Composable private fun ColorRow(colors:List<Int>,selected:Int,onPick:(Int)->Unit){Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.padding(vertical=8.dp)){colors.forEach{c->Surface(onClick={onPick(c)},modifier=Modifier.size(36.dp),shape=RoundedCornerShape(10.dp),color=Color(c),border=if(c==selected) BorderStroke(2.dp,Color(0xFF39805D)) else BorderStroke(1.dp,Color.LightGray)){} }}}
