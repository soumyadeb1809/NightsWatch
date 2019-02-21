package com.highradius.nightswatch.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat

object Utility {

    fun isPermissionsGranted(activity: Activity): Boolean {
        return !(ActivityCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED)
    }

    fun askForPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS), 0)
    }
}
