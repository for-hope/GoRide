package me.lamine.goride

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso


class NotificationAdapter(private var context: Context, private var notif:List<ExtendedBookingNotif>): RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {
    override fun onBindViewHolder(holder: NotificationAdapter.NotificationViewHolder, position: Int) {
       holder.username.text = notif[position].username
        holder.tripDate.text =notif[position].date
        val originToDes = "${notif[position].origin} to ${notif[position].dest}"
        holder.tripOriginAndDestination.text = originToDes
        val link = "https://firebasestorage.googleapis.com/v0/b/ridego-1555252117345.appspot.com/o/user_images%2Fmale_default.png?alt=media&token=658a01b5-ba18-4491-8aa7-e15c30284200"
        Picasso.get().load(link).into(holder.profilePicure)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.booking_request_card, parent, false)
        return NotificationAdapter.NotificationViewHolder(view).listen { pos, type ->
            val item = notif[pos]
            val userItem = notif[pos]
            Toast.makeText(view.context,"item is ${item.dest}", Toast.LENGTH_SHORT).show()
            view.findViewById<CardView>(R.id.booking_notif).setOnClickListener {
                Toast.makeText(view.context,"item is ${item.dest}", Toast.LENGTH_SHORT).show() }

        }
    }


    override fun getItemCount(): Int {
       return notif.size
    }
    private fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }

    open class NotificationViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            var username:TextView = v.findViewById(R.id.booking_username)
             var tripDate:TextView = v.findViewById(R.id.booking_date)
             var tripOriginAndDestination:TextView = v.findViewById(R.id.booking_origin)
             var profilePicure:ImageView = v.findViewById(R.id.booking_pfp)

    }
}