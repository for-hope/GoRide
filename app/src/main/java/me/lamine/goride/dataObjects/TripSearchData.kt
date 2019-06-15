package me.lamine.goride.dataObjects

import java.io.Serializable

data class TripSearchData(var originCode:Int, var destinationCode:Int, var originSubCity:String, var destSubCity:String):Serializable {
    var tripDate:String = ""
}