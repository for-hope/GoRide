package me.lamine.goride.dataObjects
data class User(var userId:String = "", var email: String = "", var fullName:String="", var phoneNumber:String="", var birthday:String="", var gender:String="", var description:String="", var isDriver:Boolean=false) {

var profilePic:String = ""
//var activeTrips:MutableList<Trip> = mutableListOf()
    //TODO ADD GAS MAP
var peopleDriven:Int =0
var userRating:Double = 0.0
var userReviews:MutableList<String> = mutableListOf()



}