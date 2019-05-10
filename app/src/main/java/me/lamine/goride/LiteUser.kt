package me.lamine.goride

import java.io.Serializable

data class LiteUser(var userId:String,var name:String,var gender:String,var birthday:String,var ratings:Double,var peopleDriven:Int):Serializable {

}