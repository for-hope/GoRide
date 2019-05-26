package me.lamine.goride.dataObjects

data class StandaredNotification(var tripId:String){
    var username:String=""
    var profilePic:String=""
    var description:String =""
    var timestamp:String = ""
    var userId = ""
     var trip:Trip? = null
    var type = ""
    var otd = ""

}