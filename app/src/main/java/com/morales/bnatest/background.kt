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

    fun creat_testPrefs(){

        saveTimeAndIntentsityToPrefs("00:24:03",0.012073709635700933)
        saveTimeAndIntentsityToPrefs("00:31:07",0.07007782320969118)
        saveTimeAndIntentsityToPrefs("01:05:27",0.03983172478162306)
        saveTimeAndIntentsityToPrefs("04:51:44",0.15407832015650702)

    }


    private fun saveTimeAndIntentsityToPrefs( key: String, value: Any) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("StatusOfRollPrefs_test", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        when (value) {
            is String -> editor.putString(key, value)
            is Int -> editor.putInt(key, value)
            is Boolean -> editor.putBoolean(key, value)
            is Float -> editor.putFloat(key, value)
            is Long -> editor.putLong(key, value)
            is Double -> editor.putFloat(key, value.toFloat())
            else -> throw IllegalArgumentException("不支持的数据类型: ${value.javaClass.name}")
        }

        editor.apply()
        Log.d("RollDection","保存成功")
    }


}