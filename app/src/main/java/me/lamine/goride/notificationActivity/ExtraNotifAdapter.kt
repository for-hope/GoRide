package me.lamine.goride.notificationActivity

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import me.lamine.goride.R
import me.lamine.goride.dataObjects.StandaredNotification
import me.lamine.goride.utils.wilayaArrayEN
import org.w3c.dom.Text


class ExtraNotifAdapter(private var context: Context, private var notify:List<StandaredNotification>): RecyclerView.Adapter<ExtraNotifAdapter.NotificationViewHolder>() {
    private lateinit var database: DatabaseReference
    private var currentUser: FirebaseUser? = null
    private var mAuth: FirebaseAuth? = null
    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        Log.i("NotifyMe3","adapter set")
        mAuth = FirebaseAuth.getInstance()
        if (mAuth?.currentUser == null) {
            Toast.makeText(context,"LOGIN FIRST",Toast.LENGTH_LONG).show()
            val activity = context as Activity
            activity.finish()
        }else {
            currentUser = mAuth?.currentUser
        }
        database = FirebaseDatabase.getInstance().reference
        if (notify[position].username == ""){
            holder.username.visibility = View.GONE
            holder.profilePicture.visibility = View.GONE
        } else{
            holder.username.text = notify[position].username
        }
        if (notify[position].trip != null){
            holder.tripDate.text =notify[position].trip!!.date
            val originToDes = "${notify[position].trip!!.originSubCity} to ${notify[position].trip!!.destSubCity}"
            holder.tripOriginAndDestination.text = originToDes
        } else {
            holder.tripDate.text = notify[position].timestamp
            holder.tripOriginAndDestination.visibility = View.GONE
        }


        val link = notify[position].profilePic
        if(link != ""){
            Picasso.get().load(link).into(holder.profilePicture)
        }
        when {
            notify[position].type == "unbookedUsers" -> {
                val ds1 = "Someone just unbooked! Check your trip feed to see who's left."
                val ds2 = "Check your trip feed to see who's left."
                holder.desc1.text = ds1
            }
            notify[position].type == "canceledTrips" -> {
                val desStr = notify[position].otd.substring(notify[position].otd.length - 2)
                val destination = wilayaArrayEN[desStr.toInt()-1]
                val ds1 = "Your trip to $destination have been canceled. Sorry about that. You can report the driver"
                holder.desc1.text = ds1

            }
            notify[position].type == "bookedUsers" -> {
                val ds1 = "Someone just booked! Check your trip feed to see more info."
                holder.desc1.text = ds1

            }
        }
        holder.timestamp.text = notify[position].timestamp
        Log.i("NotifyMe3","adapter set")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        Log.i("NotifyMe3","adapter set")
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.notif_standered_layout, parent, false)
        return NotificationViewHolder(view).listen { _, _ ->
           // val item = notify[pos]
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
            var username:TextView = v.findViewById(R.id.notif_username)
             var tripDate:TextView = v.findViewById(R.id.notif_date)
            var desc1:TextView = v.findViewById(R.id.notif_desc1)
            var desc2:TextView = v.findViewById(R.id.notif_desc2)
             var tripOriginAndDestination:TextView = v.findViewById(R.id.notif_origin)
             var profilePicture:ImageView = v.findViewById(R.id.notif_user_pfp)
            val timestamp:TextView = v.findViewById(R.id.timestamp_notif)

    }
}