package com.ts.smshubconnect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log.*


class SMSReceiver : BroadcastReceiver() {


    private var TAG = "SmsBroadcastReceiver"

    private lateinit var serviceProviderNumber: String
    private lateinit var serviceProviderSmsCondition: String

    private var listener: Listener? = null

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            var smsSender = ""
            var smsBody = ""
            for (smsMessage in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                smsSender = smsMessage.displayOriginatingAddress
                smsBody += smsMessage.messageBody
            }
            e(TAG, smsBody)
            val settingsManager = SettingsManager(context)

            PostReceivedMessage().execute(settingsManager.receiveURL, settingsManager.deviceId, smsBody, smsSender)


            val i = Intent("SMS_RECEIVED")
            // Data you need to pass to activity
            i.putExtra("number", smsSender)
            i.putExtra("message", smsBody)
            context.sendBroadcast(i)

            if (::serviceProviderNumber.isInitialized && smsSender == serviceProviderNumber && smsBody.startsWith(
                    serviceProviderSmsCondition
                )
            ) {
                if (listener != null) {
                    listener!!.onTextReceived(smsBody)
                }
            }
        }
    }


    internal interface Listener {
        fun onTextReceived(text: String)
    }
}