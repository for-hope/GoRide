package me.lamine.goride.notificationActivity

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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.fragment_notifications.*
import me.lamine.goride.R
import me.lamine.goride.dataObjects.*
import me.lamine.goride.interfaces.OnGetDataListener
import me.lamine.goride.utils.Database
import me.lamine.goride.utils.setPb


/**
 * A simple [Fragment] subclass.
 */
class NotificationFragment : androidx.fragment.app.Fragment() {
    private lateinit var notifications: MutableList<BookingNotification>
    private lateinit var extendedNotification: MutableList<ExtendedBookingNotif>
    private lateinit var standardNotifications: MutableList<StandaredNotification>
    private var listOfLiteUser = mutableListOf<LiteUser>()
    private var driverNotificationsList: MutableList<DriveNotification> = mutableListOf()
    private lateinit var llm2: LinearLayoutManager
    private lateinit var mDatabase: Database
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mLayout: LinearLayout
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mDatabase = Database()
        mProgressBar = activity!!.findViewById(R.id.pb_notif)
        mLayout = activity!!.findViewById(R.id.greyout_notif)
        this.notif_list_res_view.setHasFixedSize(true)
        val llm = LinearLayoutManager(this.context)
        llm.orientation = RecyclerView.VERTICAL
        notif_list_res_view.isNestedScrollingEnabled = false
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

        getNotificationsData()
        notifications = mutableListOf()
        listOfLiteUser = mutableListOf()
        extendedNotification = mutableListOf()
        standardNotifications = mutableListOf()
        driverNotificationsList = mutableListOf()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notifications, container, false)

    }

    private fun saveNotif(tripID: String, origin: String, dest: String, date: String) {
        for (user in listOfLiteUser) {

            val eBN = ExtendedBookingNotif(origin, dest, date)
            eBN.username = user.name
            eBN.profilePic = user.profilePic
            eBN.timestamp = user.pendingTS
            eBN.tripID = tripID
            eBN.userID = user.userId

            extendedNotification.add(eBN)


            if (tripID == notifications.last().tripID && user == listOfLiteUser.last()) {
                if (notif_list_res_view != null) {
                    notif_list_res_view.adapter = this.context?.let {
                        NotificationAdapter(
                            it,
                            extendedNotification
                        )
                    }
                    setPb(null, mProgressBar, mLayout, 0)
                } else {
                    this.activity?.finish()
                }


            }
        }
    }

    private fun saveLiteUser(dataSnapshot: DataSnapshot, timestamp: String): LiteUser {
        var ratings = dataSnapshot.child("ratings").value as Long?
        var peopleDriven = dataSnapshot.child("peopleDriven").value as Long?
        var fullName = dataSnapshot.child("fullName").value as String?
        val gender = dataSnapshot.child("gender").value as String?
        val birthday = dataSnapshot.child("birthday").value as String?
        val profilePic = dataSnapshot.child("profilePic").value as String?
        val userID = dataSnapshot.key
        if (ratings == null) {
            ratings = 0
        }
        if (peopleDriven == null) {
            peopleDriven = 0
        }
        if (fullName == null) {
            fullName = "GoRide User"
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

    private fun setDriverRequestsAdapter() {
        setPb(null, mProgressBar, mLayout, 0)
        if (req_notif_list_res_view != null) {
            req_notif_list_res_view.adapter = this.context?.let {
                DriveRequestsAdapter(
                    it,
                    driverNotificationsList
                )

            }


        }
    }

    private fun readDrivers() {
        //fill drivers list if it causes errors
        for ((index, notif) in driverNotificationsList.withIndex()) {
            mDatabase.fetchUser(notif.userId, object : OnGetDataListener {
                override fun onStart() {

                }

                override fun onSuccess(data: DataSnapshot) {
                    val mDriver = data.getValue(User::class.java)
                    Log.i("KEY_", data.key!!)
                    notif.userObject = mDriver!!
                    if (index == driverNotificationsList.lastIndex) {
                        setDriverRequestsAdapter()
                    }

                }

                override fun onFailed(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@NotificationFragment.context,
                        "Error ${databaseError.message}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

            })
        }

    }

    private fun readDrivingNotifications() {
        mDatabase.fetchFromCurrentUser("driveRequests", object : OnGetDataListener {
            override fun onStart() {
                setPb(null, mProgressBar, mLayout, 1)
            }

            override fun onSuccess(data: DataSnapshot) {
                for (ds in data.children) {
                    val tripId = ds.key!!
                    var userId: String
                    var otd: String
                    var timestamp: String
                    var date: String
                    for (dataChild in ds.children) {
                        userId = dataChild.key!!
                        otd = dataChild.child("otd").value as String
                        date = dataChild.child("date").value as String
                        timestamp = dataChild.child("timestamp").value as String
                        val dNotif = DriveNotification(tripId, userId, otd, date, timestamp)
                        driverNotificationsList.add(dNotif)
                    }
                    readDrivers()

                }
                setPb(null, mProgressBar, mLayout, 0)
            }

            override fun onFailed(databaseError: DatabaseError) {

            }

        })

    }

    private fun getAllPendingUsers(
        listOfPairs: List<Pair<String, String>>,
        t: String,
        o: String,
        d: String,
        date: String
    ) {
        for (pair in listOfPairs) {
            mDatabase.fetchUser(pair.first, object : OnGetDataListener {
                override fun onStart() {

                }

                override fun onSuccess(data: DataSnapshot) {
                    listOfLiteUser.add(saveLiteUser(data, pair.second))
                    if (pair == listOfPairs.last()) {
                        saveNotif(t, o, d, date)
                    }

                }

                override fun onFailed(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@NotificationFragment.context,
                        "Error ${databaseError.message}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })
        }
    }

    private fun getTripData(dataSnapshot: DataSnapshot) {
        val origin: String? = dataSnapshot.child("originCity").value as String
        val dest = dataSnapshot.child("destCity").value as String
        val date = dataSnapshot.child("date").value as String
        val tripID = dataSnapshot.key

        for (notification in notifications) {
            if (notification.tripID == tripID) {
                getAllPendingUsers(notification.pendingUsers, tripID, origin!!, dest, date)
            }
        }


    }

    private fun checkInfoInServer() {
        for (trip in notifications) {
            mDatabase.fetchTrip(trip.tripID, trip.otd, object : OnGetDataListener {
                override fun onStart() {
                }

                override fun onSuccess(data: DataSnapshot) {
                    getTripData(data)
                }

                override fun onFailed(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@NotificationFragment.context,
                        "Error ${databaseError.message}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })
        }

    }

    private fun getData(dataSnapshot: DataSnapshot) {
        var otd = ""
        var userId: String
        var timestamp: String
        val listOfUsers: MutableList<Pair<String, String>> = mutableListOf()
        val tripID = dataSnapshot.key
        for (data in dataSnapshot.children) {
            userId = data.key!!
            timestamp = data.child("timestamp").value as String
            otd = data.child("otd").value as String

            listOfUsers.add(Pair(userId, timestamp))


        }
        val bookingNotif = BookingNotification(tripID!!, listOfUsers)
        bookingNotif.otd = otd
        notifications.add(bookingNotif)
    }

    private fun getStandardData(ds: DataSnapshot) {
        when {
            ds.key == "unbookedUsers" -> for (child in ds.children) {
                val tripId = child.key
                var userId = ""
                val sn = StandaredNotification(tripId!!)
                for (otherChild in child.children) {
                    when {
                        otherChild.key == "otd" -> {
                            sn.otd = otherChild.value as String
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

                standardNotifications.add(sn)

            }
            ds.key == "canceledTrips" -> for (child in ds.children) {
                Log.i("CANCEL", ":OL")
                val tripId = child.key
                val type = "canceledTrips"
                val sn = StandaredNotification(tripId!!)
                sn.type = type
                sn.otd = child.child("otd").value as String
                sn.timestamp = child.child("timestamp").value as String
                Log.i("TIMESTAMP", "${sn.timestamp} lol")
                standardNotifications.add(sn)
            }
            ds.key == "modifiedTrips" -> for (child in ds.children) {
                val tripId = child.key
                val type = "modifiedTrips"
                val sn = StandaredNotification(tripId!!)
                sn.type = type
                sn.timestamp = child.value as String
                standardNotifications.add(sn)
            }
            ds.key == "acceptedDriveRequest" -> for (child in ds.children) {
                val tripId = child.key
                val type = "acceptedDriveRequest"
                val sn = StandaredNotification(tripId!!)
                sn.type = type
                sn.otd = child.child("otd").value as String
                sn.timestamp = child.child("timestamp").value as String
                standardNotifications.add(sn)
            }
            ds.key == "declinedTripRequests" -> for (child in ds.children) {
                val tripId = child.key
                val type = "declinedTripRequests"
                val sn = StandaredNotification(tripId!!)
                sn.type = type
                sn.otd = child.child("otd").value as String
                sn.timestamp = child.child("timestamp").value as String
                standardNotifications.add(sn)
            }
            ds.key == "bookedUsers" -> for (child in ds.children){
                val tripId = child.key
                val type = "bookedUsers"
                val sn = StandaredNotification(tripId!!)
                sn.type = type
                sn.otd = child.child("otd").value as String
                sn.timestamp = child.child("timestamp").value as String
                standardNotifications.add(sn)
            }
            ds.key == "acceptedTripRequests" -> for (child in ds.children) {
                val tripId = child.key
                val type = "acceptedTripRequests"
                val sn = StandaredNotification(tripId!!)
                sn.type = type
                sn.otd = child.child("otd").value as String
                sn.timestamp = child.child("timestamp").value as String
                standardNotifications.add(sn)
            }
        }
    }

    private fun getNotificationsData() {
        mDatabase.fetchFromCurrentUser("notifications", object : OnGetDataListener {
            override fun onStart() {

                setPb(null, mProgressBar, mLayout, 1)
            }

            override fun onSuccess(data: DataSnapshot) {
                if (data.childrenCount.toInt() != 0) {
                    for (ds in data.children) {
                        if (ds.exists() && ds.key != null) {
                            if (ds.key == "tripRequests" && ds.hasChildren()) {
                                for (child in ds.children) {
                                    getData(ds)
                                }
                            } else if (ds.key != "tripRequests") {
                                getStandardData(ds)
                            }

                        }

                    }
                }
                if (notifications.isEmpty()) {
                    setEmptyUI()
                } else {
                    checkInfoInServer()
                }
                if (standardNotifications.isEmpty()) {
                    setEmptyUI()
                } else {
                    checkAllNotifications()
                    readDrivingNotifications()
                }
            }

            override fun onFailed(databaseError: DatabaseError) {
                Toast.makeText(this@NotificationFragment.context, "Error ${databaseError.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })

    }

    private fun checkAllNotifications() {
        for (notif in standardNotifications) {
            mDatabase.fetchTrip(notif.tripId, notif.otd, object : OnGetDataListener {
                override fun onStart() {
                    //DO SOME THING WHEN START GET DATA HERE
                }

                override fun onSuccess(data: DataSnapshot) {
                    val mTrip = data.getValue(Trip::class.java)
                    for ((i, n) in standardNotifications.withIndex()) {
                        if (n.tripId == data.key) {
                            standardNotifications[i].trip = mTrip
                        }
                    }
                    if (notif == standardNotifications.last()) {
                        if (empty_list_notif != null && snotif_list_res_view != null) {
                            empty_list_notif.visibility = View.GONE
                            snotif_list_res_view.adapter = this@NotificationFragment.context?.let {
                                ExtraNotifAdapter(
                                    standardNotifications
                                )
                            }
                            snotif_list_res_view.layoutManager = llm2
                        }
                    }
                }

                override fun onFailed(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@NotificationFragment.context,
                        "Error ${databaseError.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }
    }

    private fun setEmptyUI() {
        if (empty_list_notif !== null) {
            empty_list_notif.visibility = View.VISIBLE
            setPb(null, mProgressBar, mLayout, 0)
        }

    }
}