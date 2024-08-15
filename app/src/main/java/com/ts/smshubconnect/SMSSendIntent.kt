package com.ts.smshubconnect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.Activity
import android.util.Log
import khttp.responses.Response
import org.jetbrains.anko.doAsync


class SMSSendIntent : BroadcastReceiver() {
    private val pageTAG = "SMSSendIntent"

    override fun onReceive(context: Context?, intent: Intent?) {
        val status: String

        val delivered = intent!!.getIntExtra("delivered", 0)
        status = if (delivered == 1) {
            "DELIVERED"
        } else {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    "SENT"
                }

                else -> {
                    "FAILED"
                }
            }

        }

        val statusUrl = intent.getStringExtra("statusURL")
        val deviceId = intent.getStringExtra("deviceId")
        val messageId = intent.getStringExtra("messageId")

        Log.d(pageTAG , "async->$messageId-$status-sucker$deviceId")

        doAsync {
            lateinit var res: Response
            try {
                Log.d(pageTAG, "Post status to $statusUrl")
                res = khttp.post(
                    url = statusUrl,
                    data = mapOf(
                        "deviceId" to deviceId,
                        "messageId" to messageId,
                        "status" to status,
                        "action" to "STATUS_UPDATE",
                        "auth" to APP_TOKEN_KEY
                    )
                )
                Log.d(pageTAG, res.text)
            } catch (e: java.net.ConnectException) {
                Log.d(pageTAG, "Cannot connect to URL")
            }
        }
    }
}