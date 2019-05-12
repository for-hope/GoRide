package me.lamine.goride

import java.sql.Timestamp

data class BookingNotification(var tripID:String, var pendingUsers:List<Pair<String,String>>) {
}