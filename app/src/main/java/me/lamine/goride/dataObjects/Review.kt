package me.lamine.goride.dataObjects

data class Review(var userId:String="") {
    var username:String = ""
    var reviewerType: String = ""
    var reviewDate: String = ""
    var reviewOtd: String = ""
    var reviewDesc: String = ""
    var reviewUser:User? = null
    var reviwerPfp:String = ""
}