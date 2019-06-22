package me.lamine.goride.searchActivity


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.gigamole.navigationtabstrip.NavigationTabStrip
import kotlinx.android.synthetic.main.activity_trips_list.*
import me.lamine.goride.R
import me.lamine.goride.dataObjects.TripSearchData
import java.util.*
import com.google.android.gms.maps.model.LatLng
import android.location.Geocoder
import me.lamine.goride.requestActivity.RequestTripActivity


class TripsListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trips_list)
        setSupportActionBar(findViewById(R.id.results_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val toText: String = intent.getStringExtra("to")
        val fromText: String = intent.getStringExtra("from")
        val tsd: TripSearchData = intent.getSerializableExtra("tsd") as TripSearchData
        val listOfStringTrips: ArrayList<String> = arrayListOf()

        val bundle = Bundle()
        Log.i("SEARCH_TRIPS", listOfStringTrips.size.toString())
        bundle.putStringArrayList("TripsList", listOfStringTrips)
        bundle.putSerializable("PassedTrip", intent.getSerializableExtra("PostingActivity"))
        bundle.putString("toText", toText)
        bundle.putString("fromText", fromText)
        val str = "${tsd.originSubCity} to ${tsd.destSubCity}"
        origin_to_des.text = str
        post_req_cardview.setOnClickListener {
            startActivity(Intent(this,RequestTripActivity::class.java))
        }
        val navigationTabStrip = findViewById<NavigationTabStrip>(R.id.trip_results_tablayout)
        val fragmentAdapter =
            MyTripResultPageAdapter(supportFragmentManager, tsd, toText, fromText)
        viewpager_r.adapter = fragmentAdapter
        navigationTabStrip.setViewPager(viewpager_r)
        initNavStripe(navigationTabStrip)



    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    private fun initNavStripe(navigationTabStrip: NavigationTabStrip) {
        navigationTabStrip.setTitles("Trips", "Requests")
        navigationTabStrip.setViewPager(viewpager_r, 0)
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