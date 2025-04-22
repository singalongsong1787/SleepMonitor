package com.morales.bnatest

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class wakeUpAndDeepSleep(
    val start_end:List<List<String>>,
    val wakeup_interval:List<List<String>>,  //有问题，应该是一个列表
    val deepsleep_interval:List<List<String>>
)