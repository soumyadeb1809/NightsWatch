package com.highradius.nightswatch.ui

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import com.highradius.nightswatch.R
import com.highradius.nightswatch.constants.AppConstants

class MainActivity : AppCompatActivity() {

    private lateinit var etSenderPhone: EditText
    private lateinit var tvStatus: TextView
    private lateinit var btnAction: LinearLayout
    private lateinit var tvBtnActionText: TextView

    private var isReadingSms = false

    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setOnClickListeners()
    }

    private fun initViews() {
        etSenderPhone = findViewById(R.id.et_sender_phone)
        tvStatus = findViewById(R.id.txt_status)
        btnAction = findViewById(R.id.grp_action)
        tvBtnActionText = findViewById(R.id.tv_action_text)

        preferences = getSharedPreferences("sender_data", Context.MODE_PRIVATE)

        isReadingSms = preferences.getString("sender_phone", AppConstants.SMS.PHONE_EMPTY) != AppConstants.SMS.PHONE_EMPTY

        reloadStatus()

    }

    private fun reloadStatus() {
        if(!isReadingSms) {
            etSenderPhone.isEnabled = true
            btnAction.setBackgroundResource(R.drawable.back_blue_accent_rounded)
            tvBtnActionText.setText("Start Reading");
            tvStatus.setText("Enter the sender's number to read and reply OTPs")
        } else {
            etSenderPhone.isEnabled = false
            btnAction.setBackgroundResource(R.drawable.back_orange_accent_rounded)
            tvBtnActionText.setText("Stop Reading");
            tvStatus.setText("Reading and replying OTPs for sender")
            etSenderPhone.setText(preferences.getString("sender_phone", AppConstants.SMS.PHONE_EMPTY))
        }
    }

    private fun setOnClickListeners() {

        var preferencesEditor: SharedPreferences.Editor = preferences.edit()

        btnAction.setOnClickListener {
            if(!isReadingSms) {
                var senderPhone = etSenderPhone.text.toString()
                if (TextUtils.isEmpty(senderPhone) || null == senderPhone) {
                    Toast.makeText(this@MainActivity, "Please enter sender's phone number", Toast.LENGTH_SHORT).show()
                } else if (senderPhone.length < 10) {
                    Toast.makeText(this@MainActivity, "Please enter valid phone number", Toast.LENGTH_SHORT).show()
                } else {
                    preferencesEditor.putString("sender_phone", senderPhone)
                    preferencesEditor.commit()
                    AppConstants.SMS.SENDER_PHONE = senderPhone
                    isReadingSms = true
                    reloadStatus()
                }
            } else {
                preferencesEditor.putString("sender_phone", AppConstants.SMS.PHONE_EMPTY)
                preferencesEditor.commit()
                AppConstants.SMS.SENDER_PHONE = AppConstants.SMS.PHONE_EMPTY
                isReadingSms = false
                reloadStatus()
            }

            Log.i("SMSL", "Main -> Pref -> Sender Phone: " + preferences.getString("sender_phone", AppConstants.SMS.PHONE_EMPTY))
        }

    }
}
