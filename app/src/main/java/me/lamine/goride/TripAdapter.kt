package me.lamine.goride

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.widget.ImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class TripAdapter(private var tripsList: List<Trip>): RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = tripsList[position]
        val name = "Lamine Fet"
        holder.vName.text = name
        val OLD_FORMAT = "dd/MM/yyyy"
        val NEW_FORMAT = "EEE, MMM dd"

        val newDate: String

        val sdf = SimpleDateFormat(OLD_FORMAT, Locale.US)
        val d = sdf.parse(trip.date)
        sdf.applyPattern(NEW_FORMAT)
        newDate = sdf.format(d)

        val fullDate = "$newDate at ${trip.time}"
        holder.date.text = fullDate
        holder.origin.text = trip.origin
        holder.destination.text = trip.destination
        val vehiclePref = "${trip.vehicleModel} ${trip.vehicleColor} '${trip.vehicleYear}"
        if (trip.hasVehicleInfo){
            holder.vehicleInfo.text = vehiclePref
        }
        holder.luggageSize.text = trip.luggageSize
        if (trip.noSmoking){
            holder.noSmoking.visibility = View.VISIBLE
        } else {
            holder.noSmoking.visibility = View.GONE
        }
        if (trip.petsAllowed) {
            holder.petsAllowed.visibility = View.VISIBLE
        } else {
            holder.petsAllowed.visibility = View.GONE
        }
        val nbSeats = "${trip.numberOfSeats} seats left"
        holder.numberOfSeats.text = nbSeats
        val seatPrice = "${trip.pricePerSeat} DZD"
        holder.pricePerSeat.text = seatPrice
        if (trip.bookingPref == 0){
            holder.bookingPref.visibility = View.GONE
        } else {
            holder.bookingPref.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
       return  tripsList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.trip_card_layout, parent, false)

        return TripViewHolder(itemView)
    }

    open class TripViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var origin: TextView
        var destination: TextView
        var stops:ArrayList<String> = ArrayList()
        var numberOfStops = 0
        var date:TextView
        var vehicleInfo:TextView
        var luggageSize:TextView
        var noSmoking:ImageView
        var petsAllowed:ImageView
        var numberOfSeats:TextView
        var pricePerSeat:TextView
        var bookingPref:ImageView
        var vName: TextView
       //  var vDestination: TextView
         //var vDate: TextView
        // var vOrigin: TextView

        init {
            vName = v.findViewById(R.id.trip_fullname_card)
            date = v.findViewById(R.id.trip_date_card)
            origin = v.findViewById(R.id.trip_origin_card)
            destination = v.findViewById(R.id.trip_destination_card)
            vehicleInfo = v.findViewById(R.id.trip_vehicleperf_card)
            luggageSize = v.findViewById(R.id.trip_luggage_card)
            noSmoking = v.findViewById(R.id.trip_ic_smoke_card)
            petsAllowed = v.findViewById(R.id.trip_ic_pet_card)
            numberOfSeats = v.findViewById(R.id.trip_seats_card)
            pricePerSeat = v.findViewById(R.id.trip_price_card)
            bookingPref = v.findViewById(R.id.trip_ic_instant_card)
        }
    }
}

