package com.highradius.nightswatch.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*

import com.highradius.nightswatch.R
import com.highradius.nightswatch.constants.AppConstants
import com.highradius.nightswatch.utils.Utility

class MainActivity : AppCompatActivity() {

    private lateinit var etSenderPhone: EditText
    private lateinit var tvStatus: TextView
    private lateinit var btnAction: LinearLayout
    private lateinit var tvBtnActionText: TextView
    private lateinit var imgRegexConfig: ImageView
    private lateinit var imgEmailLogs: ImageView

    private lateinit var regexConfigAlert: AlertDialog

    private var isReadingSms = false

    private lateinit var preferences: SharedPreferences
    private lateinit var preferencesEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = getSharedPreferences("sender_data", Context.MODE_PRIVATE)
        preferencesEditor = preferences.edit()

        initViews()
        setOnClickListeners()
    }

    private fun initViews() {
        etSenderPhone = findViewById(R.id.et_sender_phone)
        tvStatus = findViewById(R.id.txt_status)
        btnAction = findViewById(R.id.grp_action)
        tvBtnActionText = findViewById(R.id.tv_action_text)
        imgRegexConfig = findViewById(R.id.img_regex_config)
        imgEmailLogs = findViewById(R.id.img_email_logs)

        var alertBuilder = AlertDialog.Builder(this@MainActivity)
        var alertView: View = layoutInflater.inflate(R.layout.alert_regex_config, null)

        var etRegexPattern: EditText = alertView.findViewById(R.id.et_regex_config)
        var cbDetectAnyNum: CheckBox = alertView.findViewById(R.id.cb_detect_any_num)
        var btnSaveConfig: LinearLayout = alertView.findViewById(R.id.grp_save_config)
        var btnCancel: LinearLayout = alertView.findViewById(R.id.grp_cancel)

        var canDetectAnyNum: Boolean = preferences.getBoolean(AppConstants.SP.TAG_CAN_DETECT_ANY_NUM, true)
        cbDetectAnyNum.isChecked = canDetectAnyNum

        var regexConfig: String? = preferences.getString(AppConstants.SP.TAG_REGEX_PATTERN, "")
        etRegexPattern.setText(regexConfig)

        btnCancel.setOnClickListener { v: View ->
            regexConfigAlert.dismiss()
        }

        btnSaveConfig.setOnClickListener { v: View ->
            var regexPattern: String = etRegexPattern.text.toString()
            var canDetectAnyNum = cbDetectAnyNum.isChecked
            updateRegexConfig(regexPattern, canDetectAnyNum)
            regexConfigAlert.dismiss()
        }

        alertBuilder.setTitle("Regex Configuration")
        alertBuilder.setView(alertView)
        regexConfigAlert = alertBuilder.create()

        isReadingSms = preferences.getString("sender_phone", AppConstants.SMS.PHONE_EMPTY) != AppConstants.SMS.PHONE_EMPTY

        imgEmailLogs.setOnClickListener { sendEmail() }

        reloadStatus()

    }

    private fun sendEmail() {
        var logs: String = preferences.getString("logs", "Logs not found!")
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:") // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, "soumya.deb@highradius.com")
        intent.putExtra(Intent.EXTRA_SUBJECT,"LOGS: NightsWatch")
        intent.putExtra(Intent.EXTRA_TEXT, logs)
        //intent.type = "text/plain"

        startActivity(intent)
    }

    private fun updateRegexConfig(regexPattern: String, canDetectAnyNum: Boolean) {
        preferencesEditor.putString(AppConstants.SP.TAG_REGEX_PATTERN, regexPattern)
        preferencesEditor.putBoolean(AppConstants.SP.TAG_CAN_DETECT_ANY_NUM, canDetectAnyNum)
        preferencesEditor.commit()
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
            tvBtnActionText.setText("Stop Reading")
            tvStatus.setText("Reading and replying OTPs for sender")
            etSenderPhone.setText(preferences.getString("sender_phone", AppConstants.SMS.PHONE_EMPTY))
        }
    }

    private fun setOnClickListeners() {

        btnAction.setOnClickListener { v: View ->
            if(Utility.isPermissionsGranted(this@MainActivity)) {
                if (!isReadingSms) {
                    var senderPhone = etSenderPhone.text.toString()
                    if (TextUtils.isEmpty(senderPhone) || null == senderPhone) {
                        Toast.makeText(this@MainActivity, "Please enter sender's phone number", Toast.LENGTH_SHORT).show()
                    } else if (senderPhone.length < 1) {
                        Toast.makeText(this@MainActivity, "Please enter valid phone number", Toast.LENGTH_SHORT).show()
                    } else {
                        preferencesEditor.putString("sender_phone", senderPhone)
                        preferencesEditor.commit()
                        AppConstants.SMS.SENDER_PHONE = senderPhone
                        isReadingSms = true
                        reloadStatus()
                        Snackbar.make(v, "Crows have started their watch", Snackbar.LENGTH_SHORT).show()

                    }
                } else {
                    preferencesEditor.putString("sender_phone", AppConstants.SMS.PHONE_EMPTY)
                    preferencesEditor.commit()
                    AppConstants.SMS.SENDER_PHONE = AppConstants.SMS.PHONE_EMPTY
                    isReadingSms = false
                    reloadStatus()
                    Snackbar.make(v, "Crows watch has ended", Snackbar.LENGTH_SHORT).show()
                }
            }
            else {
                Utility.askForPermissions(this@MainActivity)
            }

            Log.i("SMSL", "Main -> Pref -> Sender Phone: " + preferences.getString("sender_phone", AppConstants.SMS.PHONE_EMPTY))
        }

        imgRegexConfig.setOnClickListener { v: View ->
            regexConfigAlert.show()
        }

    }

}
