package me.lamine.goride

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_trips_list.*

class TripsListActivity: AppCompatActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trips_list)
        trips_list_rec_view.setHasFixedSize(true)
       val llm = LinearLayoutManager(this)
        llm.orientation = RecyclerView.VERTICAL
        trips_list_rec_view.layoutManager = llm
        val passedTrip:Trip = intent.getSerializableExtra("PostingActivity") as Trip
        val listOfTrips:List<Trip> = listOf(passedTrip)
        trips_list_rec_view.adapter = TripAdapter(listOfTrips)




    }
}