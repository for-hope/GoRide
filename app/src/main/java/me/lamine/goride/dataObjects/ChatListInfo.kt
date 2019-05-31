package me.lamine.goride.dataObjects

data class ChatListInfo(var userId:String = "") {
    var username:String = ""
    var isSender = false
    var lastMsg:String = ""
    var timestamp:Long = 0
    var isseen:Boolean = false
    lateinit var mUser:User
}