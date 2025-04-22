package com.morales.bnatest
import android.os.Parcelable
import kotlinx.parcelize.Parcelize



@Parcelize

data class SleepData(
    val date: String,//日期
    val startTime: String,//开始时间
    val endTime: String,//结束时间
    val sleepDuration:String,//睡眠时间
    val snoreDataPath: String,//鼾声保存路径
    val endDate:String,//结束日期
    val snoringDuration:String //鼾声持续时间
): Parcelable

//我们也应该通过一些方式去储存这个类下的对象
//可以尝试使用JSON格式储存对象信息到文件中
