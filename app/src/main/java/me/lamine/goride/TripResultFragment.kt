package me.lamine.goride

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_trip_results.*
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.ArrayList
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.FirebaseDatabase




class TripResultFragment() : Fragment() {
    private lateinit var returnedTrip:Trip
    private lateinit var database: DatabaseReference
    private val listOfTrips:MutableList<Trip> = mutableListOf()
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    private var listOfUsersLite:MutableList<LiteUser> = mutableListOf()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
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
        database = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        if (mAuth?.currentUser == null) {
            activity?.finish()
        }else {
            currentUser = mAuth?.currentUser
        }
        //pass here
        val childName = "${fromText}_$toText"
        database.child("trips").child(childName)
        val myTripQuery = database.child("trips").child(childName)
            .orderByChild("date")
        ////
        //
        //
        Log.i("MYDATE","STARTED $toText, $fromText")

        /* var myTrip:Serializable? = null
         if (bundle != null) {
            myTrip = bundle.getSerializable("PassedTrip")
         }
         val passedTrip = myTrip as Trip*/
     /*   val myTripsString = bundle?.getStringArrayList("TripsList")!!

        val listOfTrips: MutableList<Trip> = mutableListOf()
        for (json in myTripsString) {
            val gson = Gson()
            val tripOBJ = gson.fromJson<Trip>(json, Trip::class.java)
            listOfTrips.add(tripOBJ)
        }*/ //todo check added

        //val passedTrip:Trip = intent.getSerializableExtra("PostingActivity") as Trip
        //val listOfTrips:List<Trip> = listOf(passedTrip)
      //todo  Log.i("size_of_trips",listOfTrips.size.toString())

      //  trips_list_rec_view.adapter = context?.let { TripAdapter(it,listOfTrips) }
        mCheckTripInfoInServer(childName)

    }

    private fun getData(dataSnapshot: DataSnapshot):Trip{
        val origin = dataSnapshot.child("origin").value as String?
        val destination = dataSnapshot.child("destination").value as String?
        // val stops = dataSnapshot.child("stops").value as ArrayList<String>?
        val date = dataSnapshot.child("date").value as String?
        val time = dataSnapshot.child("time").value as String?
        val vehicleModel= dataSnapshot.child("vehicleModel").value as String?
        Log.i("V_Model",vehicleModel)
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
        val userID = dataSnapshot.child("userID").value as String?
        val carPhoto:String = ""
        var luggageSize:Int? = 0
        when (luggageSizeInt) {
            "None" -> luggageSize = 0
            "S" -> luggageSize = 1
            "M" -> luggageSize = 2
            "L" -> luggageSize = 3
        }
        val stops: ArrayList<String> = arrayListOf()
        returnedTrip = Trip(origin!!,destination!!,stops,date!!,luggageSize!!,time!!,numberOfSeats?.toInt()!!,pricePerSeat?.toInt()!!
            ,bookingPref?.toInt()!!)
        if (hasVehicleInfo!!) {
            returnedTrip.addVehicleInfo(vehicleModel!!, vehicleType!!, vehicleColor!!, vehicleYear?.toInt()!!, licensePlate!!,carPhoto)
        }
        returnedTrip.addPreferences(noSmoking!!,petsAllowed!!)
        returnedTrip.addDescription(description!!)
        returnedTrip.addUserID(userID!!)
        return returnedTrip



    }
    private fun fetchUsers(child: String, listener: OnGetDataListener) {
        listener.onStart()
        Log.i("Fetching...", child)
        val rootRef = FirebaseDatabase.getInstance().reference
        val currentUserIdRef = rootRef.child("users").child(child)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user:LiteUser = setupUserLite(child,dataSnapshot)
                listOfUsersLite.add(user)
                Log.i("Username is :", listOfUsersLite[0].name)
                listener.onSuccess(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException() // don't ignore errors

            }
        }
        currentUserIdRef.addListenerForSingleValueEvent(eventListener)
    }

    private fun mReadDataOnce(childName: String, listener: OnGetDataListener) {
        listener.onStart()
        val rootRef = FirebaseDatabase.getInstance().reference
        val currentUserIdRef = rootRef.child("trips").child(childName)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (ds in dataSnapshot.children) {
                    val lastDs = dataSnapshot.children.last()
                    val userIdRef = rootRef.child("trips").child(childName).child(ds.key!!)
                    val eventListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            //todo set up trips
                            val addTrip = getData(dataSnapshot)
                            listOfTrips.add(addTrip)
                            mCheckUserInfoInServer(addTrip.userID)
                           // fetchUsers(addTrip.userID,listener)
                            // val date = dataSnapshot.child("date").value as String?
                            Log.i("MY___DATE",addTrip.date)
                            Log.i("size_trips",listOfTrips.size.toString())
                            //todo
                        if (ds.value == lastDs.value) {
                            Log.i("succss",listOfTrips.size.toString())
                            listener.onSuccess(dataSnapshot)
                        }

                        }

                        override fun onCancelled(databaseError: DatabaseError) {
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
    private fun setupUserLite(childID:String,dataSnapshot: DataSnapshot):LiteUser{
        var ratings = dataSnapshot.child("ratings").value as Long?
        var peopleDriven = dataSnapshot.child("peopleDriven").value as Long?
        var fullName = dataSnapshot.child("fullName").value as String?
        var gender =dataSnapshot.child("gender").value as String?
        var birthday = dataSnapshot.child("birthday").value as String?
        Log.i("NAME","name is $fullName")
        if (ratings == null ){
            ratings = 1
        }
        if (peopleDriven == null)  {
            peopleDriven = 1
        }
        if (fullName == null){
            fullName = "RideGo User"
        }

        for (trip in listOfTrips){
       //    listOfUsersLite.add(LiteUser(trip.userID,ratings.toDouble(),peopleDriven.toInt()))
        }
        return LiteUser(childID,fullName.toString(),gender!!,birthday!!,ratings.toDouble(),peopleDriven.toInt())
    }
    private fun mCheckUserInfoInServer(child: String){
        fetchUsers(child,object : OnGetDataListener {
            override fun onStart() {
                //DO SOME THING WHEN START GET DATA HERE
                //todo add loading
            }

            override fun onSuccess(data: DataSnapshot) {
                //DO SOME THING WHEN GET DATA SUCCESS HERE
                Log.i("Username", listOfUsersLite[0].name.toString())
                Log.i("Hello","DONE!! ${listOfUsersLite[0].userId.toString()}")
                trips_list_rec_view.adapter = context?.let { TripAdapter(it,listOfTrips,listOfUsersLite)}
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
                //todo add loading
            }

            override fun onSuccess(data: DataSnapshot) {
                //DO SOME THING WHEN GET DATA SUCCESS HERE
                Log.i("size_succbus_trips",listOfTrips.size.toString())
                Log.i("ModelTrIP",listOfTrips[0].origin)
               // trips_list_rec_view.adapter = context?.let { TripAdapter(it,listOfTrips,listOfUsersLite)}
            }

            override fun onFailed(databaseError: DatabaseError) {
                //DO SOME THING WHEN GET DATA FAILED HERE
                //todo
            }
        })

    }
}