package com.example.devicemonitor

import android.app.*
import android.content.*
import android.os.*
import androidx.core.app.NotificationCompat

class MonitorService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val loop = object : Runnable { override fun run() { refreshWidgets(); val p=getSharedPreferences("widget_settings", MODE_PRIVATE); if(p.getBoolean("monitoring",false)) handler.postDelayed(this, p.getInt("interval",15).coerceIn(5,60)*1000L) else stopSelf() } }
    override fun onCreate() { super.onCreate(); val channel=NotificationChannel("device_monitor", "Device Monitor", NotificationManager.IMPORTANCE_LOW); getSystemService(NotificationManager::class.java).createNotificationChannel(channel); val n=NotificationCompat.Builder(this,"device_monitor").setSmallIcon(android.R.drawable.ic_menu_info_details).setContentTitle("Device Monitor").setContentText("端末情報を監視中").setOngoing(true).build(); startForeground(41,n) }
    override fun onStartCommand(intent: Intent?, flags:Int, startId:Int):Int { handler.removeCallbacks(loop); handler.post(loop); return START_STICKY }
    private fun refreshWidgets() { val mgr=AppWidgetManager.getInstance(this); val ids=mgr.getAppWidgetIds(ComponentName(this,MonitorWidgetProvider::class.java)); ids.forEach { MonitorWidgetProvider.updateOne(this,mgr,it) } }
    override fun onDestroy(){ handler.removeCallbacks(loop); super.onDestroy() }
    override fun onBind(intent:Intent?):IBinder?=null
}

class MonitorBootReceiver : BroadcastReceiver() { override fun onReceive(context:Context,intent:Intent) { if(intent.action==Intent.ACTION_BOOT_COMPLETED && context.getSharedPreferences("widget_settings",0).getBoolean("monitoring",false)) { val i=Intent(context,MonitorService::class.java); if(Build.VERSION.SDK_INT>=26) context.startForegroundService(i) else context.startService(i) } } }
