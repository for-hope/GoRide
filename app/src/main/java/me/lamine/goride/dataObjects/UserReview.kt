package me.lamine.goride.dataObjects

data class UserReview(val globalRating:Float) {
    var tRating:Float = 0.0f
    var sRating:Float = 0.0f
    var cRating:Float = 0.0f
    var review:String = ""
}