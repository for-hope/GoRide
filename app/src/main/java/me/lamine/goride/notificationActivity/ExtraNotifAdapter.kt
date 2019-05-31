package me.lamine.goride.notificationActivity


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import me.lamine.goride.R
import me.lamine.goride.dataObjects.StandaredNotification
import me.lamine.goride.utils.wilayaArrayEN


class ExtraNotifAdapter(private var notify: List<StandaredNotification>) :
    RecyclerView.Adapter<ExtraNotifAdapter.NotificationViewHolder>() {
    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        if (notify[position].username == "") {
            holder.username.visibility = View.GONE
            holder.profilePicture.visibility = View.GONE
        } else {
            holder.username.text = notify[position].username
        }
        if (notify[position].trip != null) {
            holder.tripDate.text = notify[position].trip!!.date
            val originToDes = "${notify[position].trip!!.originSubCity} to ${notify[position].trip!!.destSubCity}"
            holder.tripOriginAndDestination.text = originToDes
        } else {
            holder.tripDate.text = notify[position].timestamp
            holder.tripOriginAndDestination.visibility = View.GONE
        }

        val link = notify[position].profilePic
        if (link != "") {
            Picasso.get().load(link).into(holder.profilePicture)
        }
        when {
            notify[position].type == "unbookedUsers" -> {
                val ds1 = "Someone just unbooked! Check your trip feed to see who's left."
                holder.desc1.text = ds1
                holder.notifIcon.setImageResource(R.drawable.ic_error_outline_gray_50dp)
            }
            notify[position].type == "canceledTrips" -> {
                val desStr = notify[position].otd.substring(notify[position].otd.length - 2)
                val destination = wilayaArrayEN[desStr.toInt() - 1]
                val ds1 = "Your trip to $destination have been canceled. Sorry about that. You can report the driver"
                holder.desc1.text = ds1
                holder.notifIcon.setImageResource(R.drawable.ic_cancel_gray_50dp)

            }
            notify[position].type == "bookedUsers" -> {
                Log.i("NotifHere","TEST")
                val ds1 = "Someone just booked! Check your trip feed to see more info."
                holder.desc1.text = ds1
                holder.notifIcon.setImageResource(R.drawable.ic_person_add_gray_50dp)
            }
            notify[position].type == "acceptedDriveRequest" -> {
                val ds1 = "Your request to drive a trip got accepted! check your feed for more info."
                holder.desc1.text = ds1
                holder.notifIcon.setImageResource(R.drawable.ic_check_circle_gray_50p)
            }
            notify[position].type == "modifiedTrips" -> {
                val desStr = notify[position].otd.substring(notify[position].otd.length - 2)
                val destination = wilayaArrayEN[desStr.toInt() - 1]
                val ds1 = "Your trip to $destination have been modified. check your trip for more info!"
                holder.desc1.text = ds1
                holder.notifIcon.setImageResource(R.drawable.ic_error_outline_gray_50dp)
            }
            notify[position].type == "declinedTripRequests" -> {
                val desStr = notify[position].otd.substring(notify[position].otd.length - 2)
                val destination = wilayaArrayEN[desStr.toInt() - 1]
                val ds1 = "Your request to the trip to $destination have been rejected. you can always search for more!"
                holder.desc1.text = ds1
                holder.notifIcon.setImageResource(R.drawable.ic_cancel_gray_50dp)
            }
            notify[position].type == "acceptedTripRequests" -> {
                val desStr = notify[position].otd.substring(notify[position].otd.length - 2)
                val destination = wilayaArrayEN[desStr.toInt() - 1]
                val ds1 = "Your request to the trip to $destination have been accepted. check your trips feed for more information!"
                holder.desc1.text = ds1
                holder.notifIcon.setImageResource(R.drawable.ic_check_circle_gray_50p)
            }



        }
        holder.timestamp.text = notify[position].timestamp
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.notif_standered_layout, parent, false)
        return NotificationViewHolder(view).listen { _, _ ->
        }
    }

    override fun getItemCount(): Int {
        return notify.size
    }

    private fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }

    open class NotificationViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var username: TextView = v.findViewById(R.id.notif_username)
        var tripDate: TextView = v.findViewById(R.id.notif_date)
        var desc1: TextView = v.findViewById(R.id.notif_desc1)
        var desc2: TextView = v.findViewById(R.id.notif_desc2)
        var tripOriginAndDestination: TextView = v.findViewById(R.id.notif_origin)
        var profilePicture: ImageView = v.findViewById(R.id.notif_user_pfp)
        val timestamp: TextView = v.findViewById(R.id.timestamp_notif)
        val notifIcon: ImageView = v.findViewById(R.id.notif_ic)

    }
}