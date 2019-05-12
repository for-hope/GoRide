package me.lamine.goride

import java.sql.Timestamp

data class ExtendedBookingNotif(var origin:String, var dest:String,var date:String) {
    var username:String = ""
    var profilePic:String = ""
    var timestamp: String= ""
}