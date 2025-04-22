package com.morales.bnatest

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class background(private val context: Context) {
    private companion object {
        private const val PREF_NAME = "SleepDataPrefs"
    }

    fun updateSleepData(goToBedTime: String, wakeUpTime: String, snoringDuration: String){
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        //找到离得最近的日期
        val closestDateKey = findClosestDateKey(prefs)
        closestDateKey?.let {
            try {
                val jsonString = prefs.getString(it, "")
                Log.d("background","json为${jsonString}")
                val jsonObject = JSONObject(jsonString)

                // 更新上床时间、起床时间和鼾声持续时间
                jsonObject.put("startTime", goToBedTime)
                jsonObject.put("endTime", wakeUpTime)
                jsonObject.put("snoringDuration", snoringDuration)

                // 更新 SharedPreferences 中的数据
                editor.putString(it, jsonObject.toString())
                editor.apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }

    private fun findClosestDateKey(prefs: SharedPreferences): String? {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = Date()
        var closestDateKey: String? = null
        var minDiff: Long = Long.MAX_VALUE

        val allEntries = prefs.all
        for ((key, _) in allEntries) {
            try {
                val date = sdf.parse(key)
                date?.let {
                    val diff = Math.abs(it.time - currentDate.time)
                    if (diff < minDiff) {
                        minDiff = diff
                        closestDateKey = key
                    }
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        return closestDateKey
    }

}