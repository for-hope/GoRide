package me.lamine.goride.dataObjects


import android.util.Log
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class Trip( tripOrigin: String="",  tripDestination: String="",  allStops:ArrayList<String> = ArrayList(),  date:String="", luggageSizeInt:Int=0, time:String="", numberOfSeats:Int=0, price:Int=0, bookingPref:Int=0):Serializable {
     var origin:String = tripOrigin
     var destination:String = tripDestination
     var stops:ArrayList<String> = ArrayList()
     var bookedUsers:HashMap<String,Int> = hashMapOf()
     var date:String = date
     var time:String = time
     var vehicleModel:String = ""
      var vehicleType:String = ""
      var vehicleColor:String = ""
     var vehicleYear:Int = 0
     var licensePlate:String = ""
     var luggageSize:String = "N"
    @field:JvmField  var noSmoking:Boolean = false
    @field:JvmField  var petsAllowed = false
     var numberOfSeats:Int = 0
     var pricePerSeat:Int = 0
     var bookingPref = 0
    @field:JvmField  var hasVehicleInfo = false
     var description:String = ""
     var carPhoto:String = ""
     var userID:String = ""
      var originCity = ""
    var originSubCity = ""
     var originFullAddress = ""
     var destCity = ""
     var destSubCity = ""
     var destFullAddress = ""
     var tripID = ""
     var timestamp:Long = 0
     var poster: User? = null

    init {
        when (luggageSizeInt) {
            0 -> luggageSize = "N"
            1 -> luggageSize = "S"
            2 -> luggageSize = "M"
            3 -> luggageSize = "L"
        }
        this.numberOfSeats = numberOfSeats
        this.pricePerSeat = price
        this.bookingPref = bookingPref
        for (stop in allStops){
            stops.add(stop)
        }
        if (date !=""){
            val fullDate = "$date $time "
            val datetimeFormatter1 = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
            val lFromDate1 = datetimeFormatter1.parse(fullDate)
            timestamp = lFromDate1.time
        }



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
    fun addVehicleInfo(vehicleModel:String, vehicleType:String, vehicleColor:String, vehicleYear:Int,vehiclePlate:String, carPhoto:String) {
        hasVehicleInfo = true
        this.vehicleModel = vehicleModel
        this.vehicleType = vehicleType
        this.vehicleColor = vehicleColor
        this.vehicleYear = vehicleYear
        this.licensePlate = vehiclePlate
        this.carPhoto = carPhoto
    }
    fun addPreferences(noSmoking:Boolean, petsAllowed:Boolean){
        this.noSmoking = noSmoking
        this.petsAllowed = petsAllowed
    }
    fun addDescription(desc:String){
        this.description = desc
    }
    fun addUserID(id:String){
        this.userID = id
    }
    fun printAllInfo(){
        Log.i("ALL TRIP INFO (MAIN)",
            "ORIGIN : $origin  DESTINATION: $destination, DATE: $date, TIME: $time, NUMBER OF SEATS: $numberOfSeats," +
                    "Price per seat: $pricePerSeat, Luggage Size: $luggageSize, Booking Preference: $bookingPref")
        for (stop in stops) {
            Log.i("ALL STOPS",stop)
        }
        Log.i("ALL VEHICLE INFO","MODEL: $vehicleModel, TYPE: $vehicleType, Color: $vehicleColor, YEAR: $vehicleYear," +
                "PLATE: $licensePlate")
        Log.i("ALL PREFERENCES", "No Smoking: $noSmoking, Pets Allowed: $petsAllowed, description: $description")


    }

}