package me.lamine.goride.tripActivity

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.activity_trip.*
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import android.graphics.Color
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.stfalcon.imageviewer.StfalconImageViewer
import de.hdodenhof.circleimageview.CircleImageView
import me.lamine.goride.R
import me.lamine.goride.R.drawable.ic_mtrl_chip_checked_circle
import me.lamine.goride.dataObjects.Trip
import me.lamine.goride.dataObjects.User
import me.lamine.goride.interfaces.OnGetDataListener
import me.lamine.goride.userActivity.UserActivity
import me.lamine.goride.utils.decodeWilaya
import org.jetbrains.anko.*
import java.sql.Timestamp
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_trip.trip_ac_submit_btn
import me.lamine.goride.dataObjects.TripRequest
import me.lamine.goride.postingActivity.PostOptionsActivity
import me.lamine.goride.postingActivity.PostingActivity
import me.lamine.goride.requestActivity.RequestTripActivity
import me.lamine.goride.utils.wilayaArrayEN
import kotlin.collections.HashMap


class TripActivity:AppCompatActivity() {
    private lateinit var mStorageRef: StorageReference
    private lateinit var database: DatabaseReference
    private val pendingUsersList: MutableList<String> = mutableListOf()
   // private val listOfTrips:MutableList<Trip> = mutableListOf()
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    private var userIdList:MutableList<String> = mutableListOf()
    private var seatsLeft = -1
    private lateinit var trip:Trip
    private var menu:Menu?=null
    private var menuType = 0
    private var isPendingUser = false
    private lateinit var userLite:User
    private lateinit var tripR:TripRequest
    private var isRequest = false
    private var pendingUsers:HashMap<String,Any> = hashMapOf()
    private var bookedUsersList:MutableList<User> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip)
        setSupportActionBar(trip_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        mStorageRef = FirebaseStorage.getInstance().reference
        database = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        if (mAuth?.currentUser == null) {
            finish()
        }else {
            currentUser = mAuth?.currentUser
        }
        profile_card_view.setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            intent.putExtra("UserProfile",userLite)
            startActivity(intent)
        }
         isRequest = intent.getBooleanExtra("isRequest",false)
        if (!isRequest){
            trip  = intent.getSerializableExtra("ClickedTrip") as Trip
            userLite = intent.getSerializableExtra("ClickedTripUser") as User

            if (trip.bookingPref != 1) {
                pendingUsersListener()
            }
            setupView(trip, userLite)
            if (trip.userID == currentUser?.uid!!){
                Log.i("VIEW","GONE HERE 1")
                trip_ac_submit_btn.visibility = View.GONE
                showModifyTrip()
            }
        } else {
            trip_toolbar.title = "Trip Request"
            tripR= intent.getSerializableExtra("ClickedTrip") as TripRequest
            userLite = intent.getSerializableExtra("ClickedTripUser") as User
            setupViewRequest(tripR,userLite)
        }


    }
    //////REQUEST////////
 private fun setupViewRequest(trip: TripRequest, userLite: User){
        //user card setup
        val name = userLite.fullName
        trip_ac_user_name.text = name
        val pplDriven = "${userLite.peopleDriven} people driven"
        trip_ac_people_drv.text = pplDriven
        val ratingAndReviews = "${userLite.userRating} * ${userLite.userReviews.size} reviews"
        trip_ac_rating_review.text = ratingAndReviews
        Picasso.get().load(userLite.profilePic).into(trip__ac_user_pfp)
        ////
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
        val fod = "${trip.tripOrigin}, ${trip.originFullAddress}"
        trip__ac_desc1_card.text = fod
        val fdd = "${trip.tripDestination}, ${trip.destFullAddress}"
        trip_ac_desc2.text = fdd

        trip_ac_car_model.visibility = View.GONE
        trip_ac_type_color_year.visibility= View.GONE
        plate_layout.visibility = View.GONE
        trip_ac_type_color_year.visibility = View.GONE



        //show icon of booked users
        pendingUsers = trip.pendingDrivers
        trip_ac_luggage.text = trip.luggageSize
        //todo change icon
        when {
            trip.luggageSize == "N" -> trip_ac_luggage_text.text = getString(R.string.no_luggage)
            trip.luggageSize == "S" -> trip_ac_luggage_text.text = getString(R.string.small_luggage)
            trip.luggageSize == "M" -> trip_ac_luggage_text.text = getString(R.string.medium_luggage)
            trip.luggageSize == "L" -> trip_ac_luggage_text.text = getString(R.string.large_luggage)
        }

        trip_ac_smokeprf.visibility = View.GONE
        trip_ac_pets_pref.visibility = View.GONE
        trip_ac_price.visibility = View.GONE
        trip_ac_desc.text = trip.description
        seatsLeft = trip.numberOfSeats
        val nbSeats = "$seatsLeft Seats Required"
        trip_ac_seats.text = nbSeats
        pref_layout.visibility = View.GONE
        booked_layout.visibility = View.GONE
        val checkCode = checkRequestStatus(trip)
        if (checkCode == 0){
            trip_ac_submit_btn.visibility = View.VISIBLE
            val txt = "Request to drive"
            trip_ac_submit_btn.text = txt
        }
        menuType = checkCode
        trip_ac_submit_btn.setOnClickListener {
            when (checkCode) {
                0 -> submitRideRequest(trip)
                1 -> Toast.makeText(applicationContext,"You already request to drive for this trip.",Toast.LENGTH_SHORT).show()
                2 -> Toast.makeText(applicationContext,"You're the owner...",Toast.LENGTH_SHORT).show()
            }
        }
    }
private fun submitRideRequest(trip:TripRequest){
    val childName = trip.tripID
    val destCode = decodeWilaya(trip.destCity)
    val originCode = decodeWilaya(trip.originCity)
    var newRef = database.child("tripRequests").child("${originCode}_$destCode").child(childName)

    newRef.child("pendingDrivers").child(currentUser?.uid!!).setValue(0){ databaseError, _ ->
        if (databaseError != null) {
            Log.i("FireBaseEroor",databaseError.message)
            Toast.makeText(this, "Error $databaseError", Toast.LENGTH_LONG).show()}
    }
    newRef = database.child("users").child(trip.userID).child("driveRequests").child(trip.tripID)
    newRef.child(currentUser?.uid!!).setValue(Timestamp(System.currentTimeMillis()).toString())
    newRef.child("otd").setValue("${originCode}_$destCode")
    database.push()
    trip_users_layout.removeAllViews()
    pendingUsers[currentUser?.uid!!] = 1
}
private fun checkRequestStatus(trip:TripRequest):Int{
    return when {
        trip.userID == currentUser?.uid!! -> 2
        pendingUsers.containsKey(currentUser?.uid!!) -> 1
        else -> 0
    }

}



    ///////TRIP POST////////
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        val inflater = menuInflater
        inflater.inflate(R.menu.more_menu, menu)
        initMenu()

        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.modify_trip -> {
                modifyTrip()
                true
            }
            R.id.cancel_trip -> {
                cancelTrip()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun cancelOperation(type: Int){
        //0 -> pending
        //1 -> booked
        //2 -> owner
        val destCode = decodeWilaya(tripR.destCity)
        val originCode = decodeWilaya(tripR.originCity)
        val userRef = database.child("users")
        val tripRef = database.child("trips").child("${originCode}_$destCode").child(tripR.tripID)

        when (type) {
            0 -> {
                tripRef.child("pendingBookedUsers").child(currentUser?.uid!!).removeValue{ databaseError, _ ->
                    if (databaseError != null) {
                        Log.i("FireBaseEroor",databaseError.message)
                        Toast.makeText(this, "Error $databaseError", Toast.LENGTH_LONG).show()}
                }
                val newRef = userRef.child(tripR.userID).child("tripRequests").child(tripR.tripID)
                newRef.child(currentUser?.uid!!).removeValue{ databaseError, _ ->
                    if (databaseError != null) {
                        Log.i("FireBaseEroor",databaseError.message)
                        Toast.makeText(this, "Error $databaseError", Toast.LENGTH_LONG).show()}
                }
                database.push()
            }
            1 -> {
                tripRef.child("bookedUsers").child(currentUser?.uid!!).removeValue{ databaseError, _ ->
                    if (databaseError != null) {
                        Log.i("FireBaseEroor",databaseError.message)
                        Toast.makeText(this, "Error $databaseError", Toast.LENGTH_LONG).show()}
                }
                userRef.child(currentUser?.uid!!).child("bookedTrips").child(tripR.tripID).removeValue{ databaseError, _ ->
                    if (databaseError != null) {
                        Log.i("FireBaseEroor",databaseError.message)
                        Toast.makeText(this, "Error $databaseError", Toast.LENGTH_LONG).show()}
                }

                //notify owner
                userRef.child(tripR.userID).child("notifications").child("unbookedUsers").child(tripR.tripID).child(currentUser?.uid!!)
                    .setValue(1).addOnCompleteListener {
                        Log.i("DELETED,","DONE")
                        for (user in bookedUsersList){
                            if (user.userId == currentUser?.uid!!){
                                bookedUsersList.remove(user)
                            }
                        }
                        bookedUsers(trip) }
                val notifRef = userRef.child(tripR.userID).child("notifications").child("unbookedUsers").child(trip.tripID)
                notifRef.child("otd").setValue("${originCode}_$destCode")
                notifRef.child("timestamp").setValue(Date().toString())
                database.push()

            }
            2 -> {
                for (user in bookedUsersList){
                   // userRef.child(user.userId).child("notifications").child("canceledTrips").child(trip.tripID).setValue(1)
                    val notifRef =  userRef.child(user.userId).child("notifications")
                        .child("canceledTrips").child(trip.tripID)
                    notifRef.child("otd").setValue("${originCode}_$destCode")
                    notifRef.child("timestamp").setValue(Date().toString())
                    //todo
                    userRef.child(user.userId).child("bookedTrips").child(trip.tripID).removeValue()
                }

                tripRef.removeValue()
                //notify users
            }
            3 -> {
                val ref = database.child("tripRequests").child("${originCode}_$destCode").child(tripR.tripID)
                    .child("pendingDrivers")
                ref.child(currentUser?.uid!!).removeValue()
                val uRef = database.child("users").child(tripR.userID).child("driveRequests").child(tripR.tripID)
                    uRef.child(tripR.userID).removeValue()
            }
            4 -> {
                val ref = database.child("tripRequests").child("${originCode}_$destCode").child(tripR.tripID)
                ref.removeValue()
            }
        }


    }
    private fun showCancelingDialog(type:Int){
        //0 = pending
        //1 = booked
        //2 = owner
        var msg = "Cancel?"
        // Specifying a listener allows you to take an action before dismissing the dialog.
        // The dialog is automatically dismissed when a dialog button is clicked.

        // A null listener allows the button to dismiss the dialog and take no further action.
        //.setNegativeButton("Done!", null)
        when (type) {
            0 -> msg = "Do you want to cancel your pending request?"
            1 -> msg = "You can be suspended for canceling bookings, Do you wanna cancel your booking?"
            2 -> msg = "You can be suspended for canceling trips, Do you wanna cancel your trip? "
            3 -> msg = "You sure want to remove your request to drive this trip?"
            4 -> msg = "You sure you want to cancel your trip request?"
        }
        AlertDialog.Builder(this)
            .setTitle("Warning")
            .setMessage(msg)
            // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton("Yes") { dialog, which ->
             //   Toast.makeText(this,"Trip Canceled",Toast.LENGTH_SHORT).show()
                cancelOperation(type)
                dialog.cancel()

            }
            .setNegativeButton("No") { dialog, which ->
                dialog.cancel()
            }

            // A null listener allows the button to dismiss the dialog and take no further action.
            //.setNegativeButton("Done!", null)
            .setIcon(R.drawable.ic_warning_black_24dp)
            .show()
    }
    private fun modifyTrip(){
        if (!isRequest){
        Toast.makeText(this, "Modify Party", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, PostingActivity::class.java)
        intent.putExtra("modifyTrip",true)
        intent.putExtra("tripToModify",trip)
        startActivity(intent)
        } else {
            val intent = Intent(this, RequestTripActivity::class.java)
            intent.putExtra("modifyTrip",true)
            intent.putExtra("tripToModify",tripR)
            startActivity(intent)
        }
    }
    private fun cancelTrip(){
        if (!isRequest){
            if (menuType==1){
                if(isPendingUser){
                    showCancelingDialog(0)
                    Toast.makeText(this,"Remove Pending",Toast.LENGTH_SHORT).show()
                } else {
                    showCancelingDialog(1)
                    Toast.makeText(this,"Remove Booking",Toast.LENGTH_SHORT).show()
                }
            } else {
                showCancelingDialog(2)
                Toast.makeText(this,"Remove Trip",Toast.LENGTH_SHORT).show()
            }
        } else {
            if (menuType==1){
                showCancelingDialog(3)
                Toast.makeText(this,"Remove Drive Request",Toast.LENGTH_SHORT).show()
            } else if (menuType == 2){
                showCancelingDialog(4)
                Toast.makeText(this,"Cancel Trip Request",Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun initMenu(){

        if (menu!=null){
        when (menuType) {
            1 -> {
                val item = menu!!.findItem(R.id.cancel_trip)
                item.isVisible = true
            }
            2 -> {
                var item = menu!!.findItem(R.id.modify_trip)
                item.isVisible = true
                item = menu!!.findItem(R.id.cancel_trip)
                item.isVisible = true

            }
            else -> {
                var item = menu!!.findItem(R.id.modify_trip)
                item.isVisible = false
                item = menu!!.findItem(R.id.cancel_trip)
                item.isVisible = false
            }
        }
        } else {
            Log.i("Menu","Error")
        }
    }
    private fun showModifyTrip(){
        menuType = 2
        initMenu()

    }
    private fun cancelBookingMenu(){
        menuType = 1
        initMenu()
    }

    private fun setupView(trip: Trip, userLite: User){
        //user card setup
        bookedUsers(trip)
        val name = userLite.fullName
        trip_ac_user_name.text = name
        val pplDriven = "${userLite.peopleDriven} people driven"
        trip_ac_people_drv.text = pplDriven
        val ratingAndReviews = "${userLite.userRating} * ${userLite.userReviews.size} reviews"
        trip_ac_rating_review.text = ratingAndReviews
        Picasso.get().load(userLite.profilePic).into(trip__ac_user_pfp)
        ////
        val OLD_FORMAT = "dd/MM/yyyy"
        val NEW_FORMAT = "EEE, MMM dd"
        val newDate: String
   
        val sdf = SimpleDateFormat(OLD_FORMAT, Locale.US)
        val d = sdf.parse(trip.date)
        sdf.applyPattern(NEW_FORMAT)
        newDate = sdf.format(d)

        val fullDate = "$newDate at ${trip.time}"
        trip_ac_date.text = fullDate

        trip_ac_car_photo.setOnClickListener {
            val images = arrayListOf(trip.carPhoto)
            StfalconImageViewer.Builder<String>(this, images) { view, image ->
                Picasso.get().load(image).into(view)
            }.show()
        }

        trip_ac_origin.text = trip.originSubCity
        trip_ac_des.text = trip.destSubCity
        val fod = "${trip.origin}, ${trip.originFullAddress}"
        trip__ac_desc1_card.text = fod
        val fdd = "${trip.destination}, ${trip.destFullAddress}"
        trip_ac_desc2.text = fdd


        if (trip.hasVehicleInfo){
            //check vehicle prefs views
            if (trip.vehicleType=="" && trip.vehicleColor=="" && trip.vehicleYear==0){
                trip_ac_type_color_year.visibility = View.GONE
            } else {
                var vehiclePref = "${trip.vehicleType}, ${trip.vehicleColor} '${trip.vehicleYear}"
                 if(trip.vehicleType ==""){
                     vehiclePref = "${trip.vehicleColor} '${trip.vehicleYear}"
                 }
                trip_ac_type_color_year.text = vehiclePref
            }
            if (trip.vehicleModel != ""){
                trip_ac_car_model.text = trip.vehicleModel
            } else {
                trip_ac_car_model.visibility = View.GONE
            }
            if (trip.licensePlate != ""){
                trip_ac_lisence.text = trip.licensePlate
            } else {
                plate_layout.visibility = View.GONE
            }
            if (trip.carPhoto != ""){
                this.async { uiThread { downloadCarImage(trip.carPhoto) } }
            }
        } else {
            trip_ac_car_model.visibility = View.GONE
            trip_ac_type_color_year.visibility= View.GONE
            plate_layout.visibility = View.GONE
            trip_ac_type_color_year.visibility = View.GONE
        }


        //show icon of booked users

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


        trip_ac_submit_btn.setOnClickListener {
            when (checkTripStatus(trip)) {
                0 -> submitRequest(trip)
                1 -> Toast.makeText(applicationContext,"You're already booked! Wait for the trip.",Toast.LENGTH_SHORT).show()
                2 -> Toast.makeText(applicationContext,"Trip is full and you're booked. have a nice trip!",Toast.LENGTH_SHORT).show()
                3 -> Toast.makeText(applicationContext,"Your booking is pending, wait to be accepted.",Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setupBookingBtn(trip:Trip){

        Log.i("ENTERED HERE", "LOL4")
        if (checkTripStatus(trip) == 3 ){
            trip_ac_submit_btn.visibility = View.VISIBLE
            val btn:MaterialButton = findViewById(R.id.trip_ac_submit_btn)
            val text = "Pending Booking"
            trip_ac_submit_btn.text = text
            btn.icon = getDrawable(R.drawable.ic_date_range_black_24dp)
            btn.backgroundTintList = ContextCompat.getColorStateList(this@TripActivity, R.color.quantum_orange300)
            cancelBookingMenu()
            isPendingUser = true
            Log.i("ENTERED HERE", "LOL3")
        } else {
        if (trip.userID == currentUser?.uid!!){
            Log.i("ENTERED HERE", "LOL = ${trip.userID} + ${currentUser?.uid!!}")
            trip_ac_submit_btn.visibility = View.GONE
            showModifyTrip()
        } else {
            Log.i("ENTERED HERE", "LOL2")
            trip_ac_submit_btn.visibility = View.VISIBLE
            if (checkTripStatus(trip) == 0){
                if (trip.bookingPref == 0){
                    val btn:MaterialButton = findViewById(R.id.trip_ac_submit_btn)
                    trip_ac_submit_btn.text = getString(R.string.request_booking)
                    btn.icon = null
                    btn.backgroundTintList = ContextCompat.getColorStateList(this@TripActivity, R.color.colorPrimaryDark)
                } else {
                    val btn:MaterialButton = findViewById(R.id.trip_ac_submit_btn)
                    trip_ac_submit_btn.text = getString(R.string.instant_booking)
                    btn.icon = ContextCompat.getDrawable(this,R.drawable.ic_flash_on_yellow_24dp)
                    btn.backgroundTintList = ContextCompat.getColorStateList(this@TripActivity, R.color.colorPrimaryDark)
                }
            } else if(checkTripStatus(trip) == 1) {
                Log.i("ENTERED","LOL5")
                val btn:MaterialButton = findViewById(R.id.trip_ac_submit_btn)
                cancelBookingMenu()
                trip_ac_submit_btn.text = getString(R.string.your_booked)
                btn.icon = getDrawable(R.drawable.ic_date_range_black_24dp)
                btn.backgroundTintList = ContextCompat.getColorStateList(this@TripActivity, R.color.quantum_orange300)
            } else if (checkTripStatus(trip) == 2){
                cancelBookingMenu()
                val btn:MaterialButton = findViewById(R.id.trip_ac_submit_btn)
                btn.text = getString(R.string.full_trip)
                Log.i("COLOR", "C")
                btn.icon = getDrawable(ic_mtrl_chip_checked_circle)
                btn.backgroundTintList = ContextCompat.getColorStateList(this@TripActivity, R.color.quantum_googgreen500)

            }
        }
        }

    }
    private fun pendingUsersListener(){
        Log.i("bookedUsers", "DONE!")
        collectPendingUsers(object : OnGetDataListener {
            override fun onStart() {

            }

            override fun onSuccess(data: DataSnapshot) {
                //DO SOME THING WHEN GET DATA SUCCESS HERE

                for (child in data.children){
                    pendingUsersList.add(child.key!!)

                }
            }

            override fun onFailed(databaseError: DatabaseError) {
                Toast.makeText(this@TripActivity,"Failed to deliver data. Try Again.",Toast.LENGTH_LONG).show()
                this@TripActivity.finish()
            }
        })

    }
    private fun collectPendingUsers(listener: OnGetDataListener){
        listener.onStart()
        val childName = trip.tripID
        val destCode = decodeWilaya(trip.destCity)
        val originCode = decodeWilaya(trip.originCity)
        val newRef = database.child("trips").child("${originCode}_$destCode").child(childName).child("pendingBookedUsers")
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
    private fun checkTripStatus(trip:Trip):Int{
        Log.i("usersCount", bookedUsersList.size.toString())
        var result = 0
        if (trip.bookingPref != 1){

             if (pendingUsersList.contains(currentUser?.uid!!)){
                 Log.i("usersPCount", pendingUsersList.size.toString())
                 result = 3
             }
        }
        for (user in bookedUsersList){
            Log.i("usersid", user.userId)
            if (user.userId == currentUser?.uid!! || trip.userID == currentUser?.uid!!){
                //isRegistered
                result = 1
                if (bookedUsersList.size == trip.numberOfSeats){
                    //isFull
                    result = 2
                    break
                }
            }
        }

        return result
    }
    private fun addUserView(user:User){

        val layout = trip_users_layout
        val circleImageView = CircleImageView(this)
        Picasso.get().load(user.profilePic).into(circleImageView)
        circleImageView.setPadding(10,0,10,0)
        val params:LinearLayout.LayoutParams = layout.layoutParams as LinearLayout.LayoutParams

        params.height = matchParent
        params.width = wrapContent

        val layoutParams = LinearLayout.LayoutParams(100, wrapContent)
        circleImageView.layoutParams = layoutParams
        circleImageView.borderWidth = 2
        circleImageView.circleBackgroundColor = Color.parseColor("#FF000000")
        circleImageView.onClick {  }
        layout.addView(circleImageView)
}
    private fun submitRequest(trip: Trip){
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
           // trip_ac_submit_btn.visibility = View.VISIBLE

        }
    }
    private fun setupBookedCount(trip: Trip, mCount:Int){

        seatsLeft = trip.numberOfSeats - mCount
        val nbSeats = "$seatsLeft Seats Left"
        trip_ac_seats.text = nbSeats

    }
    private fun getBookedUsersFromDB(userId: String,listener: OnGetDataListener){
        val userRef = database.child("users").child(userId)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listener.onSuccess(dataSnapshot)
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        }
        userRef.addListenerForSingleValueEvent(eventListener)

    }
    private fun bookedUsersDataListener(userId: String){
        getBookedUsersFromDB(userId, object : OnGetDataListener {
            override fun onStart() {


            }

            override fun onSuccess(data: DataSnapshot) {
              val user = data.getValue(User::class.java)
                Log.i("ADDED", "DONE!")
                bookedUsersList.add(user!!)
                addUserView(user)
                setupBookingBtn(trip)
            }

            override fun onFailed(databaseError: DatabaseError) {

            }
        })

    }
    private fun bookedUsers(trip: Trip){
        var mCount: Int
        Log.i("bookedUsers", "DONE!")
        countBookedPeople(trip,object : OnGetDataListener {
            override fun onStart() {

                setPb(1)
            }

            override fun onSuccess(data: DataSnapshot) {
                //DO SOME THING WHEN GET DATA SUCCESS HERE

                mCount = data.childrenCount.toInt()
                for (child in data.children){
                    userIdList.add(child.key!!)
                    Log.i("countBookedPeople", "DONE!")
                    bookedUsersDataListener(child.key!!)

                }
                if (data.childrenCount.toInt() == 0 ){
                    setupBookingBtn(trip)
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
    private fun saveToDB(trip: Trip){
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
        val  userRef = database.child("users").child(currentUser?.uid!!)
        userRef.child("bookedTrips").child(trip.tripID).setValue(1){ databaseError, _ ->
            if (databaseError != null) {
                Log.i("FireBaseEroor",databaseError.message)
                Toast.makeText(this, "Error $databaseError", Toast.LENGTH_LONG).show()}
        }
        val notifRef= database.child("users").child(trip.userID).child("notifications")
            .child("bookedUsers").child(trip.tripID)
            notifRef.child("userId").setValue(currentUser?.uid)
             notifRef.child("otd").setValue("${originCode}_$destCode")
            notifRef.child("timestamp").setValue(Date().toString())


        database.push()
        Log.i("saveToDB", "DONE!")
        bookedUsers(trip)
    }
    private fun savePendingToDB(trip: Trip){
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
        trip_users_layout.removeAllViews()
        pendingUsersListener()

        bookedUsers(trip)
    }
    private fun downloadCarImage(carPhotoURL:String){
        Picasso.get().load(carPhotoURL).into(trip_ac_car_photo)
    }

}