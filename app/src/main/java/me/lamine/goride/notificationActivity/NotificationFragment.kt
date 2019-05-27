package me.lamine.goride.notificationActivity

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_notifications.*
import me.lamine.goride.*
import me.lamine.goride.R
import me.lamine.goride.dataObjects.*
import me.lamine.goride.interfaces.OnGetDataListener
import me.lamine.goride.utils.decodeWilaya
import java.sql.Driver


/**
 * A simple [Fragment] subclass.
 */
class NotificationFragment : androidx.fragment.app.Fragment() {
    private var user: FirebaseUser? = null
    private lateinit var database:DatabaseReference
    private lateinit var notifications:MutableList<BookingNotification>
    private lateinit var extendedNotification: MutableList<ExtendedBookingNotif>
    private lateinit var standaredNotifications: MutableList<StandaredNotification>
    private var listOfLiteUser= mutableListOf<LiteUser>()
    private var driverNotificationsList:MutableList<DriveNotification> = mutableListOf()
    private var listOfDrivers:MutableList<User> = mutableListOf()
    private lateinit var llm2:LinearLayoutManager
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        user = FirebaseAuth.getInstance().currentUser
        if (user==null) {
            this.activity?.finish()
        }
        database = FirebaseDatabase.getInstance().reference
        this.notif_list_res_view.setHasFixedSize(true)
        val llm = LinearLayoutManager(this.context)
        llm.orientation = RecyclerView.VERTICAL
       notif_list_res_view.isNestedScrollingEnabled = false;
        notif_list_res_view.layoutManager = llm

        this.snotif_list_res_view.setHasFixedSize(true)
        llm2 = LinearLayoutManager(this.context)
        llm2.orientation = RecyclerView.VERTICAL
        snotif_list_res_view.isNestedScrollingEnabled = false

        this.req_notif_list_res_view.setHasFixedSize(true)
        val llm3 = LinearLayoutManager(this.context)
        llm3.orientation = RecyclerView.VERTICAL
        req_notif_list_res_view.isNestedScrollingEnabled = false
        req_notif_list_res_view.layoutManager = llm3
      //  snotif_list_res_view.layoutManager = llm2
        mCheckTripInfoInServer()
        notifications = mutableListOf()
        listOfLiteUser = mutableListOf()
        extendedNotification = mutableListOf()
        standaredNotifications = mutableListOf()
        driverNotificationsList = mutableListOf()

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notifications, container, false)

    }
    private fun saveNotif(dataSnapshot: DataSnapshot,tripID:String,origin:String,dest:String,date:String){
        for (user in listOfLiteUser){
            Log.i("1111", dataSnapshot.childrenCount.toString()!!)
            val eBN = ExtendedBookingNotif(origin, dest, date)
            eBN.username = user.name
            eBN.profilePic = user.profilePic
            Log.i("NotfFrag",user.profilePic + "FF")
            eBN.timestamp = user.pendingTS
            eBN.tripID = tripID
            eBN.userID = user.userId
            //eBN.timestampf = user.
            extendedNotification.add(eBN)

            //todo
            if (tripID == notifications.last().tripID && user == listOfLiteUser.last()){
                Log.i("Suc22", dataSnapshot.childrenCount.toString()!!)
                if (notif_list_res_view != null){
                    notif_list_res_view.adapter = this.context?.let {
                        NotificationAdapter(
                            it,
                            extendedNotification
                        )
                    }
                    setPb(0)
                } else {
                    this.activity?.finish()
                }


            }
        }
    }
   private fun saveLiteUser(dataSnapshot: DataSnapshot,timestamp: String): LiteUser {
       var ratings = dataSnapshot.child("ratings").value as Long?
       var peopleDriven = dataSnapshot.child("peopleDriven").value as Long?
       var fullName = dataSnapshot.child("fullName").value as String?
       val gender =dataSnapshot.child("gender").value as String?
       val birthday = dataSnapshot.child("birthday").value as String?
       val profilePic = dataSnapshot.child("profilePic").value as String?
       val userID = dataSnapshot.key
       if (ratings == null ){
           ratings = 0
       }
       if (peopleDriven == null)  {
           peopleDriven = 0
       }
       if (fullName == null){
           fullName = "RideGo User"
       }
        val lU = LiteUser(
            userID!!,
            fullName.toString(),
            gender!!,
            birthday!!,
            ratings.toDouble(),
            peopleDriven.toInt()
        )
       lU.profilePic = profilePic!!
       lU.pendingTS = timestamp
       return lU

   }
    private fun fetchDrivingNotif(listener: OnGetDataListener){
        listener.onStart()
        val ref = database.child("users").child(user?.uid!!).child("driveRequests")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listener.onSuccess(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException()
                // don't ignore errors
            }
        }
        ref.addListenerForSingleValueEvent(eventListener)


    }
    private fun fetchDrivers(userId:String,listener: OnGetDataListener){
        val ref = database.child("users").child(userId)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listener.onSuccess(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException()
                // don't ignore errors
            }
        }
        ref.addListenerForSingleValueEvent(eventListener)
    }
    private fun setDriverRequestsAdapter(){
        Log.i("Adapter", "SettingUP")
        setPb(0)
        if (req_notif_list_res_view != null){
            req_notif_list_res_view.adapter = this.context?.let {
               DriveRequestsAdapter(
                    it,
                    driverNotificationsList
                )

            }


        }
    }
    private fun readDrivers(){
            //fill drivers list if it causes errors
        for ((index,notif) in driverNotificationsList.withIndex()){
            fetchDrivers(notif.userId, object: OnGetDataListener{
                override fun onStart() {

                }

                override fun onSuccess(data: DataSnapshot) {
                    val mDriver = data.getValue(User::class.java)
                    Log.i("KEY_",data.key!!)
                    notif.userObject = mDriver!!
                    if (index == driverNotificationsList.lastIndex){
                        setDriverRequestsAdapter()
                    }

                }

                override fun onFailed(databaseError: DatabaseError) {

                }

            })
        }

    }
    private fun readDrivingNotifications(){
        fetchDrivingNotif(object : OnGetDataListener{
            override fun onStart() {
                    setPb(1)
            }

            override fun onSuccess(data: DataSnapshot) {
               for (ds in data.children){
                   val tripId = ds.key!!
                   var userId = ""
                   var otd = ""
                   var timestamp = ""
                   var date = ""
                   for (dataChild in ds.children){
                       Log.i("datachild", dataChild.key)
                       if (dataChild.key == "otd"){
                            otd = dataChild.value as String
                           Log.i("HERE IS OTD", otd + "MHM")
                       }  else if (dataChild.key == "date"){
                           date = dataChild.value as String
                           Log.i("HERE IS DATE", otd + "MHM")
                       }
                       else {
                            userId = dataChild.key!!
                            timestamp = dataChild.value as String
                       }
                   }
                   Log.i("after otd", otd + "N")
                   val dNotif = DriveNotification(tripId,userId,otd,date,timestamp)
                   driverNotificationsList.add(dNotif)
                   readDrivers()

               }

            }

            override fun onFailed(databaseError: DatabaseError) {

            }

        })

    }
    private fun readUserInfo(listOfPairs: List<Pair<String, String>>, listener: OnGetDataListener){
        listener.onStart()
        val rootRef = FirebaseDatabase.getInstance().reference
        for (pair in listOfPairs){
            val currentUserIdRef= rootRef.child("users").child(pair.first)
            val eventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    listOfLiteUser.add(saveLiteUser(dataSnapshot,pair.second))
                    if (pair == listOfPairs.last()){
                        listener.onSuccess(dataSnapshot)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    listener.onFailed(databaseError)
                    throw databaseError.toException()
                    // don't ignore errors
                }
            }
            currentUserIdRef.addListenerForSingleValueEvent(eventListener)
        }
    }
    private fun getAllPendingUsers(listOfPairs:List<Pair<String,String>>,t:String,o:String,d:String,date:String){
        readUserInfo(listOfPairs,object : OnGetDataListener {
            override fun onStart() {
                //DO SOME THING WHEN START GET DATA HERE
                // setPb(1)
                //todo add loading
            }

            override fun onSuccess(data: DataSnapshot) {
               saveNotif(data,t,o,d,date)
            }

            override fun onFailed(databaseError: DatabaseError) {
                //DO SOME THING WHEN GET DATA FAILED HERE
                //todo
            }
        })
    }
    private fun getTripData(dataSnapshot: DataSnapshot){
        Log.i("DATA",dataSnapshot.childrenCount.toString())
        val origin:String? = dataSnapshot.child("originCity").value as String
        val dest = dataSnapshot.child("destCity").value as String
        val date = dataSnapshot.child("date").value as String
        val tripID = dataSnapshot.key

        for (notification in notifications){
            if (notification.tripID == tripID){
                getAllPendingUsers(notification.pendingUsers,tripID,origin!!,dest,date)
            }
        }


    }
    private fun getAllInfo(listener: OnGetDataListener){
        listener.onStart()
        val rootRef = FirebaseDatabase.getInstance().reference
        for (trip in notifications){

            val currentUserIdRef= rootRef.child("trips")
            val eventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (ds in dataSnapshot.children) {
                        val newRef = currentUserIdRef.child(ds.key!!).child(trip.tripID)
                        val eventListener = object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                Log.i("dskey and tripid", dataSnapshot.childrenCount.toString()!! +"/"+trip.tripID)
                                if (dataSnapshot.exists()){

                                    listener.onSuccess(dataSnapshot)
                                }

                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                listener.onFailed(databaseError)
                                throw databaseError.toException()
                                // don't ignore errors
                            }
                        }
                        newRef.addListenerForSingleValueEvent(eventListener)
                    }
                    Log.i("COUNT",trip.tripID)
                    Log.i("COUNT2",dataSnapshot.childrenCount.toString())
                    if (dataSnapshot.childrenCount.toInt() != 0){
                     //   listener.onSuccess(dataSnapshot)
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    listener.onFailed(databaseError)
                    throw databaseError.toException()
                    // don't ignore errors
                }
            }
            currentUserIdRef.addListenerForSingleValueEvent(eventListener)
        }

    }
    private fun checkInfoInServer(){
       getAllInfo(object : OnGetDataListener {
            override fun onStart() {
                //DO SOME THING WHEN START GET DATA HERE
                // setPb(1)
                //todo add loading
            }

            override fun onSuccess(data: DataSnapshot) {
                getTripData(data)
            }

            override fun onFailed(databaseError: DatabaseError) {
                //DO SOME THING WHEN GET DATA FAILED HERE
                //todo
            }
        })
    }
    private fun getData(dataSnapshot: DataSnapshot){
        val listOfUsers:MutableList<Pair<String,String>> = mutableListOf()
        val tripID = dataSnapshot.key
        for (data in dataSnapshot.children){
            listOfUsers.add(Pair(data.key!!,data.value as String))
        }
       notifications.add(BookingNotification(tripID!!, listOfUsers))
    }
    private fun getStandardData(ds: DataSnapshot){

        when {
            ds.key == "unbookedUsers" -> for (child in ds.children){
                val tripId = child.key
                var userId = ""
                val sn = StandaredNotification(tripId!!)
                for (otherChild in child.children){
                    Log.i("CHILDREN", "${child.key}")
                    when {
                        otherChild.key == "otd" -> {sn.otd = otherChild.value as String
                        Log.i("OTD ADDED","otd = ${sn.otd}")
                        }
                        otherChild.key == "timestamp" -> {
                            val ts = otherChild.value as String
                            sn.timestamp = ts
                        }
                        else -> userId = otherChild.key!!
                    }
                }
                sn.userId = userId
                sn.type = "unbookedUsers"

                standaredNotifications.add(sn)

            }
            ds.key == "canceledTrips" -> for (child in ds.children){
                Log.i("CANCEL", ":OL")
                val tripId = child.key
                val type = "canceledTrips"
                val sn = StandaredNotification(tripId!!)
                sn.type = type
                sn.otd = child.child("otd").value as String
                sn.timestamp = child.child("timestamp").value as String
                Log.i("TIMESTAMP", "${sn.timestamp} lol")
                standaredNotifications.add(sn)
            }
            ds.key =="modifiedTrips" -> for(child in ds.children){
                val tripId = child.key
                val type = "modifiedTrips"
                val sn = StandaredNotification(tripId!!)
                sn.type = type
                sn.timestamp = child.value as String
                standaredNotifications.add(sn)
            }
            ds.key == "acceptedDriveRequest" -> for(child in ds.children) {
                val tripId = child.key
                val type = "acceptedDriveRequest"
                val sn = StandaredNotification(tripId!!)
                sn.type = type
                sn.otd = child.child("otd").value as String
                sn.timestamp = child.child("timestamp").value as String
                standaredNotifications.add(sn)
            }
        }
    }
    private fun readTripRequests(listener: OnGetDataListener) {
        listener.onStart()
        val rootRef = FirebaseDatabase.getInstance().reference
       // val currentUserIdRef= rootRef.child("users").child(user?.uid!!).child("tripRequests")
        val currentUserIdRef= rootRef.child("users").child(user?.uid!!).child("notifications")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.childrenCount.toInt() == 0){
                    listener.onSuccess(dataSnapshot)
                } else {
                    for (ds in dataSnapshot.children) {

                        if (ds.exists() && ds.key != null){
                            if (ds.key == "tripRequests" && ds.hasChildren()){
                                for (child in ds.children){
                                    getData(ds)
                                }

                            } else if (ds.key != "tripRequests"){
                                Log.i("pos1","ENTER ! ${dataSnapshot.childrenCount}")
                                   getStandardData(ds)
                            }

                        }

                    }
                }
                listener.onSuccess(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException()
                // don't ignore errors
            }
        }
        currentUserIdRef.addListenerForSingleValueEvent(eventListener)
    }
    private fun mCheckTripInfoInServer() {
        readTripRequests(object : OnGetDataListener {
            override fun onStart() {
                //DO SOME THING WHEN START GET DATA HERE
                setPb(1)
            }

            override fun onSuccess(data: DataSnapshot) {
                //DO SOME THING WHEN GET DATA SUCCESS HERE
                if (notifications.isEmpty()){
                    setEmptyUI()
                } else {
                    checkInfoInServer()
                }
                if (standaredNotifications.isEmpty()){
                    setEmptyUI()
                } else {
                    checkAllNotifications()
                    readDrivingNotifications()
                }

                //readPendingBookings
            //    setPb(0)
            }

            override fun onFailed(databaseError: DatabaseError) {
                //DO SOME THING WHEN GET DATA FAILED HERE
                //todo
            }
        })

    }
    private fun checkNotificationsInDB(notif:StandaredNotification,listener: OnGetDataListener){
        listener.onStart()
        val rootRef = FirebaseDatabase.getInstance().reference
        val tripRef= rootRef.child("trips").child(notif.otd).child(notif.tripId)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val mTrip = dataSnapshot.getValue(Trip::class.java)
                Log.i("TRIP DATE", "MSG " +dataSnapshot.childrenCount.toString())
                for ((i,n) in standaredNotifications.withIndex()){
                    if (n.tripId == dataSnapshot.key){
                        standaredNotifications[i].trip = mTrip
                    }
                }

                if (notif == standaredNotifications.last()){
                    listener.onSuccess(dataSnapshot)
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException()
                // don't ignore errors
            }
        }
        tripRef.addListenerForSingleValueEvent(eventListener)


    }
    private fun checkAllNotifications(){
        Log.i("NotifyMe","${standaredNotifications.size}")
        for (notif in standaredNotifications) {
            checkNotificationsInDB(notif, object : OnGetDataListener {
                override fun onStart() {
                    //DO SOME THING WHEN START GET DATA HERE

                    //todo add loading
                }

                override fun onSuccess(data: DataSnapshot) {
                    //DO SOME THING WHEN GET DATA SUCCESS HERE
                    empty_list_notif.visibility = View.GONE
                    snotif_list_res_view.adapter = this@NotificationFragment.context?.let {
                        ExtraNotifAdapter(
                            it,
                            standaredNotifications
                        )
                    }
                    snotif_list_res_view.layoutManager = llm2

                    //readPendingBookings
                    //    setPb(0)
                }

                override fun onFailed(databaseError: DatabaseError) {
                    //DO SOME THING WHEN GET DATA FAILED HERE
                    //todo
                }
            })
        }
    }
    private fun setEmptyUI(){
        if (empty_list_notif !== null){
            empty_list_notif.visibility = View.VISIBLE
            //todo scrolling.visibility = View.GONE
            setPb(0)
        }

    }
    private fun setPb(visibility: Int){
        val mProgressBar = activity!!.findViewById<ProgressBar>(R.id.pb_notif)
        val mLayout = activity!!.findViewById<LinearLayout>(R.id.greyout_notif)
        if (visibility == 1) {
            mProgressBar.visibility = View.VISIBLE
            mLayout.visibility = View.VISIBLE

        } else {
            mProgressBar.visibility = View.GONE
            mLayout.visibility = View.GONE

        }
    }
}