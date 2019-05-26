package me.lamine.goride.dataObjects

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

data class TripRequest(var tripOrigin: String="",  var tripDestination: String="",var  date:String="",var luggageSizeInt:Int=0, var time:String="", var numberOfSeats:Int=0):Serializable {

    var description:String = ""
    var userID:String = ""
    var originCity = ""
    var originSubCity = ""
    var originFullAddress = ""
    var destCity = ""
    var destSubCity = ""
    var destFullAddress = ""
    var tripID = ""
    var timestamp:Long = 0
    var luggageSize:String = "N"
    var pendingDrivers:HashMap<String,Any> = hashMapOf()
    var poster: User? = null
    init {
        when (luggageSizeInt) {
            0 -> luggageSize = "N"
            1 -> luggageSize = "S"
            2 -> luggageSize = "M"
            3 -> luggageSize = "L"
        }
        if (date !=""){
            val fullDate = "$date $time "
            val datetimeFormatter1 = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
            val lFromDate1 = datetimeFormatter1.parse(fullDate)
            timestamp = lFromDate1.time
        }
    }
    fun addDescription(desc:String){
        this.description = desc
    }
    fun addUserID(id:String){
        this.userID = id
    }
    fun addTripDestinations(originCity:String, originSubCity:String,
                            originFullAddress:String,
                            destCity:String,
                            destSubCity:String,
                            destFullAddress:String)
    {
        this.originCity = originCity
        this.originSubCity = originSubCity
        this.originFullAddress = originFullAddress
        this.destCity = destCity
        this.destSubCity = destSubCity
        this.destFullAddress = destFullAddress
    }


}