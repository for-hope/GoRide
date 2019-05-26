package me.lamine.goride.searchActivity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_trip_results.*
import me.lamine.goride.R
import me.lamine.goride.dataObjects.*
import me.lamine.goride.interfaces.OnGetDataListener
import me.lamine.goride.tripActivity.RequestsAdapter
import me.lamine.goride.tripActivity.TripAdapter
import me.lamine.goride.utils.wilayaArrayEN
import me.lamine.goride.utils.wilayaArrayFR
import java.text.SimpleDateFormat
import java.util.*

class RequestsResultFragment:Fragment() {
    private lateinit var returnedTrip: Trip
    private lateinit var database: DatabaseReference
    private val listOfTrips:MutableList<TripRequest> = mutableListOf()
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    private var isSameOriginCity:Boolean = false
    private var isSameDestinationCity:Boolean = false
    private lateinit var tsd: TripSearchData
    private var listOfUsersLite:MutableList<User> = mutableListOf()
    private var searchAgain = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_trip_results, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        this.trips_list_rec_view.setHasFixedSize(true)
        val llm = LinearLayoutManager(this.context)
        llm.orientation = RecyclerView.VERTICAL
        trips_list_rec_view.isNestedScrollingEnabled = false;
        trips_list_rec_view.layoutManager = llm
        val bundle = this.arguments
        //
        //
        ///
        ///
        //
        val toText = bundle?.getString("toText")
        val fromText = bundle?.getString("fromText")
        tsd = bundle?.getSerializable("tsd") as TripSearchData
        database = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        if (mAuth?.currentUser == null) {
            activity?.finish()
        }else {
            currentUser = mAuth?.currentUser
        }
        //pass here
        //val childName = "${fromText}_$toText"
        val childName ="${tsd.originCode}_${tsd.destinationCode}"
        //database.child("trips").child(childName)
        //database.child("trips").child(childName).orderByChild("date").equalTo("lol")

        if (wilayaArrayEN[tsd.originCode-1] == tsd.originSubCity || wilayaArrayFR[tsd.originCode-1] == tsd.originSubCity){

            isSameOriginCity = true
        }
        if (wilayaArrayEN[tsd.destinationCode-1] == tsd.destSubCity || wilayaArrayFR[tsd.destinationCode-1] == tsd.destSubCity){
            isSameDestinationCity = true
        }
        ////
        //
        mCheckTripInfoInServer(childName)
    }
    private fun fetchUsers(child: String, listener: OnGetDataListener) {
        listener.onStart()
        Log.i("Fetching...", child)
        val rootRef = FirebaseDatabase.getInstance().reference
        val currentUserIdRef = rootRef.child("users").child(child)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listener.onSuccess(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException() // don't ignore errors

            }
        }
        currentUserIdRef.addListenerForSingleValueEvent(eventListener)
    }
    private fun setPb(visibility: Int){
        val mProgressBar = activity!!.findViewById<ProgressBar>(R.id.pb_searcn)
        val mLayout = activity!!.findViewById<LinearLayout>(R.id.grey_search_layout)
        if (visibility == 1) {
            mProgressBar.visibility = View.VISIBLE
            mLayout.visibility = View.VISIBLE

        } else {
            mProgressBar.visibility = View.GONE
            mLayout.visibility = View.GONE

        }
    }
    private fun mReadDataOnce(childName: String, listener: OnGetDataListener) {
        listener.onStart()
        val rootRef = FirebaseDatabase.getInstance().reference
        val currentUserIdRef:Any
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        currentUserIdRef = if (!searchAgain){
            rootRef.child("tripRequests").child(childName).orderByChild("destSubCity").startAt(tsd.destSubCity)
        } else {
            rootRef.child("tripRequests").child(childName).orderByChild("destSubCity").startAt(wilayaArrayEN[tsd.destinationCode-1])  //todo
        }

        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.childrenCount.toInt() == 0){
                    if (!searchAgain){
                        listener.onSuccess(dataSnapshot)
                    } else {
                        mCheckUserInfoInServer(null, noTrip = true, isFinalChild = true)
                    }
                }
                for (ds in dataSnapshot.children) {
                    val lastDs = dataSnapshot.children.last()
                    val userIdRef = rootRef.child("tripRequests").child(childName).child(ds.key!!)
                    val eventListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            var isRepeated = false
                            val addTrip = dataSnapshot.getValue(TripRequest::class.java)
                            //check for repeated trips
                            for (mTrip in listOfTrips){
                                if (mTrip.tripID == addTrip?.tripID!!){
                                    isRepeated = true
                                    break
                                }
                            }
                            //if its not repeated
                            if (!isRepeated) {
                                val dateAndTime = "${addTrip?.date} ${addTrip?.time}"
                                val tripDate = sdf.parse(dateAndTime)
                                //check if its after today
                                if (tripDate.after(Date())){
                                    //add trip
                                    mCheckUserInfoInServer(addTrip, noTrip = false, isFinalChild = false)
                                }
                            }
                            //check if it's the last element.
                            if (ds.value == lastDs.value) {
                                //check if its the same or we searched again
                                if (isSameDestinationCity || searchAgain){
                                    val dateAndTime = "${addTrip?.date} ${addTrip?.time}"
                                    val tripDate = sdf.parse(dateAndTime)
                                    //check if its after today
                                    if (tripDate.after(Date())) {
                                        mCheckUserInfoInServer(addTrip, noTrip = false, isFinalChild = true)
                                    } else {
                                        mCheckUserInfoInServer(addTrip, noTrip = true, isFinalChild = true)
                                    }
                                    //   listener.onSuccess(dataSnapshot)
                                } else {
                                    searchAgain = true
                                    mCheckTripInfoInServer(childName)

                                }

                            }

                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            listener.onFailed(databaseError)
                            throw databaseError.toException() // don't ignore errors

                        }
                    }
                    userIdRef.addListenerForSingleValueEvent(eventListener)
                }
                Log.i("size_succxcctrips",listOfTrips.size.toString())

            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException()
                // don't ignore errors
            }
        }
        currentUserIdRef.addListenerForSingleValueEvent(eventListener)
    }
    private fun mCheckUserInfoInServer(trip:TripRequest?,noTrip:Boolean, isFinalChild:Boolean){
        var child = ""
        if (trip != null){
            child = trip.userID
        }
        fetchUsers(child,object : OnGetDataListener {
            override fun onStart() {
                setPb(1)
            }

            override fun onSuccess(data: DataSnapshot) {
                //DO SOME THING WHEN GET DATA SUCCESS HERE
                if (!noTrip){
                    val mUser = data.getValue(User::class.java)
                    trip?.poster = mUser
                    if (!noTrip){
                        listOfTrips.add(trip!!)
                    }
                } else if (isFinalChild){
                    Toast.makeText(this@RequestsResultFragment.context,"DONE", Toast.LENGTH_SHORT).show()
                    val sortedList = listOfTrips.sortedWith(compareBy {it.date})
                    for (item in sortedList){
                        listOfUsersLite.add(item.poster!!)
                    }
                    setPb(0)
                    trips_list_rec_view.adapter = context?.let {
                        RequestsAdapter(
                            it,
                            sortedList,
                            listOfUsersLite
                        )
                    }
                }

            }

            override fun onFailed(databaseError: DatabaseError) {
                //DO SOME THING WHEN GET DATA FAILED HERE
                //todo
            }
        })


    }
    private fun mCheckTripInfoInServer(child: String) {
        mReadDataOnce(child, object : OnGetDataListener {
            override fun onStart() {
                //DO SOME THING WHEN START GET DATA HERE
                setPb(1)
            }

            override fun onSuccess(data: DataSnapshot) {
                //DO SOME THING WHEN GET DATA SUCCESS HERE

                Toast.makeText(this@RequestsResultFragment.context,"DONE", Toast.LENGTH_SHORT).show()
                val sortedList = listOfTrips.sortedWith(compareBy {it.date})
                for (item in sortedList){
                    listOfUsersLite.add(item.poster!!)
                }
                trips_list_rec_view.adapter = context?.let {
                    RequestsAdapter(
                        it,
                        sortedList,
                        listOfUsersLite
                    )
                }
                setPb(0)
            }

            override fun onFailed(databaseError: DatabaseError) {
                //DO SOME THING WHEN GET DATA FAILED HERE
                //todo
            }
        })

    }
}