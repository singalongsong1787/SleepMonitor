package com.morales.bnatest

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform


class AlarmAwakeReceiver : BroadcastReceiver() {
    //private var isAwake:Boolean = false
    override fun onReceive(context: Context?, intent: Intent?) {

        if (context == null || intent == null) {
            Log.e("AlarmService", "Context or Intent is null")
            return
        }

        Log.d("AlarmService", "onReceive called, action=${intent?.action}")
        Log.e("AlarmService", "Error log test")
    }

}