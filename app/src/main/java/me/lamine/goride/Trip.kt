package me.lamine.goride

import android.os.Parcelable
import android.util.Log
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

class Trip( tripOrigin: String,  tripDestination: String,  allStops:ArrayList<String>,  date:String, luggageSizeInt:Int, time:String, numberOfSeats:Int, price:Int, bookingPref:Int):Serializable {
     var origin:String = tripOrigin
     var destination:String = tripDestination
     var stops:ArrayList<String> = ArrayList()
     var date:String = date
     var time:String = time
     lateinit var vehicleModel:String
     lateinit var vehicleType:String
     lateinit var vehicleColor:String
     var vehicleYear:Int = 0
     lateinit var licensePlate:String
     var luggageSize:String = "None"
     var noSmoking:Boolean = false
     var petsAllowed = false
     var numberOfSeats:Int = 0
     var pricePerSeat:Int = 0
     var bookingPref = 0
     var hasVehicleInfo = false
     lateinit var description:String
     private lateinit var carPhoto:String
     lateinit var userID:String


    init {
        when (luggageSizeInt) {
            0 -> luggageSize = "None"
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