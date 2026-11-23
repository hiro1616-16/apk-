package com.example.devicemonitor

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.app.ActivityManager
import android.os.Build
import android.widget.RemoteViews
import java.io.File

class MonitorWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) { ids.forEach { updateOne(context, manager, it) } }
    companion object {
        fun updateOne(context: Context, manager: AppWidgetManager, id: Int) {
            val p=context.getSharedPreferences("widget_settings",Context.MODE_PRIVATE)
            val views=RemoteViews(context.packageName,R.layout.widget_monitor)
            val model=Build.MODEL.ifBlank { "Android device" }
            val soc=Build.SOC_MODEL.takeIf { it.isNotBlank() } ?: Build.HARDWARE.takeIf { it.isNotBlank() } ?: Build.BOARD
            val bm=context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val battery=bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).coerceIn(0,100)
            val am=context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val mi=ActivityManager.MemoryInfo(); am.getMemoryInfo(mi)
            val ramTotal=mi.totalMem/(1024*1024); val ramUsed=(mi.totalMem-mi.availMem)/(1024*1024)
            val cpu=cpuPercent(context)
            val data=File("/data"); val total=data.totalSpace; val used=total-data.freeSpace
            val status=context.registerReceiver(null,android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val temp=status?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,-1) ?: -1
            val isCharging=status?.getIntExtra(BatteryManager.EXTRA_STATUS,-1) in listOf(BatteryManager.BATTERY_STATUS_CHARGING,BatteryManager.BATTERY_STATUS_FULL)
            val showModel=p.getBoolean("show_model",true); val showSoc=p.getBoolean("show_soc",true); val showCpu=p.getBoolean("show_cpu",true)
            val showRam=p.getBoolean("show_ram",true); val showBattery=p.getBoolean("show_battery",true); val showBatteryTemp=p.getBoolean("show_battery_temp",true)
            val showCharge=p.getBoolean("show_charge",true); val showStorage=p.getBoolean("show_storage",true); val showAndroid=p.getBoolean("show_android",false); val showCpuTemp=p.getBoolean("show_cpu_temp",false)
            val title=if(showModel) model else "Device Monitor"
            val socLine=if(showSoc) soc else ""
            val statParts=mutableListOf<String>(); if(showCpu) statParts.add("CPU ${cpu}%"); if(showRam) statParts.add("RAM ${ramUsed}/${ramTotal}MB")
            if(showBattery) statParts.add("Battery ${battery}%${if(showBatteryTemp && temp>0) " · ${temp/10}°C" else ""}")
            if(showCharge) statParts.add(if(isCharging) "Charging" else "Not charging")
            val storageLine=if(showStorage) "Storage ${format(used)} / ${format(total)}" else ""
            val extra=mutableListOf<String>(); if(showCpuTemp) cpuTempC()?.let { extra.add("CPU temp ${it}°C") }; if(showAndroid) extra.add("Android ${Build.VERSION.RELEASE}")
            views.setTextViewText(R.id.widgetModel,title); views.setTextViewText(R.id.widgetSoc,socLine); views.setTextViewText(R.id.widgetStats,statParts.joinToString("  ·  ")); views.setTextViewText(R.id.widgetStorage,(listOf(storageLine)+extra).filter{it.isNotBlank()}.joinToString("  ·  "))
            val base=p.getInt("bg",0xFFE6F2E9.toInt()); val alpha=(p.getInt("opacity",100).coerceIn(20,100)*255/100); val bg=(base and 0x00FFFFFF) or (alpha shl 24); val fg=p.getInt("fg",0xFF343640.toInt()); val accent=p.getInt("accent",0xFF39805D.toInt())
            views.setInt(R.id.widgetRoot,"setBackgroundColor",bg); views.setInt(R.id.accentStrip,"setBackgroundColor",accent); views.setTextColor(R.id.widgetModel,fg); views.setTextColor(R.id.widgetSoc,fg); views.setTextColor(R.id.widgetStats,fg); views.setTextColor(R.id.widgetStorage,fg)
            views.setOnClickPendingIntent(R.id.widgetRoot,android.app.PendingIntent.getActivity(context,0,Intent(context,MainActivity::class.java),android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE))
            manager.updateAppWidget(id,views)
        }
        private fun cpuPercent(context:Context):Int=try{
            val row=File("/proc/stat").readLines().first().trim().split(Regex("\\s+")); val v=row.drop(1).map{it.toLong()}; val idle=v.getOrElse(3){0L}+v.getOrElse(4){0L}; val total=v.sum()
            val p=context.getSharedPreferences("cpu_sample",Context.MODE_PRIVATE); val oldTotal=p.getLong("total",0L); val oldIdle=p.getLong("idle",0L); p.edit().putLong("total",total).putLong("idle",idle).apply()
            if(oldTotal==0L || total<=oldTotal) 0 else (((total-oldTotal)-(idle-oldIdle))*100/(total-oldTotal)).toInt().coerceIn(0,100)
        }catch(_:Exception){0}
        private fun cpuTempC():Int? { val paths=listOf("/sys/class/thermal/thermal_zone0/temp","/sys/class/thermal/thermal_zone1/temp"); for(path in paths) try { val n=File(path).readText().trim().toInt(); if(n>0) return if(n>1000)n/1000 else n }catch(_:Exception){}; return null }
        private fun format(bytes:Long):String=if(bytes>=1024L*1024*1024) "${bytes/(1024L*1024*1024)}GB" else "${bytes/(1024*1024)}MB"
    }
}
