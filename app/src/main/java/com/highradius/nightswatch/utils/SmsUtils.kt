package com.highradius.nightswatch.utils

import android.content.Context
import android.telephony.SmsManager
import android.widget.Toast

object SmsUtils {

    fun sendSMS(context: Context, phoneNo: String, msg: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNo, null, msg, null, null)
            Toast.makeText(context, "Message Sent",
                    Toast.LENGTH_LONG).show()
        } catch (ex: Exception) {
            Toast.makeText(context, ex.message.toString(),
                    Toast.LENGTH_LONG).show()
            ex.printStackTrace()
        }

    }

}
