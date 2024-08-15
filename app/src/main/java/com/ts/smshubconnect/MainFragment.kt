package com.ts.smshubconnect

import android.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.net.URL


open class MainFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var param3: String? = null
    private var param4: String? = null
    private var param5: String? = null

    private val pageTAG = "MAIN FRAGMENT"
    private var layoutTextView : TextView ? = null

    open fun updateUI( checked : Boolean, url : String ) {
        val view = layoutTextView
        val paramSendingStatus = if (checked){
            "ENABLED"
        }else{
            "OFF"
        }

        var paramHostURL = ""

        if( url.length > 5 && url.contains(".") ){
            paramHostURL = extractHostUrl(url)
        }

        if (view !== null ){
            param1 = paramSendingStatus
            param3 = paramHostURL
            view.text = resources.getString(
                R.string.app_status,
                param1,
                param2,
                param3,
                param4,
                param5
            )
        }

        Log.d(pageTAG , "UpdateUi")
    }

    // To extract Host URL from given URL
    private fun extractHostUrl(url: String): String {
        val uri = URL(url)
        return "${uri.protocol}://${uri.host}"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(APP_STATUS_SMS_KEY) //SMS Sending, Enabled // Disabled
            param2 = it.getString(APP_STATUS_KEY) //Background Service, Running // Not running
            param3 = it.getString(APP_SERVER_HOST_KEY) //Server-Host URL
            param4 = it.getString(APP_CONNECTION_KEY) //Connection, Online // Offline
            param5 = it.getString(APP_LAST_CONNECT_KEY)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        val textStatusView = view.findViewById<TextView>(R.id.textStatus)
        textStatusView.text = resources.getString(R.string.app_status, param1, param2, param3, param4, param5)

        layoutTextView = textStatusView

        return view
    }

    companion object {

        @JvmStatic
        fun newInstance(p1: String, p2: String, p3 : String, p4 : String, p5 : String ) =
            MainFragment().apply {
                arguments = Bundle().apply {
                    putString(APP_STATUS_SMS_KEY, p1)
                    putString(APP_STATUS_KEY, p2)
                    putString(APP_SERVER_HOST_KEY, p3)
                    putString(APP_CONNECTION_KEY, p4)
                    putString(APP_LAST_CONNECT_KEY, p5)
                }
            }
    }
}
