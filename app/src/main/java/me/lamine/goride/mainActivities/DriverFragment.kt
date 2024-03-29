package me.lamine.goride.mainActivities

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_driver.*
import me.lamine.goride.R
import me.lamine.goride.dataObjects.Trip
import me.lamine.goride.dataObjects.User
import me.lamine.goride.interfaces.OnGetDataListener
import me.lamine.goride.tripActivity.TripAdapter
import me.lamine.goride.utils.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class DriverFragment : androidx.fragment.app.Fragment() {
    private lateinit var database: DatabaseReference
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    private var listOfCurrentTrips:MutableList<Trip> = mutableListOf()
    private var listUser:MutableList<User> = mutableListOf()
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        database = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        if (mAuth?.currentUser == null) {
            Toast.makeText(context,"You're not logged in.", Toast.LENGTH_SHORT).show()
            this.activity?.finish()
        }else {
            currentUser = mAuth?.currentUser
          //  getUserTrips()
            getUserTrips()
        }
        this.driver_trips.setHasFixedSize(true)
        val llm = LinearLayoutManager(this.context)
        llm.orientation = RecyclerView.VERTICAL

        driver_trips.layoutManager = llm
       // getSharedTrips()
        if (!pullToRefreshDriverTrips.isRefreshing){
        pullToRefreshDriverTrips.setOnRefreshListener {
            Toast.makeText(this.context,"Refreshed",Toast.LENGTH_SHORT).show()
            getUserTrips() }
        }
       // getUserTrips()

    }

    override fun onResume() {
        super.onResume()
   /*     if (mAuth?.currentUser != null) {

                        listOfCurrentTrips = mutableListOf()
                        listUser = mutableListOf()
                        getUserTrips()
        }*/


    }
    private fun showTripEmptyDialog(trip: Trip){
        val tripDest:String = wilayaArrayEN[decodeWilaya(trip.destCity)-1]
        AlertDialog.Builder(this.activity!!)
            .setTitle("Empty Trip")
            .setMessage("Your trip's deadline to $tripDest has ended, sadly no one joined.")

            // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton("Okay") { dialog, which ->
                Toast.makeText(activity?.applicationContext!!,"Good luck.", Toast.LENGTH_SHORT).show()
                dialog.cancel()
                val path = "users/${Database().currentUserId()}"
                Database().removeFromPath("$path/activeTrips/${trip.tripID}")
               // Database().removeFromPath()

            }
            // A null listener allows the button to dismiss the dialog and take no further action.
            //.setNegativeButton("Done!", null)
            .setIcon(R.drawable.ic_info_gray_24dp)
            .show()
    }
    private fun showTripDoneDialog(trip: Trip){
        val tripDest:String = wilayaArrayEN[decodeWilaya(trip.destCity)-1]
        AlertDialog.Builder(this.activity!!)
            .setTitle("Trip is done!")
            .setMessage("Your trip to $tripDest is done, did you make it?")

            // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton("Yes") { dialog, which ->
                addTripStats(trip)
                Toast.makeText(activity?.applicationContext!!,"Keep it up!", Toast.LENGTH_SHORT).show()
                dialog.cancel()

            }
            .setNegativeButton("No") { dialog, which ->
                Toast.makeText(activity?.applicationContext!!,"Too bad, feedback reported.", Toast.LENGTH_SHORT).show()
                dialog.cancel()
            }

            // A null listener allows the button to dismiss the dialog and take no further action.
            //.setNegativeButton("Done!", null)
            .setIcon(R.drawable.ic_done_green_24dp)
            .show()
    }
    private fun addTripStats(trip: Trip){
        val nbTrips = listUser[0].tripsTraveled + 1
        val pplDriven = listUser[0].peopleDriven + trip.bookedUsers.size
        val path = "users/${Database().currentUserId()}"
        Database().addToPath("$path/tripsTraveled",nbTrips)
        Database().addToPath("$path/peopleDriven",pplDriven)
        Database().removeFromPath("$path/activeTrips/${trip.tripID}")

    }
    private fun isTripDone(trip:Trip):Int{
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        val dateAndTime = "${trip.date} ${trip.time}"
        val tripDate = sdf.parse(dateAndTime)

        return if (trip.bookedUsers.size > 0 && tripDate.before(Date())){
            1
        } else if (trip.bookedUsers.size == 0 && tripDate.before(Date()) ){
            0
        } else {
            -1
        }
    }
    private fun checkEndedTrips(){
        val listOfTripsToRemove = mutableListOf<Trip>()
        for(trip in listOfCurrentTrips){
           if (isTripDone(trip) == 1){
               listOfTripsToRemove.add(trip)
               showTripDoneDialog(trip)
           } else if (isTripDone(trip )== 0){
               listOfTripsToRemove.add(trip)
              showTripEmptyDialog(trip)
           }

        }
        listOfCurrentTrips.removeAll(listOfTripsToRemove)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_driver, container, false)

    }

    private fun getUser(){
        Database().fetchUser(Database().currentUserId(),object : OnGetDataListener {
            override fun onStart() {
               setPb(null,pb_driver,greyout_driver,1)
            }
            override fun onSuccess(data: DataSnapshot) {
                //DO SOME THING WHEN GET DATA SUCCESS HERE
                listUser = mutableListOf()
                val user  = data.getValue(me.lamine.goride.dataObjects.User::class.java)
                for (trip in listOfCurrentTrips) {
                    listUser.add(user!!)
                }
                setAdapter()
                checkEndedTrips()

                }

            override fun onFailed(databaseError: DatabaseError) {

            }
        })

    }
    private fun setAdapter(){
        val tripIDs = mutableListOf<String>()
        val tripsToRemove = mutableListOf<Int>()
        if (listOfCurrentTrips.size > 1){
        for ((index,trip) in listOfCurrentTrips.withIndex()){
            if (tripIDs.contains(trip.tripID)){
                tripsToRemove.add(index)
            }
            tripIDs.add(trip.tripID)
        }
        for (id in tripsToRemove){
            if (id<tripsToRemove.size && id > -1){
            listOfCurrentTrips.removeAt(id)
            }
        }
        }

        if (listOfCurrentTrips.isNotEmpty()){
            Log.i(listOfCurrentTrips.size.toString(), listUser.size.toString())
            driver_trips.adapter?.notifyDataSetChanged()
            Log.i("adapter_size", "${listOfCurrentTrips.size}")
            setPb(null,pb_driver,greyout_driver,0)
            driver_empty_layout.visibility = View.GONE
            relativeLayout.visibility = View.VISIBLE
            driver_trips.adapter = this.context?.let {
                TripAdapter(
                    it,
                    listOfCurrentTrips,listUser
                )
            }
        }
    }

    private  fun getTripsList(tripIDs:List<String>,listener: OnGetDataListener){
        listener.onStart()
        val tripsRef = database.child("trips")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (trip in dataSnapshot.children){
                    for (tripID in tripIDs){
                        if (trip.child(tripID).exists()){
                            val mTrip = trip.child(tripID).getValue(Trip::class.java)
                            listOfCurrentTrips.add(mTrip!!)
                        }
                    }

                }
                listener.onSuccess(dataSnapshot)
            }

            override fun onCancelled(p0: DatabaseError) {
                listener.onFailed(p0)
            }
        }
        tripsRef.addListenerForSingleValueEvent(eventListener)


    }
    private fun fetchTrips(tripIDs: List<String>){
        getTripsList(tripIDs,object : OnGetDataListener{
            override fun onStart() {
                Log.i("fetchTrips","onStart")
            }

            override fun onSuccess(data: DataSnapshot) {
                getUser()
            }

            override fun onFailed(databaseError: DatabaseError) {
                Log.i("fetchTrips", "onFailed")
                Toast.makeText(this@DriverFragment.context,"Database Error.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getUserTrips(){
        listOfCurrentTrips = arrayListOf()
        listUser = arrayListOf()
        Database().fetchFromCurrentUser("activeTrips",object : OnGetDataListener{
            override fun onStart() {
              setPb(null,pb_driver,greyout_driver,1)
            }

            override fun onSuccess(data: DataSnapshot) {
                pullToRefreshDriverTrips.isRefreshing = false
                if ( data.childrenCount.toInt() > 0 ){
                    driver_empty_layout.visibility = View.GONE
                    val tripIDs:MutableList<String> = mutableListOf()
                    for (child in data.children){
                        if (!tripIDs.contains(child.key.toString())){
                        tripIDs.add(child.key.toString())
                        }
                        Log.i("TripsToAdd:", child.key.toString())
                    }
                    fetchTrips(tripIDs)
                } else {
                    setPb(driver_empty_layout,pb_driver,greyout_driver,0)
                    relativeLayout.visibility = View.GONE
                }

            }

            override fun onFailed(databaseError: DatabaseError) {
                Log.i("getUserTrips","onFailed")
            }
        })
    }

}