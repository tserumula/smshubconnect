package com.ts.smshubconnect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log

class ScheduleReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        Log.d("ScheduleReceiver", "Service broadcast received")
        val serviceIntent = Intent(context, ScheduleService::class.java)
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, javaClass.canonicalName)
        wakelock.acquire(WAKELOCK_MILLIS)
        SleepLock.instance.setMyWakeLock(wakelock)
        context.startService(serviceIntent)
    }
}