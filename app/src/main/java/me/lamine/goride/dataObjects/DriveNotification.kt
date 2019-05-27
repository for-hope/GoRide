package me.lamine.goride.dataObjects

data class DriveNotification(var tripId:String = "", var userId:String ="",var otd:String = "", var date:String= "",var timestamp:String = "") {
    lateinit var userObject:User
}