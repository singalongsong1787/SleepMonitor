/**
 * functionn:用于封装一个功能项的图标和名称
 * @param val iconResId 表示功能项的图标资源的ID
 * @param val functionName 表示gone功能项名称
 * */

//注：data class是Kotlin中的特殊类

data class FunctionItem(val iconResId: Int, val functionName: String)