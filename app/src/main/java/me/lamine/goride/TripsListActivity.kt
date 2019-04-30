package me.lamine.goride


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.gigamole.navigationtabstrip.NavigationTabStrip
import kotlinx.android.synthetic.main.activity_trips_list.*

import android.graphics.Color

import android.util.Log

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import java.util.ArrayList
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase




class TripsListActivity : AppCompatActivity() {

    private lateinit var returnedTrip:Trip
    private lateinit var database: DatabaseReference
    private val listOfTrips:MutableList<Trip> = mutableListOf()
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trips_list)
        setSupportActionBar(findViewById(R.id.results_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);
        val toText:String = intent.getStringExtra("to")
        val fromText:String = intent.getStringExtra("from")
        val listOfTrips: MutableList<Trip> = mutableListOf()
        val listOfStringTrips: ArrayList<String> = arrayListOf()
       /* database = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        if (mAuth?.currentUser == null) {
            finish()
        }else {
            currentUser = mAuth?.currentUser
        }
        val childName = "${fromText}_$toText"
      database.child("trips").child(childName)
        val myTripQuery = database.child("trips").child(childName)
            .orderByChild("date")
        ////
        //
        //
        Log.i("MYDATE","STARTED $toText, $fromText")
        val rootRef = FirebaseDatabase.getInstance().reference
        val currentUserIdRef = rootRef.child("trips").child(childName)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {

                    val userIdRef = rootRef.child("trips").child(childName).child(ds.key!!)
                    val eventListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            //todo set up trips
                            val addTrip = getData(dataSnapshot)
                            listOfTrips.add(addTrip)

                           // val date = dataSnapshot.child("date").value as String?

                             Log.i("MYDATE",addTrip.date)
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            throw databaseError.toException() // don't ignore errors
                        }
                    }
                    userIdRef.addListenerForSingleValueEvent(eventListener)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException() // don't ignore errors
            }
        }
        currentUserIdRef.addListenerForSingleValueEvent(eventListener) */


        //restore trip
        /*val mPrefs = this.getSharedPreferences("TripsPref", Context.MODE_PRIVATE)!!
        val tripIDs = mPrefs.getInt("TripID", 0)
        Log.i("TRIP_IDS ", tripIDs.toString())
        val gson = Gson()
        for (i in 1..tripIDs) {
            val id = "TripID$i"
            val json = mPrefs.getString(id, "")
            listOfStringTrips.add(json!!)
            Log.i("json", json)
            val tripOBJ = gson.fromJson<Trip>(json, Trip::class.java)!!
            listOfTrips.add(tripOBJ)
        }*/


        val bundle = Bundle()
        Log.i("SEARCH_TRIPS", listOfStringTrips.size.toString())
        bundle.putStringArrayList("TripsList", listOfStringTrips)
        bundle.putSerializable("PassedTrip", intent.getSerializableExtra("PostingActivity"))
        bundle.putString("toText",toText)
        bundle.putString("fromText",fromText)
        val tripsArrayList:ArrayList<Trip> = ArrayList(listOfTrips)

        //val tripResultsFragment:Fragment = TripResultFragment()
        //tripResultsFragment.arguments = bundle

        val navigationTabStrip = findViewById<NavigationTabStrip>(R.id.trip_results_tablayout)
        val fragmentAdapter = MyTripResultPageAdapter(supportFragmentManager, listOfStringTrips,toText,fromText)
        viewpager_r.adapter = fragmentAdapter
        navigationTabStrip.setViewPager(viewpager_r)
        initNavStripe(navigationTabStrip)
        // tabLayout.setupWithViewPager(viewpager_v)
        //  tabLayout.setTabTextColors(ContextCompat.getColor(this,R.color.colorPrimary), ContextCompat.getColor(this,R.color.whiteColor))


    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    private fun getData(dataSnapshot: DataSnapshot):Trip{
        val origin = dataSnapshot.child("origin").value as String?
        val destination = dataSnapshot.child("destination").value as String?
       // val stops = dataSnapshot.child("stops").value as ArrayList<String>?
        val date = dataSnapshot.child("date").value as String?
        val time = dataSnapshot.child("time").value as String?
        val vehicleModel= dataSnapshot.child("vahicleModel").value as String?
        val vehicleType =dataSnapshot.child("vehicleType").value as String?
        val vehicleColor = dataSnapshot.child("vehicleColor").value as String?
        val vehicleYear=dataSnapshot.child("vehicleYear").value as Long?
        val licensePlate = dataSnapshot.child("licensePlate").value as String?
        val luggageSizeInt =  dataSnapshot.child("luggageSize").value as String?
        val noSmoking= dataSnapshot.child("noSmoking").value as Boolean?
        val petsAllowed = dataSnapshot.child("petsAllowed").value as Boolean?
        val numberOfSeats = dataSnapshot.child("numberOfSeats").value as Long?
        val pricePerSeat= dataSnapshot.child("pricePerSeat").value as Long?
        val bookingPref = dataSnapshot.child("bookingPref").value as Long?
        val hasVehicleInfo = dataSnapshot.child("hasVehicleInfo").value as Boolean?
        val description = dataSnapshot.child("description").value as String?
        val userID = dataSnapshot.child("userID") as String?
        val carPhoto:String = ""
        var luggageSize:Int? = 0
        when (luggageSizeInt) {
            "None" -> luggageSize = 0
             "S" -> luggageSize = 1
             "M" -> luggageSize = 2
             "L" -> luggageSize = 3
        }
        val stops:ArrayList<String> = arrayListOf()
        returnedTrip = Trip(origin!!,destination!!,stops,date!!,luggageSize!!,time!!,numberOfSeats?.toInt()!!,pricePerSeat?.toInt()!!
            ,bookingPref?.toInt()!!)
        if (!hasVehicleInfo!!) {
            returnedTrip.addVehicleInfo(vehicleModel!!, vehicleType!!, vehicleColor!!, vehicleYear?.toInt()!!, licensePlate!!,carPhoto)
        }
        returnedTrip.addPreferences(noSmoking!!,petsAllowed!!)
        returnedTrip.addDescription(description!!)
        returnedTrip.addUserID(userID!!)
        return returnedTrip



    }

    private fun initNavStripe(navigationTabStrip: NavigationTabStrip) {
        navigationTabStrip.setTitles("Trips", "Requests")
        navigationTabStrip.setViewPager(viewpager_r, 0)
        navigationTabStrip.setTabIndex(0, true)
        navigationTabStrip.setBackgroundColor(Color.WHITE)
        navigationTabStrip.stripColor = Color.RED
        navigationTabStrip.stripType = NavigationTabStrip.StripType.POINT
        navigationTabStrip.stripGravity = NavigationTabStrip.StripGravity.BOTTOM
        navigationTabStrip.setTypeface("fonts/typeface.ttf")
        navigationTabStrip.animationDuration = 300
        navigationTabStrip.inactiveColor = Color.GRAY
        navigationTabStrip.activeColor = Color.RED
    }

}