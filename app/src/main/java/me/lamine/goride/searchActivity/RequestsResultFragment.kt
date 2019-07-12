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
import com.wajahatkarim3.easyvalidation.core.view_ktx.contains
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
    private var listOfTrips: MutableList<TripRequest> = mutableListOf()
    private var isSameOriginCity: Boolean = false
    private var isSameDestinationCity: Boolean = false
    private lateinit var tsd: TripSearchData
    private var listOfUsersLite: MutableList<User> = mutableListOf()
    private var searchAgain = false
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mLayout: LinearLayout
    private lateinit var mDatabase: Database
    private var hasDate = false
    private var searchDate = ""
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

        if (tsd.tripDate != "") {
            hasDate = true
            searchDate = tsd.tripDate
        }
        val childName = "${String.format("%02d", tsd.originCode)}_${String.format("%02d", tsd.destinationCode)}"


        if (wilayaArrayEN[tsd.originCode - 1] == tsd.originSubCity || wilayaArrayFR[tsd.originCode - 1] == tsd.originSubCity) {

            isSameOriginCity = true
        }
        if (tsd.destSubCity.contains(wilayaArrayEN[tsd.destinationCode - 1]) || tsd.destSubCity.contains(wilayaArrayFR[tsd.destinationCode - 1])) {

            isSameDestinationCity = true
        }
        ////
        //
        searchForTrips(childName)
        swipeLayout.setOnRefreshListener {
            searchForTrips(childName)
        }
    }

    private fun searchForTrips(otd: String) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)
        if (!searchAgain) {
            listOfTrips = mutableListOf()
            Database().fetchTripRequestsList(otd, "destSubCity", tsd.destSubCity, object : OnGetDataListener {
                override fun onStart() {

                }

                override fun onSuccess(data: DataSnapshot) {
                    swipeLayout.isRefreshing = false
                    if (data.childrenCount.toInt() == 0) {
                        if (isSameDestinationCity) {

                        } else {
                            searchAgain = true
                            searchForTrips(otd)
                        }

                    } else {
                        Log.i("onSuccess", "NOT_EMPTYR")

                        val lastDs = data.children.last()
                        for (ds in data.children) {
                            val mTrip = ds.getValue(TripRequest::class.java)
                            val dateAndTime = "${mTrip?.date} ${mTrip?.time}"
                            val tripDate = sdf.parse(dateAndTime)
                            val condition = if (!hasDate) {
                                tripDate.after(Date())
                            } else {
                                mTrip?.date == searchDate
                            }
                            if (ds.key == lastDs.key) {

                                if (isSameDestinationCity) {

                                    if (condition) {
                                        Log.d("REquestAct1", "SENDIT ${ds.key} and ${lastDs.key}")
                                        mCheckUserInfoInServer(mTrip, noTrip = false, isFinalChild = true)
                                    } else {
                                        mCheckUserInfoInServer(mTrip, noTrip = true, isFinalChild = true)
                                    }
                                } else {
                                    if (condition) {

                                        mCheckUserInfoInServer(mTrip, noTrip = false, isFinalChild = true)
                                    }
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
                            //val lastDs = data.children.last()
                            for (ds in data.children) {
                                val mTrip = ds.getValue(TripRequest::class.java)
                                val dateAndTime = "${mTrip?.date} ${mTrip?.time}"
                                val tripDate = sdf.parse(dateAndTime)
                                val condition = if (!hasDate) {
                                    tripDate.after(Date())
                                } else {
                                    mTrip?.date == searchDate
                                }
                                if (condition) {
                                    searchAgain = false
                                    if (tripDate.after(Date())) {
                                        Log.i("ON-c", "THIS")
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

                        }
                    }

                    override fun onFailed(databaseError: DatabaseError) {

                    }

                })

        }


    }

    private fun setAdapter(sortedList: List<TripRequest>) {
        if (sortedList.isNotEmpty()) {
            Log.e("EMPTY_ERROR", "ERROR_4")
            setPb(empty_search_layout, mProgressBar, mLayout, 0)
            swipeLayout.isRefreshing = false
            scrolling.visibility = View.VISIBLE
            empty_search_layout.visibility = View.GONE
            trips_list_rec_view.adapter = context?.let {
                RequestsAdapter(
                    it,
                    sortedList,
                    listOfUsersLite
                )
            }
        } else {
            Log.e("EMPTY_ERROR", "ERROR_1")
        }
    }

    private fun mCheckUserInfoInServer(trip: TripRequest?, noTrip: Boolean, isFinalChild: Boolean) {
        if (noTrip && isFinalChild) {
            if (listOfTrips.isEmpty()) {
                swipeLayout.isRefreshing = false
                empty_search_layout.visibility = View.VISIBLE
                setPb(empty_search_layout, mProgressBar, mLayout, 0)
            }
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
            mDatabase.fetchUser(child, object : OnGetDataListener {
                override fun onStart() {
                    setPb(null, mProgressBar, mLayout, 1)
                }

                override fun onSuccess(data: DataSnapshot) {
                    //DO SOME THING WHEN GET DATA SUCCESS HERE
                    if (!noTrip && data.exists()) {
                        val mUser = data.getValue(User::class.java)
                        trip?.poster = mUser
                        val checkList: MutableList<String> = mutableListOf()
                        for (mTrip in listOfTrips) {
                            checkList.add(mTrip.tripID)
                        }
                        if (!checkList.contains(trip?.tripID)) {
                            listOfTrips.add(trip!!)
                        }
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
}