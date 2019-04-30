package me.lamine.goride

import android.graphics.Paint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.activity_trip.*
import java.text.SimpleDateFormat
import java.util.*

class TripActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip)
        setSupportActionBar(trip_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);

        val trip  = intent.getSerializableExtra("ClickedTrip") as Trip
       setupView(trip)


    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupView(trip: Trip){
        val name = "Lamine Fet"
        trip_ac_user_name.text = name
        val OLD_FORMAT = "dd/MM/yyyy"
        val NEW_FORMAT = "EEE, MMM dd"

        val newDate: String

        val sdf = SimpleDateFormat(OLD_FORMAT, Locale.US)
        val d = sdf.parse(trip.date)
        sdf.applyPattern(NEW_FORMAT)
        newDate = sdf.format(d)

        val fullDate = "$newDate at ${trip.time}"
       trip_ac_date.text = fullDate

        trip_ac_origin.text = trip.origin
        trip_ac_des.text = trip.destination

        if (trip.hasVehicleInfo){
            val vehiclePref = "${trip.vehicleType}, ${trip.vehicleColor} '${trip.vehicleYear}"
            trip_ac_car_model.text = trip.vehicleModel
            trip_ac_type_color_year.text = vehiclePref
            trip_ac_lisence.text = trip.licensePlate
        }
        //todo add none luggage
       trip_ac_luggage.text = trip.luggageSize
        if (trip.noSmoking){
            trip_ac_smokeprf.text = getString(R.string.smoking_allowed)
            trip_ac_smokeprf.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            trip_ac_smokeprf.text = getString(R.string.smoking_allowed)
            //todo change icon
        }
        if (trip.petsAllowed) {
           trip_ac_pets_pref.text = getString(R.string.pets_allowed)
        } else {
            trip_ac_pets_pref.text = getString(R.string.pets_allowed)
            trip_ac_pets_pref.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        }
        val nbSeats = "${trip.numberOfSeats} seats left"
        trip_ac_seats.text = nbSeats
        val seatPrice = "${trip.pricePerSeat} DZD"
        trip_ac_price.text = seatPrice
        if (trip.bookingPref == 0){
            val btn:MaterialButton = findViewById(R.id.trip_ac_submit_btn)
            trip_ac_submit_btn.text = "Request Booking"
            btn.icon = null
        } else {
          trip_ac_submit_btn.text = "Instant Booking"


        }
    }

}