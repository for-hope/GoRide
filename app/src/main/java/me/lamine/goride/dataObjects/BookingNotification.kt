package me.lamine.goride.dataObjects


data class BookingNotification(var tripID:String, var pendingUsers:List<Pair<String,String>>) {
}