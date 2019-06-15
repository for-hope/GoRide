package me.lamine.goride.tripActivity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.beust.klaxon.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.squareup.picasso.Picasso
import com.stfalcon.imageviewer.StfalconImageViewer
import com.wajahatkarim3.easyvalidation.core.view_ktx.maxLength
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_trip.*
import kotlinx.android.synthetic.main.view_luggage_icon.*
import me.lamine.goride.R
import me.lamine.goride.R.drawable.ic_mtrl_chip_checked_circle
import me.lamine.goride.dataObjects.Trip
import me.lamine.goride.dataObjects.TripRequest
import me.lamine.goride.dataObjects.User
import me.lamine.goride.interfaces.OnGetDataListener
import me.lamine.goride.postingActivity.PostingActivity
import me.lamine.goride.requestActivity.RequestTripActivity
import me.lamine.goride.userActivity.UserActivity
import me.lamine.goride.utils.Database
import me.lamine.goride.utils.decodePoly
import me.lamine.goride.utils.decodeWilaya
import me.lamine.goride.utils.getUrl
import org.jetbrains.anko.*
import java.io.IOException
import java.lang.IndexOutOfBoundsException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class TripActivity : AppCompatActivity(), OnMapReadyCallback {
    private val apiKey: String = "AIzaSyDWbc3KQP6ssBlClf8HSiZWEtMxfwqSYto"
    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap
        Log.i("MAP READY","START")
       setUpMap()

    }

    private fun addMarkersOnMap() {
        map?.clear()
        val fromLatLng:LatLng?
        val toLatLng:LatLng?
        val originCity:String?
        val destCity:String?
        if (::trip.isInitialized){
            fromLatLng = getCurrentLocation(trip.originCity)
            toLatLng = getCurrentLocation(trip.destCity)
            originCity = trip.originCity
            destCity = trip.destCity
        } else {
             fromLatLng = getCurrentLocation(tripR.originCity)
             toLatLng = getCurrentLocation(tripR.destCity)
            originCity = tripR.originCity
            destCity = tripR.destCity
        }

        if (fromLatLng != null && toLatLng != null) {
            map?.addMarker(MarkerOptions().position(fromLatLng).title(originCity).icon(
                BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_RED)
            ))
            map?.addMarker(MarkerOptions().position(toLatLng).title(destCity).icon(
                BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            ))
            map?.moveCamera(CameraUpdateFactory.newLatLng(fromLatLng))
        }

        val latLongB = LatLngBounds.Builder()
        var origin = LatLng(0.0, 0.0)
        var dest = LatLng(0.0, 0.0)
        if (fromLatLng != null && toLatLng != null) {
            origin = fromLatLng
            dest = toLatLng
        }
        val sydney = origin
        val opera = dest
        val options = PolylineOptions()
        options.color(Color.RED)
        options.width(5f)
        // Getting URL to the Google Directions API
        // build URL to call API
        val url = getUrl(sydney, opera)

        async {
            // Connect to URL, download content and convert into string asynchronously
            val result = URL(url).readText()
            uiThread {
                // When API call is done, create parser and convert into JsonObjec
                val parser = Parser()
                val stringBuilder: StringBuilder = StringBuilder(result)
                val json: JsonObject = parser.parse(stringBuilder) as JsonObject
                // get to the correct element in JsonObject
                val routes = json.array<JsonObject>("routes")
                try {
                    @Suppress("UNCHECKED_CAST") val points = routes!!["legs"]["steps"][0] as JsonArray<JsonObject>
                    // For every element in the JsonArray, decode the polyline string and pass all points to a List
                    val polypts = points.flatMap { decodePoly(it.obj("polyline")?.string("points")!!) }
                    // Add  points to polyline and bounds
                    options.add(sydney)
                    latLongB.include(sydney)
                    for (point in polypts) {
                        options.add(point)
                        latLongB.include(point)
                    }
                    options.add(opera)
                    latLongB.include(opera)
                    // build bounds
                    val bounds = latLongB.build()
                    // add polyline to the map
                    map?.addPolyline(options)
                    // show map with route centered
                    map?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                } catch (e:IndexOutOfBoundsException){
                    e.stackTrace
                    Log.i("MapError", e.stackTrace.toString())
                    Toast.makeText(this@TripActivity,"Error loading the map",Toast.LENGTH_SHORT).show()

                }

            }
        }
    }

    @SuppressLint("MissingPermission")
    fun setUpMap() {
        addMarkersOnMap()
       // setCurrentLocation(trip)g

    }
    private fun getLocationFromAddress(context: Context, strAddress: String): LatLng? {

        val coder = Geocoder(context)
        val address: List<Address>?
        var p1: LatLng? = null

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5)
            if (address == null) {
                return null
            }

            val location = address[0]
            p1 = LatLng(location.getLatitude(), location.longitude)

        } catch (ex: IOException) {

            ex.printStackTrace()
        }

        return p1
    }
    private fun getCurrentLocation(location:String):LatLng? {
        Log.i("Location","Getting current location")
        val myLocation = getLocationFromAddress(this,location)
        return myLocation
      /*  if (myLocation != null) {
            val dLatitude = myLocation.latitude
            val dLongitude = myLocation.longitude
            map?.addMarker(
                MarkerOptions().position(LatLng(dLatitude, dLongitude))
                    .title("My Location").icon(
                        BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                    )
            )
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(dLatitude, dLongitude), 8f))

        } else {
            Log.i("Location","Getting current false")
            Toast.makeText(this, "Unable to fetch the current location", Toast.LENGTH_SHORT).show()
        }*/
    }


    private val pendingUsersList: MutableList<String> = mutableListOf()
    private var userIdList: MutableList<String> = mutableListOf()
    private var seatsLeft = -1
    private lateinit var trip: Trip
    private var menu: Menu? = null
    private var menuType = 0
    private var map: GoogleMap? = null
    private var isPendingUser = false
    private lateinit var userLite: User
    private lateinit var tripR: TripRequest
    private var isRequest = false
    private var pendingUsers: HashMap<String, Any> = hashMapOf()
    private var bookedUsersList: MutableList<User> = mutableListOf()
    private lateinit var mDatabase: Database
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip)
        setSupportActionBar(trip_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        async {
            uiThread {
                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map_fragment) as SupportMapFragment
                mapFragment.getMapAsync(this@TripActivity)
            }
        }




        mDatabase = Database()
        profile_card_view.setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            intent.putExtra("UserProfile", userLite)
            startActivity(intent)
        }
        isRequest = intent.getBooleanExtra("isRequest", false)
        if (!isRequest) {
            trip = intent.getSerializableExtra("ClickedTrip") as Trip
            userLite = intent.getSerializableExtra("ClickedTripUser") as User
            var stopsListText = ""
            for(stop in trip.stops){
                stopsListText = "$stopsListText, $stop"
            }
            if (stopsListText == ""){
                stops_layout.visibility = View.GONE
            } else {
                trip_ac_stops_desc.text = stopsListText
            }

            if (trip.bookingPref != 1) {
                pendingUsersListener()
            }
            setupView(trip, userLite)
            if (trip.userID == mDatabase.currentUserId()) {
                Log.i("VIEW", "GONE HERE 1")
                trip_ac_submit_btn.visibility = View.GONE
                showModifyTrip()
            }
        } else {
            trip_toolbar.title = "Trip Request"
            stops_layout.visibility = View.GONE
            tripR = intent.getSerializableExtra("ClickedTrip") as TripRequest
            userLite = intent.getSerializableExtra("ClickedTripUser") as User
            setupViewRequest(tripR, userLite)
        }


    }

    //////REQUEST////////
    private fun setupViewRequest(trip: TripRequest, userLite: User) {
        //user card setup
        val name = userLite.fullName
        trip_ac_user_name.text = name
        val pplDriven = "${userLite.peopleDriven} people driven"
        trip_ac_people_drv.text = pplDriven
        val ratingAndReviews = "${userLite.userRating} * ${userLite.userReviews.size} reviews"
        trip_ac_rating_review.text = ratingAndReviews
        Picasso.get().load(userLite.profilePic).into(trip__ac_user_pfp)
        ////
        val mOldFormat = "dd/MM/yyyy"
        val mNewFormat = "EEE, MMM dd"
        val newDate: String

        val sdf = SimpleDateFormat(mOldFormat, Locale.US)
        val d = sdf.parse(trip.date)
        sdf.applyPattern(mNewFormat)
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
        trip_ac_type_color_year.visibility = View.GONE
        //todo check
        trip_ac_licence.visibility = View.GONE
        trip_ac_type_color_year.visibility = View.GONE


        //show icon of booked users
        pendingUsers = trip.pendingDrivers

        trip_ac_luggage.text = trip.luggageSize
        if (trip.acceptedDriver.size > 0) {
            booked_layout.visibility = View.VISIBLE
            val txt = "Trip Accepted"
            trip_ac_booked.text = txt
        } else {
            booked_layout.visibility = View.GONE
        }

        //todo change icon
        when {
            trip.luggageSize == "N" -> trip_ac_luggage_text.text = getString(R.string.no_luggage)
            trip.luggageSize == "S" -> trip_ac_luggage_text.text = getString(R.string.small_luggage)
            trip.luggageSize == "M" -> trip_ac_luggage_text.text = getString(R.string.medium_luggage)
            trip.luggageSize == "L" -> trip_ac_luggage_text.text = getString(R.string.large_luggage)
        }

        trip_ac_smoke_pref.visibility = View.GONE
        trip_ac_pets_pref.visibility = View.GONE
        trip_ac_price.visibility = View.GONE
        trip_ac_desc.text = trip.description
        seatsLeft = trip.numberOfSeats
        val nbSeats = "$seatsLeft Seats Required"
        trip_ac_seats.text = nbSeats

        pref_layout.visibility = View.GONE
        val checkCode = checkRequestStatus(trip)
        if (checkCode == 0) {
            trip_ac_submit_btn.visibility = View.VISIBLE
            val txt = "Request to drive"
            trip_ac_submit_btn.text = txt
        }
        menuType = checkCode
        trip_ac_submit_btn.setOnClickListener {
            when (checkCode) {
                0 -> submitRideRequest(trip)
                1 -> Toast.makeText(
                    applicationContext,
                    "You already request to drive for this trip.",
                    Toast.LENGTH_SHORT
                ).show()
                2 -> Toast.makeText(applicationContext, "You're the owner...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitRideRequest(trip: TripRequest) {
        Log.i("RideRequest", "Requesting a ride")
        val childName = trip.tripID
        val destCode = decodeWilaya(trip.destCity)
        val originCode = decodeWilaya(trip.originCity)
        val otdPath = "${String.format("%02d", originCode)}_${String.format("%02d", destCode)}"
        var rootPath = "tripRequests/$otdPath/$childName/pendingDrivers/${mDatabase.currentUserId()}"
        mDatabase.addToPath(rootPath, 0)
        rootPath = "users/${trip.userID}/notifications/driveRequests/${trip.tripID}/${mDatabase.currentUserId()}"
        var mPath = "$rootPath/otd"
        mDatabase.addToPath(mPath, otdPath)
        mPath = "$rootPath/date"
        mDatabase.addToPath(mPath, "${trip.date} ${trip.time}")
        mPath = "$rootPath/timestamp"
        mDatabase.addToPath(mPath, Date().toString())
        trip_users_layout.removeAllViews()
        pendingUsers[mDatabase.currentUserId()] = 1
        val text = "Pending Request"
        trip_ac_submit_btn.text = text
    }

    private fun checkRequestStatus(trip: TripRequest): Int {
        return when {
            trip.userID == mDatabase.currentUserId() -> 2
            pendingUsers.containsKey(mDatabase.currentUserId()) -> 1
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
            R.id.report_trip -> {
                showReportDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    private fun sendReport(report:String){
        val mReportHashMap:HashMap<String,String> = hashMapOf()
        mReportHashMap["reporterId"] = mDatabase.currentUserId()
        mReportHashMap["tripId"] = if (isRequest) {
            tripR.tripID
        } else {
            trip.tripID
        }
        mReportHashMap["report"]= report
        val key = mDatabase.pushKey("reports/")
        mDatabase.addToPath("reports/$key/", mReportHashMap)
    }
    private fun showReportDialog() {
        val input:EditText = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.maxLines = 10
        input.maxLength(300)
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Report Trip")
            .setMessage("Write below your report about this trip.")
            .setView(input)
            // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked

            .setPositiveButton("Done") { _, _ ->
                if (input.text.toString() != ""){
                sendReport(input.text.toString())
                Toast.makeText(this, "Report Received!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel"){ _, _ ->
                Toast.makeText(this,"Canceled.",Toast.LENGTH_SHORT).show()
            }
            // A null listener allows the button to dismiss the dialog and take no further action.
            //.setNegativeButton("Done!", null)
            .setIcon(R.drawable.ic_info_gray_24dp)
            .show()
    }
    private fun cancelOperation(type: Int) {
        //0 -> pending
        //1 -> booked
        //2 -> owner
        Log.i("TYPE", "t = $type")
        //3 pending request trip
        //4 cancel request trip
        var otdPath = ""
        val userPath = "users"
        var tripPath = ""
        if (type <3){
        val destCode = decodeWilaya(trip.destCity)
        val originCode = decodeWilaya(trip.originCity)
        otdPath = "${String.format("%02d", originCode)}_${String.format("%02d", destCode)}"
        tripPath = "trips/$otdPath/${trip.tripID}"}
        else {
        val destCode = decodeWilaya(tripR.destCity)
        val originCode = decodeWilaya(tripR.originCity)
        otdPath = "${String.format("%02d", originCode)}_${String.format("%02d", destCode)}"
        tripPath = "tripRequests/$otdPath/${tripR.tripID}"

        }


        when (type) {
            0 -> {
                var mPath = "$tripPath/pendingBookedUsers/${mDatabase.currentUserId()}"
                mDatabase.removeFromPath(mPath)
                mPath = "$userPath/${trip.userID}/notifications/tripRequests/${trip.tripID}/${mDatabase.currentUserId()}"
                mDatabase.removeFromPath(mPath)

            }
            1 -> {
                var mPath = "$tripPath/bookedUsers/${mDatabase.currentUserId()}"
                mDatabase.removeFromPath(mPath)
                mPath = "$userPath/${mDatabase.currentUserId()}/bookedTrips/${trip.tripID}"
                mDatabase.removeFromPath(mPath)
                mPath =
                    "$userPath/${trip.userID}/notification/unbookedUsers/${trip.tripID}/${mDatabase.currentUserId()}"
                mDatabase.addToPath(mPath, otdPath)
                //onCompleteListener
                for (user in bookedUsersList) {
                    if (user.userId == mDatabase.currentUserId()) {
                        bookedUsersList.remove(user)
                    }
                }
                bookedUsers(trip)
                //notify user
                mPath = "$userPath/${trip.userID}/notifications/unbookedUsers/${trip.tripID}"
                mDatabase.addToPath("$mPath/otd", otdPath)
                mDatabase.addToPath("$mPath/timestamp", Date().toString())

            }
            2 -> {
                for (user in bookedUsersList) {
                    // userRef.child(user.userId).child("notifications").child("canceledTrips").child(trip.tripID).setValue(1)
                    val rootPath = "$userPath/${user.userId}/notifications/canceledTrips/${trip.tripID}"
                    var mPath = "$rootPath/otd"
                    mDatabase.addToPath(mPath, otdPath)
                    mPath = "$rootPath/timestamp"
                    mDatabase.addToPath(mPath, Date().toString())


                    mPath = "$userPath/${user.userId}/bookedTrips/${trip.tripID}"
                    mDatabase.removeFromPath(mPath)

                }
                mDatabase.removeFromPath(tripPath)
                //notify users
            }

            3 -> {
                var rootPath = "tripRequests/$otdPath/${tripR.tripID}/pendingDrivers/${mDatabase.currentUserId()}"
                mDatabase.removeFromPath(rootPath)
                rootPath = "users/${tripR.userID}/notifications/driveRequests/${tripR.tripID}/${tripR.userID}"
                mDatabase.removeFromPath(rootPath)
            }
            4 -> {

                val rootPath = "tripRequests/$otdPath/${tripR.tripID}"
                mDatabase.removeFromPath("users/${mDatabase.currentUserId()}/activeTripRequests/${tripR.tripID}")
                mDatabase.removeFromPath(rootPath)

                Log.i("TYPE", "t2 = $rootPath")

            }
        }
        finish()
        if (type != 4 && type != 2){
        overridePendingTransition(0, 0);
        startActivity(intent)
        overridePendingTransition(0, 0);}


    }

    private fun showCancelingDialog(type: Int) {
        //0 = pending
        //1 = booked
        //2 = owner

        //3 remove pending request to request
        //4 remove request
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
            .setPositiveButton("Yes") { dialog, _ ->
                //   Toast.makeText(this,"Trip Canceled",Toast.LENGTH_SHORT).show()
                cancelOperation(type)
                dialog.cancel()

            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }

            // A null listener allows the button to dismiss the dialog and take no further action.
            //.setNegativeButton("Done!", null)
            .setIcon(R.drawable.ic_warning_black_24dp)
            .show()
    }

    private fun modifyTrip() {
        if (!isRequest) {
            Toast.makeText(this, "Modify Party", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, PostingActivity::class.java)
            intent.putExtra("modifyTrip", true)
            intent.putExtra("tripToModify", trip)
            startActivity(intent)
        } else {
            val intent = Intent(this, RequestTripActivity::class.java)
            intent.putExtra("modifyTrip", true)
            intent.putExtra("tripToModify", tripR)
            startActivity(intent)
        }
    }

    private fun cancelTrip() {
        if (!isRequest) {
            if (menuType == 1) {
                if (isPendingUser) {
                    showCancelingDialog(0)
                    Toast.makeText(this, "Remove Pending", Toast.LENGTH_SHORT).show()
                } else {
                    showCancelingDialog(1)
                    Toast.makeText(this, "Remove Booking", Toast.LENGTH_SHORT).show()
                }
            } else {
                showCancelingDialog(2)
                Toast.makeText(this, "Remove Trip", Toast.LENGTH_SHORT).show()
            }
        } else {
            if (menuType == 1) {
                showCancelingDialog(3)
                Toast.makeText(this, "Remove Drive Request", Toast.LENGTH_SHORT).show()
            } else if (menuType == 2) {
                showCancelingDialog(4)
                Toast.makeText(this, "Cancel Trip Request", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun initMenu() {

        if (menu != null) {
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
            Log.i("Menu", "Error")
        }
    }

    private fun showModifyTrip() {
        menuType = 2
        initMenu()

    }

    private fun cancelBookingMenu() {
        menuType = 1
        initMenu()
    }

    private fun setupView(trip: Trip, userLite: User) {
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
        val mOldFormat = "dd/MM/yyyy"
        val mNewFormat = "EEE, MMM dd"
        val newDate: String

        val sdf = SimpleDateFormat(mOldFormat, Locale.US)
        val d = sdf.parse(trip.date)
        sdf.applyPattern(mNewFormat)
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


        if (trip.hasVehicleInfo) {
            //check vehicle prefs views
            if (trip.vehicleType == "" && trip.vehicleColor == "" && trip.vehicleYear == 0) {
                trip_ac_type_color_year.visibility = View.GONE
            } else {
                var vehiclePref = "${trip.vehicleType}, ${trip.vehicleColor} '${trip.vehicleYear}"
                if (trip.vehicleType == "") {
                    vehiclePref = "${trip.vehicleColor} '${trip.vehicleYear}"
                }
                trip_ac_type_color_year.text = vehiclePref
            }
            if (trip.vehicleModel != "") {
                trip_ac_car_model.text = trip.vehicleModel
            } else {
                trip_ac_car_model.visibility = View.GONE
            }
            if (trip.licensePlate != "") {
                trip_ac_licence.text = trip.licensePlate
            } else {
                trip_ac_licence.visibility = View.GONE
            }
            if (trip.carPhoto != "") {
                this.async { uiThread { downloadCarImage(trip.carPhoto) } }
            }
        } else {
            trip_ac_car_model.visibility = View.GONE
            trip_ac_type_color_year.visibility = View.GONE
            trip_ac_licence.visibility = View.GONE
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
        if (trip.noSmoking) {
            trip_ac_smoke_pref.text = getString(R.string.smoking_allowed)
            trip_ac_smoke_pref.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            trip_ac_smoke_pref.text = getString(R.string.smoking_allowed)
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
                1 -> Toast.makeText(
                    applicationContext,
                    "You're already booked! Wait for the trip.",
                    Toast.LENGTH_SHORT
                ).show()
                2 -> Toast.makeText(
                    applicationContext,
                    "Trip is full and you're booked. have a nice trip!",
                    Toast.LENGTH_SHORT
                ).show()
                3 -> Toast.makeText(
                    applicationContext,
                    "Your booking is pending, wait to be accepted.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @SuppressLint("PrivateResource")
    private fun setupBookingBtn(trip: Trip) {

        Log.i("ENTERED HERE", "LOL4")
        if (checkTripStatus(trip) == 3) {
            trip_ac_submit_btn.visibility = View.VISIBLE
            val btn: MaterialButton = findViewById(R.id.trip_ac_submit_btn)
            val text = "Pending Booking"
            trip_ac_submit_btn.text = text
            btn.icon = getDrawable(R.drawable.ic_date_range_black_24dp)
            btn.backgroundTintList = ContextCompat.getColorStateList(this@TripActivity, R.color.quantum_orange300)
            cancelBookingMenu()
            isPendingUser = true
            Log.i("ENTERED HERE", "LOL3")
        } else {
            if (trip.userID == mDatabase.currentUserId()) {
                Log.i("ENTERED HERE", "LOL = ${trip.userID} + ${mDatabase.currentUserId()}")
                trip_ac_submit_btn.visibility = View.GONE
                showModifyTrip()
            } else {
                Log.i("ENTERED HERE", "LOL2")
                trip_ac_submit_btn.visibility = View.VISIBLE
                if (checkTripStatus(trip) == 0) {
                    if (trip.bookingPref == 0) {
                        val btn: MaterialButton = findViewById(R.id.trip_ac_submit_btn)
                        trip_ac_submit_btn.text = getString(R.string.request_booking)
                        btn.icon = null
                        btn.backgroundTintList =
                            ContextCompat.getColorStateList(this@TripActivity, R.color.colorPrimaryDark)
                    } else {
                        val btn: MaterialButton = findViewById(R.id.trip_ac_submit_btn)
                        trip_ac_submit_btn.text = getString(R.string.instant_booking)
                        btn.icon = ContextCompat.getDrawable(this, R.drawable.ic_flash_on_yellow_24dp)
                        btn.backgroundTintList =
                            ContextCompat.getColorStateList(this@TripActivity, R.color.colorPrimaryDark)
                    }
                } else if (checkTripStatus(trip) == 1) {
                    Log.i("ENTERED", "LOL5")
                    val btn: MaterialButton = findViewById(R.id.trip_ac_submit_btn)
                    cancelBookingMenu()
                    trip_ac_submit_btn.text = getString(R.string.your_booked)
                    btn.icon = getDrawable(R.drawable.ic_date_range_black_24dp)
                    btn.backgroundTintList =
                        ContextCompat.getColorStateList(this@TripActivity, R.color.quantum_orange300)
                } else if (checkTripStatus(trip) == 2) {
                    cancelBookingMenu()
                    val btn: MaterialButton = findViewById(R.id.trip_ac_submit_btn)
                    btn.text = getString(R.string.full_trip)
                    Log.i("COLOR", "C")
                    //todo suprressed
                    btn.icon = getDrawable(ic_mtrl_chip_checked_circle)
                    btn.backgroundTintList =
                        ContextCompat.getColorStateList(this@TripActivity, R.color.quantum_googgreen500)

                }
            }
        }

    }

    private fun pendingUsersListener() {
        val tripId: String = trip.tripID
        val destCode = decodeWilaya(trip.destCity)
        val originCode = decodeWilaya(trip.originCity)
        val otdPath = "${String.format("%02d", originCode)}_${String.format("%02d", destCode)}"
        mDatabase.fetchFromTrip(tripId, otdPath, "pendingBookedUsers", object : OnGetDataListener {
            override fun onStart() {

            }

            override fun onSuccess(data: DataSnapshot) {
                //DO SOME THING WHEN GET DATA SUCCESS HERE

                for (child in data.children) {
                    pendingUsersList.add(child.key!!)

                }
            }

            override fun onFailed(databaseError: DatabaseError) {
                Toast.makeText(this@TripActivity, "Failed to deliver data. Try Again.", Toast.LENGTH_LONG).show()
                this@TripActivity.finish()
            }
        })

    }

    private fun checkTripStatus(trip: Trip): Int {
        var result = 0
        if (trip.bookingPref != 1) {

            if (pendingUsersList.contains(mDatabase.currentUserId())) {
                result = 3
            }
        }
        for (user in bookedUsersList) {
            if (user.userId == mDatabase.currentUserId() || trip.userID == mDatabase.currentUserId()) {
                //isRegistered
                result = 1
                if (bookedUsersList.size == trip.numberOfSeats) {
                    //isFull
                    result = 2
                    break
                }
            }
        }

        return result
    }

    private fun addUserView(user: User) {

        val layout = trip_users_layout
        val circleImageView = CircleImageView(this)
        Picasso.get().load(user.profilePic).into(circleImageView)
        circleImageView.setPadding(10, 0, 10, 0)
        val params: LinearLayout.LayoutParams = layout.layoutParams as LinearLayout.LayoutParams

        params.height = matchParent
        params.width = wrapContent

        val layoutParams = LinearLayout.LayoutParams(100, wrapContent)
        circleImageView.layoutParams = layoutParams
        circleImageView.borderWidth = 2
        circleImageView.circleBackgroundColor = Color.parseColor("#FF000000")
        circleImageView.onClick { }
        layout.addView(circleImageView)
    }

    private fun submitRequest(trip: Trip) {
        if (trip.bookingPref == 1) {
            saveToDB(trip)
        } else {

            savePendingToDB(trip)
        }

    }

    private fun setPb(visibility: Int) {

        if (visibility == 1) {

            pb_search_act.visibility = View.VISIBLE
            grey_sact_layout.visibility = View.VISIBLE

        } else {

            pb_search_act.visibility = View.GONE
            grey_sact_layout.visibility = View.GONE
            // trip_ac_submit_btn.visibility = View.VISIBLE

        }
    }

    private fun setupBookedCount(trip: Trip, mCount: Int) {
        seatsLeft = trip.numberOfSeats - mCount
        val nbSeats = "$seatsLeft Seats Left"
        trip_ac_seats.text = nbSeats
    }

    private fun bookedUsersDataListener(userId: String) {
        //todo clear
        bookedUsersList.clear()
        mDatabase.fetchUser(userId, object : OnGetDataListener {
            override fun onStart() {
            }

            override fun onSuccess(data: DataSnapshot) {
                val user = data.getValue(User::class.java)
                bookedUsersList.add(user!!)
                addUserView(user)
                setupBookingBtn(trip)
            }

            override fun onFailed(databaseError: DatabaseError) {

            }
        })

    }

    private fun bookedUsers(trip: Trip) {
        val tripId = trip.tripID
        val destCode = decodeWilaya(trip.destCity)
        val originCode = decodeWilaya(trip.originCity)
        val otdPath = "${String.format("%02d", originCode)}_${String.format("%02d", destCode)}"
        var mCount: Int
        mDatabase.fetchFromTrip(tripId, otdPath, "bookedUsers", object : OnGetDataListener {
            override fun onStart() {

                setPb(1)
            }

            override fun onSuccess(data: DataSnapshot) {
                //DO SOME THING WHEN GET DATA SUCCESS HERE

                mCount = data.childrenCount.toInt()
                for (child in data.children) {
                    userIdList.add(child.key!!)
                    bookedUsersDataListener(child.key!!)

                }
                if (data.childrenCount.toInt() == 0) {
                    setupBookingBtn(trip)
                }
                setupBookedCount(trip, mCount)
                setPb(0)

            }

            override fun onFailed(databaseError: DatabaseError) {
                Toast.makeText(this@TripActivity, "Failed to deliver data. Try Again.", Toast.LENGTH_LONG).show()
                this@TripActivity.finish()
            }
        })

    }

    private fun saveToDB(trip: Trip) {
        val childName = trip.tripID
        val destCode = decodeWilaya(trip.destCity)
        val originCode = decodeWilaya(trip.originCity)
        val otdPath = "${String.format("%02d", originCode)}_${String.format("%02d", destCode)}"
        var rootPath = "trips/$otdPath/$childName/bookedUsers/${mDatabase.currentUserId()}"
        mDatabase.addToPath(rootPath, 1)
        rootPath = "users/${mDatabase.currentUserId()}/bookedTrips/${trip.tripID}"
        mDatabase.addToPath(rootPath, otdPath)
        rootPath = "users/${trip.userID}/notifications/bookedUsers/${trip.tripID}"
        var mPath = "$rootPath/userId"
        mDatabase.addToPath(mPath, mDatabase.currentUserId())
        mPath = "$rootPath/otd"
        mDatabase.addToPath(mPath, otdPath)
        mPath = "$rootPath/timestamp"
        mDatabase.addToPath(mPath, Date().toString())
        bookedUsers(trip)
    }

    private fun savePendingToDB(trip: Trip) {
        val childName = trip.tripID
        val destCode = decodeWilaya(trip.destCity)
        val originCode = decodeWilaya(trip.originCity)
        val otdPath = "${String.format("%02d", originCode)}_${String.format("%02d", destCode)}"
        var rootPath = "trips/$otdPath/$childName/pendingBookedUsers/${mDatabase.currentUserId()}"
        mDatabase.addToPath(rootPath, 0)
        rootPath = "users/${trip.userID}/notifications/tripRequests/${trip.tripID}/${mDatabase.currentUserId()}"
        var mPath = "$rootPath/otd"
        mDatabase.addToPath(mPath, otdPath)
        mPath = "$rootPath/timestamp"
        mDatabase.addToPath(mPath, Date().toString())
        trip_users_layout.removeAllViews()
        pendingUsersListener()
        bookedUsers(trip)
    }

    private fun downloadCarImage(carPhotoURL: String) {
        Picasso.get().load(carPhotoURL).into(trip_ac_car_photo)
    }
    private fun MapSetup(){

    }

}