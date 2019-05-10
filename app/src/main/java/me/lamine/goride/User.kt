package me.lamine.goride


import java.net.URL

data class User(var userId:String, var email: String, var fullName:String, var phoneNumber:String, var birthday:String, var gender:String, var description:String, var isDriver:Boolean) {
private var profilePicture:String = ""
var peopleDriven:Int =0
var userRating:Double = 0.0
var userReviews:MutableList<String> = mutableListOf()

 fun setProfilePic(profilePicURL:String)   {
    profilePicture = profilePicURL
}
 fun getProfilePic():String{
     return profilePicture
 }


}