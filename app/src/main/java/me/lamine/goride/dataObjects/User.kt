package me.lamine.goride.dataObjects

import java.io.Serializable

data class User(var userId:String = "", var email: String = "", var fullName:String="", var phoneNumber:String="", var birthday:String="", var gender:String="", var description:String="", var isDriver:Boolean=false):Serializable {

var profilePic:String = "https://firebasestorage.googleapis.com/v0/b/ridego-1555252117345.appspot.com/o/user_images%2Fmale_default.png?alt=media&token=658a01b5-ba18-4491-8aa7-e15c30284200"
//var activeTrips:MutableList<Trip> = mutableListOf()
    //TODO ADD GAS MAP
var peopleDriven:Int =0
var userRating:Double = 0.0
 var userReviews:HashMap<String,Any> = hashMapOf()

var tripsTraveled = 0
var accountCreatingDate = "May 2019"
var phoneVerfication = true
var emailVerification = true
var tripsAsPassenger = 0
var userRatingCount = 0
var rawRating = 0

 var driversGoneWith:HashMap<String,Int> = hashMapOf()
var tRating = 0f
var tRatingCount = 0
var rawTRating = 0

var sRating = 0f
var sRatingCount= 0
var rawSRating = 0

var cRating = 0f
 var cRatingCount= 0
var rawCRating = 0





}