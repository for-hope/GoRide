package me.lamine.goride.mainActivities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_passenger.*
import me.lamine.goride.R
import me.lamine.goride.dataObjects.Trip
import me.lamine.goride.dataObjects.User
import me.lamine.goride.interfaces.OnGetDataListener
import me.lamine.goride.reviewActivity.ReviewActivity
import me.lamine.goride.tripActivity.TripAdapter
import me.lamine.goride.utils.decodeWilaya
import me.lamine.goride.utils.wilayaArrayEN
import java.text.SimpleDateFormat
import java.util.*
import com.google.gson.Gson
import me.lamine.goride.dataObjects.TripRequest
import me.lamine.goride.tripActivity.RequestsAdapter
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 */
class PassengerFragment : androidx.fragment.app.Fragment() {
    private var listOfCurrentTrips:MutableList<Trip> = mutableListOf()
    private var listUser:MutableList<User> = mutableListOf()
    private lateinit var database: DatabaseReference
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    private var listOfCurrentRequest:MutableList<TripRequest> = mutableListOf()
    private var requestIdHashMap:HashMap<String,String> = hashMapOf()
    private var listOfUserR:MutableList<User> = mutableListOf()
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.findViewById<TextView>(R.id.click_here_p_textview_)
            ?.setOnClickListener { Toast.makeText(this.context,"Post a request",Toast.LENGTH_SHORT).show() }

        database = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        if (mAuth?.currentUser == null) {
            Toast.makeText(context,"You're not logged in.", Toast.LENGTH_SHORT).show()
            this.activity?.finish()
        }else {
            currentUser = mAuth?.currentUser
            getUserTrips()
            getTripRequests()
        }
        this.pass_trips.setHasFixedSize(true)
        this.req_trips.setHasFixedSize(true)
        val llm = LinearLayoutManager(this.context)
        val llm2 = LinearLayoutManager(this.context)
        llm.orientation = RecyclerView.VERTICAL
        llm2.orientation = RecyclerView.VERTICAL
        req_trips.isNestedScrollingEnabled = false
        pass_trips.isNestedScrollingEnabled = false
        req_trips.layoutManager = llm2
        pass_trips.layoutManager = llm
    }


    private fun fetchTripRequests(listener: OnGetDataListener){
        listener.onStart()
        val ref = database.child("users").child(currentUser?.uid!!).child("activeTripRequests")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listener.onSuccess(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException() as Throwable // don't ignore errors
            }
        }
        ref.addListenerForSingleValueEvent(eventListener)

    }
    private fun setRequestAdapter(){
        if (listOfCurrentRequest.isNotEmpty()){
            Log.i("PassengerFragement", "FULL LIST")
            req_trips.adapter?.notifyDataSetChanged()
            scroll_pass_frg.visibility = View.VISIBLE
            req_trips.adapter = this.context?.let {
                RequestsAdapter(
                    it,
                   listOfCurrentRequest,listOfUserR
                )
            }
        } else {
            Log.i("PassengerFragement", "EMPTY LIST")
        }

    }
    private fun fetchRequestObjects(key:String,value:String,listener: OnGetDataListener){
        listener.onStart()
        val ref = database.child("tripRequests").child(value).child(key)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listener.onSuccess(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException() as Throwable // don't ignore errors
            }
        }
        ref.addListenerForSingleValueEvent(eventListener)
    }
    private fun getRequestObjects(){
        var hashSize = requestIdHashMap.size
        val user = getCurrentUserObj()

      for (id in requestIdHashMap) {
          fetchRequestObjects(id.key, id.value, object : OnGetDataListener {
              override fun onStart() {
                  setPb(1)
              }

              override fun onSuccess(data: DataSnapshot) {

                  hashSize -= 1
                  val tripRequest = data.getValue(TripRequest::class.java)
                  listOfUserR.add(user!!)
                  listOfCurrentRequest.add(tripRequest!!)
                  if (hashSize == 0){
                      setPb(0)
                      setRequestAdapter()
                  }
              }

              override fun onFailed(databaseError: DatabaseError) {
                  Toast.makeText(this@PassengerFragment.context,"Failed",Toast.LENGTH_SHORT).show()
              }

          })
      }
    }
    private fun getTripRequests(){
        fetchTripRequests(object : OnGetDataListener{
            override fun onStart() {
                setPb(1)
            }

            override fun onSuccess(data: DataSnapshot) {

               if (data.exists()){
                   Log.i("PassengerFragement", "EMPTY LIST2")
                   for (requestData in data.children){
                       val key = requestData.key
                       val value = requestData.value as String
                       requestIdHashMap[key!!] = value
                       if (requestData.key == data.children.last().key){
                           Log.i("PassengerFragement", "EMPTY LIST4")
                        getRequestObjects()
                       }

                   }


               } else {
                   setPb(0)
               }
            }

            override fun onFailed(databaseError: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })

    }


    private fun setPb(visibility: Int){

        if (visibility == 1) {
            //trip_ac_submit_btn.visibility = View.GONE
            //trip_ac_submit_btn.visibility = View.GONE
            pass_empty_layout.visibility = View.GONE
            pb_passenger.visibility = View.VISIBLE
            greyout_pass.visibility = View.VISIBLE

        } else {
            pass_empty_layout.visibility = View.VISIBLE
            pb_passenger.visibility = View.GONE
            greyout_pass.visibility = View.GONE
            // trip_ac_submit_btn.visibility = View.VISIBLE

        }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_passenger, container, false)

    }
    private fun setAdapter(){
        if (listOfCurrentTrips.isNotEmpty()){
            pass_trips.adapter?.notifyDataSetChanged()
            scroll_pass_frg.visibility = View.VISIBLE
            pass_trips.adapter = this.context?.let {
                TripAdapter(
                    it,
                    listOfCurrentTrips,listUser
                )
            }
        } else {
            pass_empty_layout.visibility = View.VISIBLE
            scroll_pass_frg.visibility = View.GONE

        }
    }
    private fun getUserInfo(index: Int,listener: OnGetDataListener){
        listener.onStart()

        var newRef = database.child("users")

        val trip =listOfCurrentTrips[index]
         newRef = newRef.child(trip.userID)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    listUser.add(dataSnapshot.getValue(User::class.java)!!)
                    listener.onSuccess(dataSnapshot)
                } else {
                    Log.i("PassengerFrag","DOESNT EXIST")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException() as Throwable // don't ignore errors
            }
        }
            newRef.addListenerForSingleValueEvent(eventListener)


    }
    private fun addTripStats(trip: Trip){
        val user = getCurrentUserObj()
        val nbTrips = user?.tripsAsPassenger!! + 1
        val newRef = database.child("users").child(currentUser?.uid!!)
        newRef.child("tripsAsPassenger").setValue(nbTrips){ databaseError, _ ->
            if (databaseError != null) {
                Log.i("FireBaseEroor",databaseError.message)
                Toast.makeText(activity?.applicationContext!!, "Error $databaseError", Toast.LENGTH_LONG).show()}
        }
        newRef.child("bookedTrips").child(trip.tripID).removeValue()
        newRef.child("driversGoneWith").child(trip.userID).setValue(1)
        //newRef.child("bookedTrips").child(trip.tripID).removeValue()
        database.push()
    }
    private fun getCurrentUserObj():User?{
        val mPrefs = this.activity?.getSharedPreferences("TripsPref", Context.MODE_PRIVATE)!!
        val currentUserStr = mPrefs.getString("currentUser","")
        val gson = Gson()
        return gson.fromJson(currentUserStr, User::class.java)
    }
    private fun showReviewDialog(trip: Trip){
        AlertDialog.Builder(this.activity!!)
            .setTitle("Review Driver")
            .setMessage("Wanna tell us how was your trip?")

            // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton("Yes") { dialog, which ->
                val intent = Intent(this.activity,ReviewActivity::class.java)
                intent.putExtra("userID",trip.userID)
                startActivity(intent)
                dialog.cancel()

            }
            .setNegativeButton("No") { dialog, which ->
                Toast.makeText(activity?.applicationContext!!,"You can still review users on their profiles.", Toast.LENGTH_SHORT).show()
                dialog.cancel()
            }

            // A null listener allows the button to dismiss the dialog and take no further action.
            //.setNegativeButton("Done!", null)
            .setCancelable(false)
            .setIcon(R.drawable.ic_rate_review_black_24dp)
            .show()

    }
    private fun showTripDoneDialog(trip: Trip){
        val tripDest:String = wilayaArrayEN[decodeWilaya(trip.destCity) -1]
        AlertDialog.Builder(this.activity!!)
            .setTitle("Trip is done!")
            .setMessage("Your trip to $tripDest is done, did you make it?")

            // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton("Yes") { dialog, which ->
                addTripStats(trip)
                Toast.makeText(activity?.applicationContext!!,"Keep it up!", Toast.LENGTH_SHORT).show()
                showReviewDialog(trip)
                dialog.cancel()

            }
            .setNegativeButton("No") { dialog, which ->
                Toast.makeText(activity?.applicationContext!!,"Something wrong? send us a feedback!", Toast.LENGTH_SHORT).show()
                addTripStats(trip)
                dialog.cancel()
            }

            // A null listener allows the button to dismiss the dialog and take no further action.
            //.setNegativeButton("Done!", null)
            .setCancelable(false)
            .setIcon(R.drawable.ic_done_green_24dp)
            .show()
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
    private fun getUser(index:Int){
        getUserInfo(index,object : OnGetDataListener {
            override fun onStart() {
               setPb(1)
            }
            override fun onSuccess(data: DataSnapshot) {
                //DO SOME THING WHEN GET DATA SUCCESS HERE

                if (index==listOfCurrentTrips.size-1){
                    checkEndedTrips()
                    setAdapter()
                    setPb(0)
                } else{
                    getUser(index+1)
                }

               // checkEndedTrips()

            }

            override fun onFailed(databaseError: DatabaseError) {

            }
        })

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

                getUser(0)
            }

            override fun onFailed(databaseError: DatabaseError) {
                Log.i("fetchTrips", "onFailed")
            }
        })
    }
    private fun findUserTrips(listener: OnGetDataListener){
        listener.onStart()
        val userRef = database.child("users").child(currentUser?.uid!!).child("bookedTrips")
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
    private fun getUserTrips(){
        listOfCurrentTrips = arrayListOf()
        listUser = arrayListOf()
        findUserTrips(object : OnGetDataListener {
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
                } else {
                   // setPb(0)
                }

            }

            override fun onFailed(databaseError: DatabaseError) {
                Log.i("getUserTrips","onFailed")
            }
        })
    }
}