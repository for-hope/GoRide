package me.lamine.goride

import java.io.Serializable
import java.sql.Timestamp

data class LiteUser(var userId:String,var name:String,var gender:String,var birthday:String,var ratings:Double,var peopleDriven:Int):Serializable {
var profilePic = ""
    var pendingTS:String = ""
}