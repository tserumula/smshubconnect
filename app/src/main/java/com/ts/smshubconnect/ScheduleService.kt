package com.ts.smshubconnect

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import java.util.*


class ScheduleService : Service() {

    private lateinit var timerSend: Timer
    private lateinit var settingsManager: SettingsManager

    private var sendIntent = SMSSendIntent()
    private var deliverIntent = SMSSendIntent()
    private val pageTAG = "ScheduleService"

    private fun updateTimer() {
        settingsManager.updateSettings()

        Log.d(pageTAG, "Update timer")
        Log.d(pageTAG, settingsManager.isSendEnabled.toString())
        if (settingsManager.isSendEnabled) {
            startTimer()
        } else {
            cancelTimer()
        }
    }

    private fun cancelTimer() {
        Log.d(pageTAG , "Cancelling the timer")
        if (::timerSend.isInitialized) {
            timerSend.cancel()
        }
        timerSend = Timer("SendSMS", true)
    }

    private fun startTimer() {
        Log.d(pageTAG, "Start timer")
        if (::timerSend.isInitialized) {
            timerSend.cancel()
        }
        timerSend = Timer("SendSMS", true)
        if (settingsManager.isSendEnabled) {
            val seconds = settingsManager.interval * 60
            val interval = (seconds * 1000).toLong()

            Log.d(pageTAG, "Timer started at $interval")
            timerSend.schedule(SendTask(settingsManager, this), interval, interval)
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.d(pageTAG, "Service on destroy")

        if (::timerSend.isInitialized) {
            timerSend.cancel()
        }

        SleepLock.instance.getMyWakeLock()?.release()

        try {
            unregisterReceiver(sendIntent)
            unregisterReceiver(deliverIntent)
         //   unregisterReceiver(broadcastReceiver)
        } catch (e: IllegalArgumentException) {
            Log.d(pageTAG, "No receivers")
        }

        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d(pageTAG, "Service is started")

        settingsManager = SettingsManager(this.applicationContext)

        try {
            //registerReceiver(broadcastReceiver, IntentFilter(RECEIVED_SMS_FLAG))
            registerReceiver(sendIntent, IntentFilter(SENT_SMS_FLAG))
            registerReceiver(deliverIntent, IntentFilter(DELIVER_SMS_FLAG))

        } catch (e: IllegalArgumentException) {
            Log.d(pageTAG, "Already subscribed")
        }

        //initialize timer for the first time
        updateTimer()

        return super.onStartCommand(intent, flags, startId)
    }

}