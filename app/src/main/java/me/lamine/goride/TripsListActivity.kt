package me.lamine.goride

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigamole.navigationtabstrip.NavigationTabStrip
import kotlinx.android.synthetic.main.activity_trips_list.*
import android.R.attr.key
import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import com.google.gson.Gson
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import java.util.ArrayList


class TripsListActivity: AppCompatActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trips_list)
        setSupportActionBar(findViewById(R.id.results_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);
        val listOfTrips:MutableList<Trip> = mutableListOf()
        val listOfStringTrips:ArrayList<String> = arrayListOf()
        //restore trip
        val mPrefs = this.getSharedPreferences("TripsPref",Context.MODE_PRIVATE)!!
        val tripIDs = mPrefs.getInt("TripID", 0)
        Log.i("TRIP_IDS ", tripIDs.toString())
        val gson = Gson()
        for (i in 1..tripIDs){
        var id = "TripID$i"
        val json = mPrefs.getString(id, "")
         listOfStringTrips.add(json!!)
         Log.i("json",json)
        val tripOBJ = gson.fromJson<Trip>(json, Trip::class.java)!!
        listOfTrips.add(tripOBJ)}


        val bundle = Bundle()
        Log.i("SEARCH_TRIPS",listOfStringTrips.size.toString())
        bundle.putStringArrayList("TripsList",listOfStringTrips)
        bundle.putSerializable("PassedTrip", intent.getSerializableExtra("PostingActivity"))
        //val tripResultsFragment:Fragment = TripResultFragment()
        //tripResultsFragment.arguments = bundle
        val navigationTabStrip = findViewById<NavigationTabStrip>(R.id.trip_results_tablayout)
        val fragmentAdapter = MyTripResultPageAdapter(supportFragmentManager,listOfStringTrips)
        viewpager_r.adapter = fragmentAdapter
        navigationTabStrip.setViewPager(viewpager_r)
        initNavStripe(navigationTabStrip)
       // tabLayout.setupWithViewPager(viewpager_v)
      //  tabLayout.setTabTextColors(ContextCompat.getColor(this,R.color.colorPrimary), ContextCompat.getColor(this,R.color.whiteColor))





    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    private fun initNavStripe(navigationTabStrip: NavigationTabStrip) {
        navigationTabStrip.setTitles("Trips", "Requests")
        navigationTabStrip.setViewPager(viewpager_r,0)
        navigationTabStrip.setTabIndex(0, true)
        navigationTabStrip.setBackgroundColor(Color.WHITE)
        navigationTabStrip.stripColor = Color.RED
        navigationTabStrip.stripType = NavigationTabStrip.StripType.POINT
        navigationTabStrip.stripGravity = NavigationTabStrip.StripGravity.BOTTOM
        navigationTabStrip.setTypeface("fonts/typeface.ttf")
        navigationTabStrip.animationDuration = 300
        navigationTabStrip.inactiveColor = Color.GRAY
        navigationTabStrip.activeColor = Color.RED
    }

}