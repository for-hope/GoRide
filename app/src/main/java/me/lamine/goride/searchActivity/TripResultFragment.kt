package me.lamine.goride.searchActivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_trip_results.*
import android.util.Log
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.FirebaseDatabase
import me.lamine.goride.R
import me.lamine.goride.dataObjects.LiteUser
import me.lamine.goride.dataObjects.Trip
import me.lamine.goride.dataObjects.TripSearchData
import me.lamine.goride.dataObjects.User
import me.lamine.goride.interfaces.OnGetDataListener
import me.lamine.goride.tripActivity.TripAdapter
import me.lamine.goride.utils.wilayaArrayEN
import me.lamine.goride.utils.wilayaArrayFR
import java.util.*


class TripResultFragment() : Fragment() {
    private lateinit var returnedTrip: Trip
    private lateinit var database: DatabaseReference
    private val listOfTrips:MutableList<Trip> = mutableListOf()
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    private var isSameOriginCity:Boolean = false
    private var isSameDestinationCity:Boolean = false
    private lateinit var tsd: TripSearchData
    private var listOfUsersLite:MutableList<LiteUser> = mutableListOf()
    private var searchAgain = false
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
        Log.i("CITY 1", wilayaArrayEN[tsd.originCode-1])
        Log.i("CITY",  tsd.originSubCity)
        if (wilayaArrayEN[tsd.originCode-1] == tsd.originSubCity || wilayaArrayFR[tsd.originCode-1] == tsd.originSubCity){

            isSameOriginCity = true
        }
        if (wilayaArrayEN[tsd.destinationCode-1] == tsd.destSubCity || wilayaArrayFR[tsd.destinationCode-1] == tsd.destSubCity){
            isSameDestinationCity = true
        }
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

    private fun getData(dataSnapshot: DataSnapshot): Trip {
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
        val carPhoto:String? = dataSnapshot.child("carPhoto").value as String?
        val originCity = dataSnapshot.child("originCity").value as String?
        val originSubCity = dataSnapshot.child("originSubCity").value as String?
        val originFullAddress = dataSnapshot.child("originFullAddress").value as String?
        val destCity = dataSnapshot.child("destCity").value as String?
        val destSubCity = dataSnapshot.child("destSubCity").value as String?
        val destFullAddress = dataSnapshot.child("destFullAddress").value as String?
        val tripID = dataSnapshot.key
        //val tripID = dataSnapshot.child("tripID").value as String?
        var luggageSize:Int? = 0
        when (luggageSizeInt) {
            "None" -> luggageSize = 0
            "S" -> luggageSize = 1
            "M" -> luggageSize = 2
            "L" -> luggageSize = 3
        }
        val stops: ArrayList<String> = arrayListOf()
        returnedTrip = Trip(
            origin!!,
            destination!!,
            stops,
            date!!,
            luggageSize!!,
            time!!,
            numberOfSeats?.toInt()!!,
            pricePerSeat?.toInt()!!
            ,
            bookingPref?.toInt()!!
        )
        if (hasVehicleInfo!!) {
            returnedTrip.addVehicleInfo(vehicleModel!!, vehicleType!!, vehicleColor!!, vehicleYear?.toInt()!!, licensePlate!!,carPhoto!!)
        }
        returnedTrip.tripID = tripID!!
        returnedTrip.originCity = originCity!!
        returnedTrip.originSubCity = originSubCity!!
        returnedTrip.originFullAddress = originFullAddress!!
        returnedTrip.destCity = destCity!!
        returnedTrip.destSubCity = destSubCity!!
        returnedTrip.destFullAddress = destFullAddress!!
        returnedTrip.addPreferences(noSmoking!!,petsAllowed!!)
        returnedTrip.addDescription(description!!)
        returnedTrip.addUserID(userID!!)
        return returnedTrip



    }
    private fun fetchUsers(child: String,isFinalChild: Boolean, listener: OnGetDataListener) {
        listener.onStart()
        Log.i("Fetching...", child)
        val rootRef = FirebaseDatabase.getInstance().reference
        val currentUserIdRef = rootRef.child("users").child(child)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user: LiteUser = setupUserLite(child,dataSnapshot)
                val mUser = dataSnapshot.getValue(User::class.java)
                listOfUsersLite.add(user)
                Log.i("Username is :", listOfUsersLite[0].name)
                if (isFinalChild){
                    listener.onSuccess(dataSnapshot)
                }

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
        Log.i("OPTiOPN", searchAgain.toString() +""+ isSameOriginCity.toString())
        currentUserIdRef = if (!searchAgain){
            rootRef.child("trips").child(childName).orderByChild("destSubCity").startAt(tsd.destSubCity)
           //todo
        } else {
            rootRef.child("trips").child(childName).orderByChild("destSubCity").startAt(wilayaArrayEN[tsd.destinationCode])  //todo
        }

        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.childrenCount.toInt() == 0){
                    listener.onSuccess(dataSnapshot)
                }
                for (ds in dataSnapshot.children) {
                    val lastDs = dataSnapshot.children.last()
                    val childrenNb = dataSnapshot.childrenCount
                    val userIdRef = rootRef.child("trips").child(childName).child(ds.key!!)
                    val eventListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            var isRepeated = false
                            val addTrip = getData(dataSnapshot)
                            for (mTrip in listOfTrips){
                                if (mTrip.tripID == addTrip.tripID){
                                    isRepeated = true
                                    break
                                }
                            }
                            if (!isRepeated) {
                                listOfTrips.add(addTrip)
                            }





                        if (ds.value == lastDs.value) {
                            if (isSameDestinationCity || searchAgain){
                                mCheckUserInfoInServer(addTrip.userID,true)
                                Log.i("BOOL",isSameOriginCity.toString())
                             //   listener.onSuccess(dataSnapshot)
                            } else {

                                searchAgain = true
                                mCheckUserInfoInServer(addTrip.userID,false)
                                mCheckTripInfoInServer(childName)

                            }

                        } else{
                            mCheckUserInfoInServer(addTrip.userID,false)
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
    private fun setupUserLite(childID:String,dataSnapshot: DataSnapshot): LiteUser {
        var ratings = dataSnapshot.child("ratings").value as Long?
        var peopleDriven = dataSnapshot.child("peopleDriven").value as Long?
        var fullName = dataSnapshot.child("fullName").value as String?
        val gender =dataSnapshot.child("gender").value as String?
        val birthday = dataSnapshot.child("birthday").value as String?
        val profilePic:String? = dataSnapshot.child("profilePic").value as String?
        Log.i("PFP",profilePic.toString() + " pfp")
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
        val liteUser = LiteUser(
            childID,
            fullName.toString(),
            gender!!,
            birthday!!,
            ratings.toDouble(),
            peopleDriven.toInt()
        )
        liteUser.profilePic = profilePic!!
        return liteUser
    }
    private fun mCheckUserInfoInServer(child: String, isFinalChild:Boolean){
        fetchUsers(child,isFinalChild,object : OnGetDataListener {
            override fun onStart() {
                setPb(1)
                //DO SOME THING WHEN START GET DATA HERE
                //todo add loading
            }

            override fun onSuccess(data: DataSnapshot) {
                //DO SOME THING WHEN GET DATA SUCCESS HERE
                setPb(0)
                trips_list_rec_view.adapter = context?.let {
                    TripAdapter(
                        it,
                        listOfTrips,
                        listOfUsersLite
                    )
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
                //todo add loading
            }

            override fun onSuccess(data: DataSnapshot) {
                //DO SOME THING WHEN GET DATA SUCCESS HERE
                trips_list_rec_view.adapter = context?.let {
                    TripAdapter(
                        it,
                        listOfTrips,
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