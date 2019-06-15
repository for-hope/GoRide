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
import kotlinx.android.synthetic.main.fragment_driver.*
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
    private var listOfTrips: MutableList<Trip> = mutableListOf()
    private var isSameOriginCity: Boolean = false
    private var isSameDestinationCity: Boolean = false
    private lateinit var tsd: TripSearchData
    private var listOfUsersLite: MutableList<User> = mutableListOf()
    private var searchAgain = false
    private lateinit var mProgressBar:ProgressBar
    private lateinit var mLayout:LinearLayout
    private var hasDate:Boolean = false
    private var searchDate = ""
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

        if (tsd.tripDate != ""){
            hasDate = true
            searchDate = tsd.tripDate
        }
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

        swipeLayout.setOnRefreshListener {
            searchForTrips(childName) }

    }
    private fun setAdapter(sortedList:List<Trip>){
        if (sortedList.isNotEmpty()){
        setPb(empty_search_layout,mProgressBar,mLayout,0)
        swipeLayout.isRefreshing = false
        scrolling.visibility = View.VISIBLE
        trips_list_rec_view.adapter = context?.let {
            TripAdapter(
                it,
                sortedList,
                listOfUsersLite
            )
        }
        }
    }
    private fun mCheckUserInfoInServer(trip: Trip?, noTrip: Boolean, isFinalChild: Boolean) {
        Log.i("Adapter","Setup;;")
        if (noTrip && isFinalChild){
            val sortedList = listOfTrips.sortedWith(compareBy { it.date })
            for (item in sortedList) {
                listOfUsersLite.add(item.poster!!)
            }
            setAdapter(sortedList)
            } else {



        var child = ""
        if (trip != null) {
            child = trip.userID
        }
        Database().fetchUser(child, object : OnGetDataListener {
            override fun onStart() {

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
                  setAdapter(sortedList)
                }

            }

            override fun onFailed(databaseError: DatabaseError) {
                Toast.makeText(this@TripResultFragment.context, "Failed to fetch data, check your connection", Toast.LENGTH_SHORT).show()
            }
        })

        }
    }

    private fun searchForTrips(otd: String) {
        setPb(empty_search_layout,mProgressBar,mLayout,1)
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        if (!searchAgain) {
            listOfTrips = mutableListOf()
            Database().fetchTripsList(otd, "destSubCity", tsd.destSubCity, object : OnGetDataListener {
                override fun onStart() {

                }

                override fun onSuccess(data: DataSnapshot) {
                    Log.i("S1", data.childrenCount.toInt().toString())
                    if (data.childrenCount.toInt() == 0) {
                        if (isSameDestinationCity) {
                            swipeLayout.isRefreshing = false
                            setPb(empty_search_layout,mProgressBar,mLayout,0)
                        } else {
                            searchAgain = true
                            searchForTrips(otd)
                        }

                    } else {
                    val lastDs = data.children.last()
                    for (ds in data.children) {
                        val mTrip = ds.getValue(Trip::class.java)
                        val dateAndTime = "${mTrip?.date} ${mTrip?.time}"
                        val tripDate = sdf.parse(dateAndTime)
                        val condition = if (!hasDate){
                            tripDate.after(Date())
                        } else {
                            mTrip?.date == searchDate
                        }
                        if (ds.value == lastDs.value) {
                            if (isSameDestinationCity) {
                                if (condition) {
                                    mCheckUserInfoInServer(mTrip, noTrip = false, isFinalChild = true)
                                } else {
                                    mCheckUserInfoInServer(mTrip, noTrip = true, isFinalChild = true)
                                }
                            } else {
                                searchAgain = true
                                searchForTrips(otd)
                            }
                        } else {
                            if (condition) {
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
        else {

            Database().fetchTripsList(
                otd,
                "destSubCity",
                wilayaArrayEN[tsd.destinationCode - 1],
                object : OnGetDataListener {
                    override fun onStart() {

                    }

                    override fun onSuccess(data: DataSnapshot) {

                        if (data.childrenCount.toInt() == 0) {
                            Log.i("ON-c2", "THIS")
                            swipeLayout.isRefreshing = false
                            setPb(empty_search_layout,mProgressBar,mLayout,0)

                            mCheckUserInfoInServer(null, noTrip = true, isFinalChild = true)
                        } else {
                        val lastDs = data.children.last()
                        for (ds in data.children) {
                            val mTrip = ds.getValue(Trip::class.java)
                            val dateAndTime = "${mTrip?.date} ${mTrip?.time}"
                            val tripDate = sdf.parse(dateAndTime)
                            val condition = if (!hasDate){
                                tripDate.after(Date())
                            } else {
                                mTrip?.date == searchDate
                            }
                            if (ds.value == lastDs.value) {
                                searchAgain = false
                                if (condition) {
                                    mCheckUserInfoInServer(mTrip, noTrip = false, isFinalChild = true)
                                } else {
                                    mCheckUserInfoInServer(mTrip, noTrip = true, isFinalChild = true)
                                }
                            } else {
                                if (condition) {
                                    mCheckUserInfoInServer(mTrip, noTrip = false, isFinalChild = false)
                                }
                            }

                        }

                    }}

                    override fun onFailed(databaseError: DatabaseError) {

                    }

                })

        }


    }
}