package com.ts.smshubconnect

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.telephony.*
import android.util.Log
import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException
import khttp.post
import khttp.responses.Response
import java.net.URLDecoder
import java.util.*


class SMS(val message: String, val number: String, val messageId: String)

class SendTask(private var settings: SettingsManager, var context: Context) : TimerTask() {

    private var requestCode =  (0..10).random()
    private val pageTAG = "SendTask"

    private fun nextRequestCode(): Int {
        return ++requestCode
    }

    private fun getRadioSignalLevel(): Int {
        val telMgr = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(pageTAG , "NO Permission")
            return -1
        }

        if (telMgr != null) {
            return when (val info = telMgr.allCellInfo?.firstOrNull()) {
                is CellInfoLte -> info.cellSignalStrength.level
                is CellInfoGsm -> info.cellSignalStrength.level
                is CellInfoCdma -> info.cellSignalStrength.level
                is CellInfoWcdma -> info.cellSignalStrength.level
                else -> 0
            }
        }
        return -2
    }

    private fun parseRadioLevel(level : Int):String{
        return when (level) {
            CellSignalStrength.SIGNAL_STRENGTH_GOOD -> "STRENGTH_GOOD"
            CellSignalStrength.SIGNAL_STRENGTH_GREAT -> "STRENGTH_GREAT"
            CellSignalStrength.SIGNAL_STRENGTH_MODERATE -> "STRENGTH_MODERATE"
            CellSignalStrength.SIGNAL_STRENGTH_NONE_OR_UNKNOWN -> "STRENGTH_NONE_OR_UNKNOWN"
            CellSignalStrength.SIGNAL_STRENGTH_POOR -> "STRENGTH_POOR"
            else -> "Unsupported"
        }
    }

    override fun run() {
        lateinit var apiResponse: Response
        var lvl = 222

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Call battery manager service
            val bm = context.getSystemService(BATTERY_SERVICE) as BatteryManager
            // Get the battery percentage and store it in a INT variable
            val batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            lvl = batLevel
        }

        val radioSignalStrength = getRadioSignalLevel()
        val radioSignalString = parseRadioLevel(radioSignalStrength).toLowerCase(Locale.ROOT)

        try {
            apiResponse = post(
                url = settings.sendURL,
                data = mapOf(
                    "deviceId" to settings.deviceId,
                    "action" to "SEND",
                    "auth" to APP_TOKEN_KEY,
                    "level" to lvl.toString(),
                    "radio_strength" to radioSignalStrength.toString(),
                    "radio_string" to radioSignalString
                )
            )
        } catch (e: Exception) {
            Log.d(pageTAG, "Cannot connect to URL")
            return
        }

        Log.d(pageTAG, "Battery level is $lvl")
        Log.d(pageTAG, "Response : " + apiResponse.text )
        Log.d(pageTAG,"Radio signal Level: $radioSignalStrength, Radio Value : $radioSignalString" )

        var smsArray: List<SMS>? = emptyList()
        var canSend = false
        try {
            smsArray = Klaxon().parseArray(apiResponse.text)
            canSend = true
        } catch (e: KlaxonException) {
            if (apiResponse.text == "") {
                Log.d(pageTAG, "Nothing")
            } else {
                Log.e(pageTAG, "Error while parsing SMS" + apiResponse.text)
            }
        } finally {
            // optional finally block
        }
        if (canSend && !smsArray.isNullOrEmpty()) {

            val messageID = smsArray[0].messageId
            val msg = smsArray[0].message
            val number = smsArray[0].number

            val sentIn = Intent(SENT_SMS_FLAG)

            settings.updateSettings()

            sentIn.putExtra("messageId", messageID)
            sentIn.putExtra("statusURL", settings.statusURL)
            sentIn.putExtra("deviceId", settings.deviceId)
            sentIn.putExtra("delivered", 0)

            val sentPIn = PendingIntent.getBroadcast( context, nextRequestCode(), sentIn, 0)

            val deliverIn = Intent(DELIVER_SMS_FLAG)

            deliverIn.putExtra("messageId", messageID)
            deliverIn.putExtra("statusURL", settings.statusURL)
            deliverIn.putExtra("deviceId", settings.deviceId)
            deliverIn.putExtra("delivered", 1)


            val deliverPIn = PendingIntent.getBroadcast(
                context,
                nextRequestCode(),
                deliverIn,
                0
            )

            val smsManager = SmsManager.getDefault() as SmsManager
            val message = URLDecoder.decode(msg, "utf-8")
            smsManager.sendTextMessage(number, null, message, sentPIn, deliverPIn)

            Log.d(pageTAG, "Sent to: $number - id: $messageID - message: $message")
            //TODO: save the log message above to user storage?

            Thread.sleep(1000)
        }
    }

}
