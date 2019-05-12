package me.lamine.goride

import android.graphics.Paint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.activity_trip.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.annotation.NonNull
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.android.libraries.places.internal.e
import android.graphics.BitmapFactory
import com.google.android.gms.common.util.IOUtils.toByteArray
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentActivity
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_trip_results.*
import org.jetbrains.anko.*
import java.io.*
import java.net.URL
import java.sql.Timestamp


class TripActivity:AppCompatActivity() {
    private lateinit var mStorageRef: StorageReference
    private lateinit var database: DatabaseReference
    private val listOfTrips:MutableList<Trip> = mutableListOf()
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    private var userIdList:MutableList<String> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip)
        setSupportActionBar(trip_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);
        mStorageRef = FirebaseStorage.getInstance().reference;
        database = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        if (mAuth?.currentUser == null) {
            finish()
        }else {
            currentUser = mAuth?.currentUser
        }
        val trip  = intent.getSerializableExtra("ClickedTrip") as Trip
        val userLite = intent.getSerializableExtra("ClickedTripUser") as LiteUser
       setupView(trip, userLite)


    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    private fun setupView(trip: Trip,userLite:LiteUser){
        val name = userLite.name
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

        trip_ac_origin.text = trip.originSubCity
        trip_ac_des.text = trip.destSubCity
        val fod = "${trip.origin}, ${trip.originFullAddress}"
        trip__ac_desc1_card.text = fod
        val fdd = "${trip.destination}, ${trip.destFullAddress}"
        trip_ac_desc2.text = fdd
        //todo download car image

        if (trip.hasVehicleInfo){
            val vehiclePref = "${trip.vehicleType}, ${trip.vehicleColor} '${trip.vehicleYear}"
            trip_ac_car_model.text = trip.vehicleModel
            trip_ac_type_color_year.text = vehiclePref
            trip_ac_lisence.text = trip.licensePlate
            async { uiThread { downloadCarImage(trip.carPhoto) } }
        } else {
            trip_ac_car_model.text = ""
            trip_ac_type_color_year.text = ""
            trip_ac_lisence.text = ""
        }
        //show icon of booked users
        bookedUsers(trip)
       trip_ac_luggage.text = trip.luggageSize
        //todo change icon
        when {
            trip.luggageSize == "N" -> trip_ac_luggage_text.text = getString(R.string.no_luggage)
            trip.luggageSize == "S" -> trip_ac_luggage_text.text = getString(R.string.small_luggage)
            trip.luggageSize == "M" -> trip_ac_luggage_text.text = getString(R.string.medium_luggage)
            trip.luggageSize == "L" -> trip_ac_luggage_text.text = getString(R.string.large_luggage)
        }
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

       // val nbSeats = "${trip.numberOfSeats} seats left"

        val seatPrice = "${trip.pricePerSeat} DZD"
        trip_ac_price.text = seatPrice
        trip_ac_desc.text = trip.description
        if (trip.bookingPref == 0){
            val btn:MaterialButton = findViewById(R.id.trip_ac_submit_btn)
            trip_ac_submit_btn.text = getString(R.string.request_booking)
            btn.icon = null
        } else {
          trip_ac_submit_btn.text = getString(R.string.instant_booking)
        }
        trip_ac_submit_btn.setOnClickListener { submitRequest(trip) }
    }

    private  fun addUserView(trip: Trip,userId:String){
        Log.i("AddedUser", "YES")
        val layout = trip_users_layout
        val circleImageView = CircleImageView(this)

        Picasso.get().load(trip.carPhoto).into(circleImageView);
        circleImageView.setPadding(10,0,10,0)
        val params:LinearLayout.LayoutParams = layout.layoutParams as LinearLayout.LayoutParams
// Changes the height and width to the specified *pixels*
        params.height = matchParent
        params.width = wrapContent
       // params.weight = 0f
        //params.gravity = Gravity.START
        //circleImageView.layoutParams = params
        val layoutParams = LinearLayout.LayoutParams(100, wrapContent)
        circleImageView.layoutParams = layoutParams
        circleImageView.borderWidth = 2
        circleImageView.circleBackgroundColor = Color.parseColor("#FF000000")
        circleImageView.onClick { Toast.makeText(this,userId,Toast.LENGTH_LONG).show() }
        layout.addView(circleImageView)
    }
    private fun submitRequest(trip:Trip){
        if (trip.bookingPref == 1){
            Log.i("BookingPref",trip.bookingPref.toString())
            saveToDB(trip)
        } else {
            Log.i("BookingPref2",trip.bookingPref.toString())
            savePendingToDB(trip)
        }



    }
    private fun setPb(visibility: Int){

        if (visibility == 1) {
            pb_search_act.visibility = View.VISIBLE
            grey_sact_layout.visibility = View.VISIBLE

        } else {
           pb_search_act.visibility = View.GONE
            grey_sact_layout.visibility = View.GONE

        }
    }
    private fun setupBookedCount(trip: Trip, mCount:Int){
        val nbSeats = "${trip.numberOfSeats - mCount} Seats Left"
        trip_ac_seats.text = nbSeats
    }
    private fun bookedUsers(trip: Trip){
        var mCount = 0

        countBookedPeople(trip,object : OnGetDataListener {
            override fun onStart() {
                setPb(1)
            }

            override fun onSuccess(data: DataSnapshot) {
                //DO SOME THING WHEN GET DATA SUCCESS HERE

               mCount = data.childrenCount.toInt()
                for (child in data.children){
                    userIdList.add(child.key!!)
                    addUserView(trip,child.key!!)
                }
                setupBookedCount(trip,mCount)
                setPb(0)

            }

            override fun onFailed(databaseError: DatabaseError) {
             Toast.makeText(this@TripActivity,"Failed to deliver data. Try Again.",Toast.LENGTH_LONG).show()
                this@TripActivity.finish()
            }
        })

    }
    private fun countBookedPeople(trip: Trip, listener: OnGetDataListener){
        listener.onStart()
        val childName = trip.tripID
        val destCode = decodeWilaya(trip.destCity)
        val originCode = decodeWilaya(trip.originCity)
        val newRef = database.child("trips").child("${originCode}_$destCode").child(childName).child("bookedUsers")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listener.onSuccess(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException() // don't ignore errors
            }
        }
        newRef.addListenerForSingleValueEvent(eventListener)
    }
    private fun saveToDB(trip:Trip){
        val childName = trip.tripID
        Log.i("FireBase", trip.tripID)
        val destCode = decodeWilaya(trip.destCity)
        val originCode = decodeWilaya(trip.originCity)
        val newRef = database.child("trips").child("${originCode}_$destCode").child(childName)

        newRef.child("bookedUsers").child(currentUser?.uid!!).setValue(1){ databaseError, _ ->
            if (databaseError != null) {
                Log.i("FireBaseEroor",databaseError.message)
                Toast.makeText(this, "Error $databaseError", Toast.LENGTH_LONG).show()}
        }
        database.push()
        bookedUsers(trip)
    }
    private fun savePendingToDB(trip:Trip){
        val childName = trip.tripID
        Log.i("savePendingToDB", trip.tripID)
        val destCode = decodeWilaya(trip.destCity)
        val originCode = decodeWilaya(trip.originCity)
        var newRef = database.child("trips").child("${originCode}_$destCode").child(childName)

        newRef.child("pendingBookedUsers").child(currentUser?.uid!!).setValue(0){ databaseError, _ ->
            if (databaseError != null) {
                Log.i("FireBaseEroor",databaseError.message)
                Toast.makeText(this, "Error $databaseError", Toast.LENGTH_LONG).show()}
        }
        newRef = database.child("users").child(trip.userID).child("tripRequests").child(trip.tripID)
        newRef.child(currentUser?.uid!!).setValue(Timestamp(System.currentTimeMillis()).toString())
        database.push()
        bookedUsers(trip)
    }
    private fun downloadCarImage(carPhotoURL:String){
        Picasso.get().load(carPhotoURL).into(trip_ac_car_photo);
    }

}