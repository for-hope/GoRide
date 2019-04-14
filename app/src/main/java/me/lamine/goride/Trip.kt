package me.lamine.goride

import android.util.Log

class Trip(tripOrigin: String, tripDestination: String,allStops:ArrayList<String>,date:String , luggageSizeInt:Int,time:String,numberOfSeats:Int,price:Int,bookingPref:Int) {
    private var origin:String = tripOrigin
    private var destination:String = tripDestination
    private var stops:ArrayList<String> = ArrayList()
    private var numberOfStops = 0
    private var date:String = date
    private var time:String = time
    private  lateinit var vehicleModel:String
    private lateinit var vehicleType:String
    private lateinit var vehicleColor:String
    private var vehicleYear:Int = 0
    private lateinit var licensePlate:String
    private var luggageSize:String = "None"
    private var noSmoking:Boolean = false
    private var petsAllowed = false
    private var numberOfSeats:Int = 0
    private var pricePerSeat:Int = 0
    private var bookingPref = 0
    private lateinit var description:String


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

    fun addVehicleInfo(vehicleModel:String, vehicleType:String, vehicleColor:String, vehicleYear:Int,vehiclePlate:String) {
        this.vehicleModel = vehicleModel
        this.vehicleType = vehicleType
        this.vehicleColor = vehicleColor
        this.vehicleYear = vehicleYear
        this.licensePlate = vehiclePlate
    }
    fun addPrefrences(noSmoking:Boolean,petsAllowed:Boolean){
        this.noSmoking = noSmoking
        this.petsAllowed = petsAllowed
    }
    fun addDescription(desc:String){
        this.description = desc
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