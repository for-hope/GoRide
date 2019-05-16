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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_notifications.*
import me.lamine.goride.*
import me.lamine.goride.dataObjects.BookingNotification
import me.lamine.goride.dataObjects.ExtendedBookingNotif
import me.lamine.goride.dataObjects.LiteUser
import me.lamine.goride.interfaces.OnGetDataListener


/**
 * A simple [Fragment] subclass.
 */
class NotificationFragment : androidx.fragment.app.Fragment() {
    private var user: FirebaseUser? = null
    object statics {
        @JvmField val TAG = "NotificationFragment"
    }
    private lateinit var notifications:MutableList<BookingNotification>
    private lateinit var extendedNotification: MutableList<ExtendedBookingNotif>
    private var listOfLiteUser= mutableListOf<LiteUser>()
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        user = FirebaseAuth.getInstance().currentUser
        if (user==null) {
            this.activity?.finish()
        }
        this.notif_list_res_view.setHasFixedSize(true)
        val llm = LinearLayoutManager(this.context)
        llm.orientation = RecyclerView.VERTICAL
       notif_list_res_view.isNestedScrollingEnabled = false;
        notif_list_res_view.layoutManager = llm
        mCheckTripInfoInServer()
        notifications = mutableListOf()
        listOfLiteUser = mutableListOf()
        extendedNotification = mutableListOf()

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
            //eBN.timestamp = user.
            extendedNotification.add(eBN)

            //todo
            if (tripID == notifications.last().tripID && user == listOfLiteUser.last()){
                Log.i("Suc22", dataSnapshot.childrenCount.toString()!!)
                notif_list_res_view.adapter = this.context?.let {
                    NotificationAdapter(
                        it,
                        extendedNotification
                    )
                }
                setPb(0)
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
                Log.i("pednis", data.childrenCount.toString()!!)
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
        val origin:String? = dataSnapshot.child("origin").value as String
        val dest = dataSnapshot.child("destination").value as String
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
        Log.i("getData",dataSnapshot.key)
        for (data in dataSnapshot.children){
            listOfUsers.add(Pair(data.key!!,data.value as String))
        }
       notifications.add(BookingNotification(tripID!!, listOfUsers))
    }
    private fun readTripRequests(listener: OnGetDataListener) {
        listener.onStart()
        val rootRef = FirebaseDatabase.getInstance().reference
        val currentUserIdRef= rootRef.child("users").child(user?.uid!!).child("tripRequests")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.childrenCount.toInt() == 0){
                    listener.onSuccess(dataSnapshot)
                    Log.i("READ",dataSnapshot.childrenCount.toString())
                } else {
                    for (ds in dataSnapshot.children) {
                        if (ds.exists() && ds.key != null){
                           getData(ds)
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
                //todo add loading
            }

            override fun onSuccess(data: DataSnapshot) {
                //DO SOME THING WHEN GET DATA SUCCESS HERE
             //todo set adapter
                if (notifications.isEmpty()){
                    setEmptyUI()
                } else {
                    checkInfoInServer()
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
    private fun setEmptyUI(){
       empty_list_notif.visibility = View.VISIBLE
        scrolling.visibility = View.GONE
        setPb(0)
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