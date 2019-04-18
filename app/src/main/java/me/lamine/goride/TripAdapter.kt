package me.lamine.goride

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater







class TripAdapter(private var tripsList: List<Trip>): RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val ci = tripsList[position]
        holder.vName.setText("Lamine Fet")
        holder.vDate.text = ci.date
        holder.vOrigin.text = ci.origin
        holder.vDestination.text = ci.destination

    }

    override fun getItemCount(): Int {
       return  tripsList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.trip_card_layout, parent, false)

        return TripViewHolder(itemView)
    }

    open class TripViewHolder(v: View) : RecyclerView.ViewHolder(v) {
         var vName: TextView
         var vDestination: TextView
         var vDate: TextView
         var vOrigin: TextView

        init {
            vName = v.findViewById(R.id.trip_fullname_card)
            vDate = v.findViewById(R.id.trip_date_card)
            vOrigin = v.findViewById(R.id.trip_origin_card)
            vDestination = v.findViewById(R.id.trip_destination_card)
        }
    }
}

