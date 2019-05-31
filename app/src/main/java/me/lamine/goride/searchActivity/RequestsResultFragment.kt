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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.fragment_trip_results.*
import me.lamine.goride.R
import me.lamine.goride.dataObjects.TripRequest
import me.lamine.goride.dataObjects.TripSearchData
import me.lamine.goride.dataObjects.User
import me.lamine.goride.interfaces.OnGetDataListener
import me.lamine.goride.tripActivity.RequestsAdapter
import me.lamine.goride.utils.Database
import me.lamine.goride.utils.setPb
import me.lamine.goride.utils.wilayaArrayEN
import me.lamine.goride.utils.wilayaArrayFR
import java.text.SimpleDateFormat
import java.util.*

class RequestsResultFragment : Fragment() {
    private val listOfTrips: MutableList<TripRequest> = mutableListOf()
    private var isSameOriginCity: Boolean = false
    private var isSameDestinationCity: Boolean = false
    private lateinit var tsd: TripSearchData
    private var listOfUsersLite: MutableList<User> = mutableListOf()
    private var searchAgain = false
    private lateinit var mProgressBar:ProgressBar
    private lateinit var mLayout:LinearLayout
    private lateinit var mDatabase: Database
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_trip_results, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        this.trips_list_rec_view.setHasFixedSize(true)
        val llm = LinearLayoutManager(this.context)
        llm.orientation = RecyclerView.VERTICAL
        trips_list_rec_view.isNestedScrollingEnabled = false
        trips_list_rec_view.layoutManager = llm
        val bundle = this.arguments
        mDatabase = Database()
        mProgressBar = activity!!.findViewById(R.id.pb_searcn)
         mLayout = activity!!.findViewById(R.id.grey_search_layout)
        //
        //
        ///
        ///
        //
        tsd = bundle?.getSerializable("tsd") as TripSearchData

        val childName = "${String.format("%02d", tsd.originCode)}_${String.format("%02d", tsd.destinationCode)}"
     

        if (wilayaArrayEN[tsd.originCode - 1] == tsd.originSubCity || wilayaArrayFR[tsd.originCode - 1] == tsd.originSubCity) {

            isSameOriginCity = true
        }
        if (wilayaArrayEN[tsd.destinationCode - 1] == tsd.destSubCity || wilayaArrayFR[tsd.destinationCode - 1] == tsd.destSubCity) {
            isSameDestinationCity = true
        }
        ////
        //
        searchForTrips(childName)
    }

    private fun searchForTrips(otd: String) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        if (!searchAgain) {
            Database().fetchTripRequestsList(otd, "destSubCity", tsd.destSubCity, object : OnGetDataListener {
                override fun onStart() {

                }

                override fun onSuccess(data: DataSnapshot) {
                    if (data.childrenCount.toInt() == 0) {
                        if (isSameDestinationCity) {
                            Log.i("onSuccess", "THIS")
                        } else {
                            searchAgain = true
                            searchForTrips(otd)
                        }

                    } else {


                    val lastDs = data.children.last()
                    for (ds in data.children) {
                        val mTrip = ds.getValue(TripRequest::class.java)
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
                }

                override fun onFailed(databaseError: DatabaseError) {

                }

            })
        } else {
            Database().fetchTripRequestsList(
                otd,
                "destSubCity",
                wilayaArrayEN[tsd.destinationCode - 1],
                object : OnGetDataListener {
                    override fun onStart() {

                    }

                    override fun onSuccess(data: DataSnapshot) {
                        if (data.childrenCount.toInt() == 0) {
                            mCheckUserInfoInServer(null, noTrip = true, isFinalChild = true)
                        } else {
                            val lastDs = data.children.last()
                            for (ds in data.children) {
                                val mTrip = ds.getValue(TripRequest::class.java)
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
                    }

                    override fun onFailed(databaseError: DatabaseError) {

                    }

                })

        }


    }

    private fun mCheckUserInfoInServer(trip: TripRequest?, noTrip: Boolean, isFinalChild: Boolean) {
        var child = ""
        if (trip != null) {
            child = trip.userID
        }
        mDatabase.fetchUser(child, object : OnGetDataListener {
            override fun onStart() {
                setPb(null,mProgressBar,mLayout,1)
            }

            override fun onSuccess(data: DataSnapshot) {
                //DO SOME THING WHEN GET DATA SUCCESS HERE
                if (!noTrip && data.exists()) {
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
                Toast.makeText(
                    this@RequestsResultFragment.context,
                    "Error, ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })


    }
}