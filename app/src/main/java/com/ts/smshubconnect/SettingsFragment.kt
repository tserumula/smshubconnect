package com.ts.smshubconnect

import android.os.Bundle
import android.app.Fragment;
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast


class SettingsFragment : Fragment() {

    protected lateinit var settingsManager: SettingsManager
    protected lateinit var mainActivity: MainActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsManager = SettingsManager(this.activity)
        mainActivity = this.activity as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        val btnSave: Button = view.findViewById(R.id.btnSave)
        val txtSendURL: EditText = view.findViewById(R.id.textSendURL)
        val txtStatusURL: EditText = view.findViewById(R.id.textStatusURL)
        val txtReceiveURL: EditText = view.findViewById(R.id.textReceiveURL)
        val txtInterval: EditText = view.findViewById(R.id.textInterval)
        val txtDeviceId: EditText = view.findViewById(R.id.textDeviceId)
        val switchIsEnabled: Switch = view.findViewById(R.id.switchIsEnabled)

        txtInterval.setText(settingsManager.interval.toString())
        switchIsEnabled.isChecked = settingsManager.isSendEnabled
        txtSendURL.setText(settingsManager.sendURL)
        txtReceiveURL.setText(settingsManager.receiveURL)
        txtStatusURL.setText(settingsManager.statusURL)
        txtDeviceId.setText(settingsManager.deviceId)

        //save
        btnSave.setOnClickListener {
            val sendURL = txtSendURL.text.toString()

            settingsManager.setSettings(
                switchIsEnabled.isChecked,
                txtInterval.text.toString().toInt(),
                sendURL,
                txtReceiveURL.text.toString(),
                txtStatusURL.text.toString(),
                txtDeviceId.text.toString()
            )
            val mainFragment = fragmentManager.findFragmentByTag("MAIN") as MainFragment

            mainFragment.updateUI( switchIsEnabled.isChecked, sendURL )

            val transaction = fragmentManager.beginTransaction()

            transaction.addToBackStack("SETTINGS")
            transaction.replace(R.id.main_view, mainFragment, "MAIN")
            transaction.commit()
            fragmentManager.executePendingTransactions()
            //mainActivity.updateTimer()
        }

        //save
        switchIsEnabled.setOnClickListener {
            var ok = true
            //if enabling first validate everything
            if (switchIsEnabled.isChecked) {
                if (txtInterval.text.toString() == "" ||
                    txtSendURL.text.toString() == "" ||
                    txtDeviceId.text.toString() == "" ||
                    txtStatusURL.text.toString() == "" ||
                    txtReceiveURL.text.toString() == ""
                ) {
                    switchIsEnabled.isChecked = false
                    Toast.makeText(activity, "Please complete all fields", Toast.LENGTH_LONG).show()
                    ok = false
                }

            }

            if (ok) {
                Log.d("SettingsFragment", switchIsEnabled.isChecked.toString())
                settingsManager.setSettings(
                    switchIsEnabled.isChecked,
                    txtInterval.text.toString().toInt(),
                    txtSendURL.text.toString(),
                    txtReceiveURL.text.toString(),
                    txtStatusURL.text.toString(),
                    txtDeviceId.text.toString()
                )
            }
            //mainActivity.updateTimer()
        }
        return view
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment settings.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString("abc", param1)
                    putString("def", param2)
                }
            }
    }
}
