package com.morales.bnatest

data class data_AlarmConfig (
    var isOpen:Boolean,
    var timePicker_h:Int,
    var timePicker_minute:Int,
    var weekdays:Set<String>,
    var isVibration:Boolean,
    var bell:Int,
    var volume:Int,
    var isAwake:Boolean,
    var awakeTime:String
)