package com.ts.smshubconnect

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast

class SettingsManager(con: Context) {

    private var sharedPref: SharedPreferences

    var isSendEnabled: Boolean = false
    var interval: Int = 1
    var sendURL: String = ""
    var statusURL: String = ""
    var receiveURL: String = ""
    var deviceId: String = ""

    var context: Context = con

    init {
        sharedPref = context.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        this.updateSettings()
    }

    fun updateSettings(){

        val defaultSendEnabled = context.resources.getBoolean(R.bool.preference_default_send_enabled)
        val defaultInterval = context.resources.getInteger(R.integer.preference_default_interval)
        val defaultSendURL = context.resources.getString(R.string.preference_default_send_url)
        val defaultReceiveURL = context.resources.getString(R.string.preference_default_receive_url)
        val defaultStatusURL = context.resources.getString(R.string.preference_default_status_url)
        val defaultDeviceId = context.resources.getString(R.string.preference_default_device_id)

        this.isSendEnabled = sharedPref.getBoolean(context.getString(R.string.preference_send_enabled), defaultSendEnabled)
        this.interval = sharedPref.getInt(context.getString(R.string.preference_interval), defaultInterval)
        this.sendURL = sharedPref.getString(context.getString(R.string.preference_send_url), defaultSendURL)
        this.receiveURL = sharedPref.getString(context.getString(R.string.preference_receive_url), defaultReceiveURL)
        this.statusURL = sharedPref.getString(context.getString(R.string.preference_status_url), defaultStatusURL)
        this.deviceId = sharedPref.getString(context.getString(R.string.preference_device_id), defaultDeviceId)
    }

    fun setSettings( paramEnabled : Boolean, paramInterval : Int, paramURL : String, paramReceive: String, paramStatus: String, paramID: String){
        this.interval = paramInterval
        this.sendURL = paramURL
        this.receiveURL = paramReceive
        this.statusURL = paramStatus
        this.deviceId = paramID
        this.isSendEnabled = paramEnabled

        with(sharedPref.edit()) {
            putBoolean(context.getString(R.string.preference_send_enabled), paramEnabled)
            putString(context.getString(R.string.preference_send_url), paramURL )
            putString(context.getString(R.string.preference_receive_url), paramReceive)
            putString(context.getString(R.string.preference_status_url), paramStatus)
            putString(context.getString(R.string.preference_device_id), paramID )
            putInt(context.getString(R.string.preference_interval), paramInterval )
            apply()

            Log.d("SettingsManager", paramEnabled.toString())
            Toast.makeText(context, "Settings updated", Toast.LENGTH_SHORT).show()
        }

        this.updateSettings()
    }

}