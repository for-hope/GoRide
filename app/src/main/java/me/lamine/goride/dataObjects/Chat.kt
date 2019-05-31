package me.lamine.goride.dataObjects

data class Chat(
    var sender: String = "",
    var receiver: String = "",
    var message: String = ""
) {
    var timestamp:Long = System.currentTimeMillis()
    var isseen:Boolean = false


}