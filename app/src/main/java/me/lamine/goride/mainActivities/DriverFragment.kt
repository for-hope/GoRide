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
import me.lamine.goride.utils.decodeWilaya
import me.lamine.goride.utils.wilayaArrayEN
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
            getUserTrips()
        }
        this.driver_trips.setHasFixedSize(true)
        val llm = LinearLayoutManager(this.context)
        llm.orientation = RecyclerView.VERTICAL
        driver_trips.isNestedScrollingEnabled = false;
        driver_trips.layoutManager = llm
       // getSharedTrips()
        pullToRefreshDriverTrips.setOnRefreshListener {
            getUserTrips()
            pullToRefreshDriverTrips.isRefreshing = false;}
       // getUserTrips()

    }
    private fun setPb(visibility: Int){

        if (visibility == 1) {
            //trip_ac_submit_btn.visibility = View.GONE
            //trip_ac_submit_btn.visibility = View.GONE
            driver_empty_layout.visibility = View.GONE
            pb_driver.visibility = View.VISIBLE
            greyout_driver.visibility = View.VISIBLE

        } else {
            driver_empty_layout.visibility = View.VISIBLE
            pb_driver.visibility = View.GONE
            greyout_driver.visibility = View.GONE
            // trip_ac_submit_btn.visibility = View.VISIBLE

        }
    }
    private fun getSharedTrips(){
        listUser = arrayListOf()
        listOfCurrentTrips= arrayListOf()
        val mPrefs = this.activity?.getSharedPreferences("TripsPref", Context.MODE_PRIVATE)!!
        var nbTrips = mPrefs.getInt("savedTrips",0)

        ///
        val gson = Gson()
        while (nbTrips >= 0){
            val json = mPrefs.getString("TripID$nbTrips", "")
            if (json != ""){
                val trip = gson.fromJson<Trip>(json, Trip::class.java)
                listOfCurrentTrips.add(trip)
            }
            nbTrips -= 1
        }
        for (trip in listOfCurrentTrips) {
            val json = mPrefs.getString("currentUser", "")
            if (json != ""){
                val user = gson.fromJson<User>(json, User::class.java)
                listUser.add(user)
            }

        }
        if (listOfCurrentTrips.isNotEmpty()){
            setAdapter()
        }


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
        //todo DONT delete trip
        val nbTrips = listUser[0].tripsTraveled + 1
        val pplDriven = listUser[0].peopleDriven + trip.bookedUsers.size
        val newRef = database.child("users").child(currentUser?.uid!!)
        newRef.child("tripsTraveled").setValue(nbTrips){ databaseError, _ ->
            if (databaseError != null) {
                Log.i("FireBaseEroor",databaseError.message)
                Toast.makeText(activity?.applicationContext!!, "Error $databaseError", Toast.LENGTH_LONG).show()}
        }
       newRef.child("peopleDriven").setValue(pplDriven){ databaseError, _ ->
               if (databaseError != null) {
                   Log.i("FireBaseEroor",databaseError.message)
                   Toast.makeText(activity?.applicationContext!!, "Error $databaseError", Toast.LENGTH_LONG).show()}

       }
        val userRef = database.child("users").child(currentUser?.uid!!).child("activeTrips").child(trip.tripID)
        userRef.removeValue()
        database.push()


    }
    private fun isTripDone(trip:Trip):Boolean{
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        val dateAndTime = "${trip.date} ${trip.time}"
        val tripDate = sdf.parse(dateAndTime)

        return trip.bookedUsers.size > 0 && tripDate.before(Date())
    }
    private fun checkEndedTrips(){
        val listOfTripsToRemove = mutableListOf<Trip>()
        for(trip in listOfCurrentTrips){
           if (isTripDone(trip)){
               listOfTripsToRemove.add(trip)
               showTripDoneDialog(trip)
           }
        }
        listOfCurrentTrips.removeAll(listOfTripsToRemove)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_driver, container, false)

    }

    private fun getUser(){
        getUserInfo(object : OnGetDataListener {
            override fun onStart() {
               setPb(1)
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
                setPb(0)
                }

            override fun onFailed(databaseError: DatabaseError) {

            }
        })

    }
    private fun getUserInfo(listener: OnGetDataListener){
        listener.onStart()

        val newRef = database.child("users").child(currentUser?.uid!!)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listener.onSuccess(dataSnapshot)
               // setAdapter()
               // checkEndedTrips()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException() as Throwable // don't ignore errors
            }
        }
        newRef.addListenerForSingleValueEvent(eventListener)
    }
    private fun setAdapter(){
        if (listOfCurrentTrips.isNotEmpty()){
            Log.i(listOfCurrentTrips.size.toString(), listUser.size.toString())
            driver_trips.adapter?.notifyDataSetChanged()
            scroll_driver_frg.visibility = View.VISIBLE
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
            }
        })
    }

    private fun getUserTrips(){
        listOfCurrentTrips = arrayListOf()
        listUser = arrayListOf()
        findUserTrips(object : OnGetDataListener{
            override fun onStart() {
                setPb(1)
            }

            override fun onSuccess(data: DataSnapshot) {
                if ( data.childrenCount.toInt() > 0 ){
                    val tripIDs:MutableList<String> = mutableListOf()
                    for (child in data.children){
                        tripIDs.add(child.key.toString())
                    }
                    fetchTrips(tripIDs)
                }

            }

            override fun onFailed(databaseError: DatabaseError) {
                Log.i("getUserTrips","onFailed")
            }
        })
    }
    private fun findUserTrips(listener: OnGetDataListener){
        listener.onStart()
        val userRef = database.child("users").child(currentUser?.uid!!).child("activeTrips")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listener.onSuccess(dataSnapshot)

            }
            override fun onCancelled(error: DatabaseError) {
                listener.onFailed(error)
            }
        }
        userRef.addListenerForSingleValueEvent(eventListener)
    }
}