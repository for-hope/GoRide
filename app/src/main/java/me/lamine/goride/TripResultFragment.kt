package me.lamine.goride

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_trip_results.*
import android.R.attr.key
import android.util.Log
import com.google.gson.Gson
import java.io.Serializable


class TripResultFragment:Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trip_results, container, false)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        this.trips_list_rec_view.setHasFixedSize(true)
        val llm = LinearLayoutManager(this.context)
        llm.orientation = RecyclerView.VERTICAL
        trips_list_rec_view.isNestedScrollingEnabled = false;
        trips_list_rec_view.layoutManager = llm
        val bundle = this.arguments
       /* var myTrip:Serializable? = null
        if (bundle != null) {
           myTrip = bundle.getSerializable("PassedTrip")
        }
        val passedTrip = myTrip as Trip*/
         val myTripsString = bundle?.getStringArrayList("TripsList")!!

        val listOfTrips:MutableList<Trip> = mutableListOf()
        for (json in myTripsString){
            val gson = Gson()
            val tripOBJ = gson.fromJson<Trip>(json, Trip::class.java)
            listOfTrips.add(tripOBJ)
        }

        //val passedTrip:Trip = intent.getSerializableExtra("PostingActivity") as Trip
        //val listOfTrips:List<Trip> = listOf(passedTrip)
        trips_list_rec_view.adapter = TripAdapter(listOfTrips)
    }


}