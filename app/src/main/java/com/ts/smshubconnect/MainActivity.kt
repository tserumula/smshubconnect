package com.ts.smshubconnect

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import java.net.URL


class MainActivity : AppCompatActivity() {

    private val pageTAG = "MAIN"

    // To check if background service is scheduled using Alarm Manager
    private fun isScheduleRunning(): Boolean {
        val intent = Intent(this, ScheduleReceiver::class.java)
        val isRunning =
            PendingIntent.getBroadcast(
                this,
                ALARM_REQUEST_CODE,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
                } else {
                    PendingIntent.FLAG_NO_CREATE
                }
            ) != null
        return isRunning
    }

    // To extract Host URL from given URL
    private fun extractHostUrl(url: String): String {
        val uri = URL(url)
        return "${uri.protocol}://${uri.host}"
    }

    private fun setSchedule() {
        /*
        * Starts background service using alarm-manager
        * The broadcast is received by ScheduleReceiver.kt which activates ScheduleService.kt
        * */

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ScheduleReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            ALARM_REQUEST_CODE,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            ALARM_INTERVAL_MILLIS,
            pendingIntent
        )

        Log.d(pageTAG, "Schedule activated")
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            //@Suppress("DEPRECATION")
            val activeNetworkInfo = connectivityManager.activeNetworkInfo ?: return false
            //@Suppress("DEPRECATION")
            return activeNetworkInfo.isConnected
        }
    }

    private fun requestSMSSendPermission() {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.SEND_SMS
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.SEND_SMS),
                    MY_PERMISSIONS_REQUEST_SEND_SMS
                )

            }
        } else {
            // Permission has already been granted
        }
    }

    /**
     * check SMS read permission
     */
    private fun smsPermissionIsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Request runtime SMS permission
     */
    private fun requestSMSReadPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)) {
            // You may display a non-blocking explanation here, read more in the documentation:
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECEIVE_SMS),
            MY_PERMISSIONS_REQUEST_SMS_RECEIVE
        )
    }

    // _____ PUT ALL OVERRIDE FUNCTIONS BELOW THIS LINE _____

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        requestSMSSendPermission()
        requestSMSReadPermission()


        val prefs = this.getSharedPreferences(resources.getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        val smsSendingEnabled = prefs.getBoolean( resources.getString(R.string.preference_send_enabled), false)
        val sendURL = prefs.getString( resources.getString(R.string.preference_send_url), "Null")

        val paramSendingStatus = if (smsSendingEnabled){
            "ENABLED"
        }else{
            "OFF"
        }

        val isRunning = isScheduleRunning()

        var paramServiceStatus = "Not Running"
        var paramConnection = if( isInternetAvailable(this)){
            "Online"
        }else{
            "Offline"
        }

        var paramHostURL = ""
        var paramLastConnect = ""

        if (!isRunning && smsPermissionIsGranted() && smsSendingEnabled ) {
            //start service
            setSchedule()
            paramServiceStatus = "Running"

            if(sendURL.length > 5 && sendURL.contains(".") ){
                paramHostURL = extractHostUrl(sendURL)
            }
        }

        val mainFragment = MainFragment()
        val args = Bundle()

        args.putString(APP_STATUS_SMS_KEY, paramSendingStatus)
        args.putString(APP_STATUS_KEY, paramServiceStatus)
        args.putString(APP_SERVER_HOST_KEY, paramHostURL)
        args.putString(APP_CONNECTION_KEY, paramConnection)
        args.putString(APP_LAST_CONNECT_KEY, paramLastConnect)

        mainFragment.arguments = args

        val transaction = fragmentManager.beginTransaction()

        transaction.replace(R.id.main_view, mainFragment, "MAIN")
        transaction.commit()
        fragmentManager.executePendingTransactions()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_SEND_SMS -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        return when (item.itemId) {
            R.id.action_settings -> {
                var settingsFragment = fragmentManager.findFragmentByTag("SETTINGS") as? SettingsFragment
                if (settingsFragment == null) {
                    settingsFragment = SettingsFragment()
                }

                val transaction = fragmentManager.beginTransaction()
                transaction.addToBackStack("MAIN")
                transaction.replace(R.id.main_view, settingsFragment, "SETTINGS")
                transaction.commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
