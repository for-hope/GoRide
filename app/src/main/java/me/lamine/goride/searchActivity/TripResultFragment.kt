package me.lamine.goride.searchActivity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.fragment_trip_results.*
import me.lamine.goride.R
import me.lamine.goride.dataObjects.Trip
import me.lamine.goride.dataObjects.TripSearchData
import me.lamine.goride.dataObjects.User
import me.lamine.goride.interfaces.OnGetDataListener
import me.lamine.goride.tripActivity.TripAdapter
import me.lamine.goride.utils.Database
import me.lamine.goride.utils.setPb
import me.lamine.goride.utils.wilayaArrayEN
import me.lamine.goride.utils.wilayaArrayFR
import java.text.SimpleDateFormat
import java.util.*


class TripResultFragment : Fragment() {
    private val listOfTrips: MutableList<Trip> = mutableListOf()
    private var isSameOriginCity: Boolean = false
    private var isSameDestinationCity: Boolean = false
    private lateinit var tsd: TripSearchData
    private var listOfUsersLite: MutableList<User> = mutableListOf()
    private var searchAgain = false
    private lateinit var mProgressBar:ProgressBar
    private lateinit var mLayout:LinearLayout
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trip_results, container, false)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        this.trips_list_rec_view.setHasFixedSize(true)
        val llm = LinearLayoutManager(this.context)
        llm.orientation = RecyclerView.VERTICAL
        trips_list_rec_view.isNestedScrollingEnabled = false
        trips_list_rec_view.layoutManager = llm
         mProgressBar = activity!!.findViewById(R.id.pb_searcn)
         mLayout = activity!!.findViewById(R.id.grey_search_layout)
        //
        //
        ///
        ///
        //
        val bundle = this.arguments
        tsd = bundle?.getSerializable("tsd") as TripSearchData

        //pass here
        //val childName = "${fromText}_$toText"
        //val childName ="${tsd.originCode}_${tsd.destinationCode}"
        val childName = "${String.format("%02d", tsd.originCode)}_${String.format("%02d", tsd.destinationCode)}"
        //database.child("trips").child(childName)
        //database.child("trips").child(childName).orderByChild("date").equalTo("lol")
        if (wilayaArrayEN[tsd.originCode - 1] == tsd.originSubCity || wilayaArrayFR[tsd.originCode - 1] == tsd.originSubCity) {

            isSameOriginCity = true
        }
        if (wilayaArrayEN[tsd.destinationCode - 1] == tsd.destSubCity || wilayaArrayFR[tsd.destinationCode - 1] == tsd.destSubCity) {
            isSameDestinationCity = true
        }
        ///
        ///
        //
        searchForTrips(childName)

    }

    private fun mCheckUserInfoInServer(trip: Trip?, noTrip: Boolean, isFinalChild: Boolean) {
        var child = ""
        if (trip != null) {
            child = trip.userID
        }
        Database().fetchUser(child, object : OnGetDataListener {
            override fun onStart() {
                setPb(null,mProgressBar,mLayout,1)
                //DO SOME THING WHEN START GET DATA HERE
            }

            override fun onSuccess(data: DataSnapshot) {
                //DO SOME THING WHEN GET DATA SUCCESS HERE
                if (!noTrip) {
                    val mUser = data.getValue(User::class.java)
                    trip?.poster = mUser

                    listOfTrips.add(trip!!)

                }
                if (isFinalChild) {
                    val sortedList = listOfTrips.sortedWith(compareBy { it.date })
                    for (item in sortedList) {
                        listOfUsersLite.add(item.poster!!)
                    }
                    setPb(null,mProgressBar,mLayout,0)
                    trips_list_rec_view.adapter = context?.let {
                        TripAdapter(
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

    private fun searchForTrips(otd: String) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        if (!searchAgain) {
            Database().fetchTripsList(otd, "destSubCity", tsd.destSubCity, object : OnGetDataListener {
                override fun onStart() {

                }

                override fun onSuccess(data: DataSnapshot) {
                    Log.i("S1", data.childrenCount.toInt().toString())
                    if (data.childrenCount.toInt() == 0) {
                        if (isSameDestinationCity) {
                            Log.i("onSuccess", "THIS")
                        } else {
                            searchAgain = true
                            searchForTrips(otd)
                        }

                    }
                    val lastDs = data.children.last()
                    for (ds in data.children) {
                        val mTrip = ds.getValue(Trip::class.java)
                        val dateAndTime = "${mTrip?.date} ${mTrip?.time}"
                        val tripDate = sdf.parse(dateAndTime)
                        if (ds.value == lastDs.value) {
                            if (isSameDestinationCity) {
                                if (tripDate.after(Date())) {
                                    mCheckUserInfoInServer(mTrip, noTrip = false, isFinalChild = true)
                                } else {
                                    mCheckUserInfoInServer(mTrip, noTrip = true, isFinalChild = true)
                                }
                            } else {
                                searchAgain = true
                                searchForTrips(otd)
                            }
                        } else {
                            if (tripDate.after(Date())) {
                                mCheckUserInfoInServer(mTrip, noTrip = false, isFinalChild = false)
                            }
                        }


                    }
                }

                override fun onFailed(databaseError: DatabaseError) {

                }

            })
        } else {
            Database().fetchTripsList(
                otd,
                "destSubCity",
                wilayaArrayEN[tsd.destinationCode - 1],
                object : OnGetDataListener {
                    override fun onStart() {

                    }

                    override fun onSuccess(data: DataSnapshot) {
                        Log.i("S2", data.childrenCount.toInt().toString())
                        if (data.childrenCount.toInt() == 0) {
                            mCheckUserInfoInServer(null, noTrip = true, isFinalChild = true)
                        }
                        val lastDs = data.children.last()
                        for (ds in data.children) {
                            val mTrip = ds.getValue(Trip::class.java)
                            val dateAndTime = "${mTrip?.date} ${mTrip?.time}"
                            val tripDate = sdf.parse(dateAndTime)
                            if (ds.value == lastDs.value) {
                                if (tripDate.after(Date())) {
                                    Log.i("ON-c", "THIS")
                                    mCheckUserInfoInServer(mTrip, noTrip = false, isFinalChild = true)
                                } else {
                                    mCheckUserInfoInServer(mTrip, noTrip = true, isFinalChild = true)
                                }
                            } else {
                                if (tripDate.after(Date())) {
                                    mCheckUserInfoInServer(mTrip, noTrip = false, isFinalChild = false)
                                }
                            }

                        }

                    }

                    override fun onFailed(databaseError: DatabaseError) {

                    }

                })

        }


    }
}