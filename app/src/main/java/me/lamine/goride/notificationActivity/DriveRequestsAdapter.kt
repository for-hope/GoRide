package me.lamine.goride.notificationActivity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import me.lamine.goride.R
import me.lamine.goride.dataObjects.DriveNotification
import me.lamine.goride.utils.Database
import me.lamine.goride.utils.wilayaArrayEN
import java.util.*


class DriveRequestsAdapter(private var context: Context, private var notify: List<DriveNotification>) :
    RecyclerView.Adapter<DriveRequestsAdapter.NotificationViewHolder>() {
    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.username.text = notify[position].userObject.fullName
        holder.tripDate.text = notify[position].date
        val otd = notify[position].otd
        val originCode = otd.substring(0, Math.min(otd.length, 2)).toInt()
        val destCode = otd.substring(Math.max(otd.length - 2, 0)).toInt()
        val originToDes = "${wilayaArrayEN[originCode - 1]} to ${wilayaArrayEN[destCode - 1]}"
        holder.tripOriginAndDestination.text = originToDes
        val link = notify[position].userObject.profilePic
        Picasso.get().load(link).into(holder.profilePicture)
        val desc1 = " accepted your request!"
        holder.desc1.text = desc1
        val desc2 = "Do you wanna go on a ride with them?"
        holder.desc2.text = desc2
        holder.itemView.findViewById<CardView>(R.id.option_card_accept).setOnClickListener {
            acceptUser(position, true)

        }
        holder.itemView.findViewById<CardView>(R.id.option_card_decline).setOnClickListener {
            acceptUser(position, false)
        }
    }

    private fun reloadFragment() {
        // Reload current fragment
        val frg: Fragment?
        val fm = (context as AppCompatActivity).supportFragmentManager
        val notificationFragment = fm.fragments[0] as NotificationFragment
        frg = notificationFragment
        val ft = fm.beginTransaction()
        ft.detach(frg)
        ft.attach(frg)
        ft.commit()
    }

    private fun acceptUser(pos: Int, response: Boolean) {
        val item = notify[pos]
        val otd = item.otd
        val originCode = otd.substring(0, Math.min(otd.length, 2)).toInt()
        val destCode = otd.substring(Math.max(otd.length - 2, 0)).toInt()
        val otdPath = "${String.format("%02d", originCode)}_${String.format("%02d", destCode)}"
        val path = "tripRequests/$otdPath/${item.tripId}"
        if (response) {
            var mPath = "$path/acceptedDriver/${item.userId}"
            Database().addToPath(mPath, 1)
            mPath = "users/${item.userId}/notifications/acceptedDriveRequest/${item.tripId}"
            Database().addToPath("$mPath/otd", otdPath)
            Database().addToPath("$mPath/timestamp", Date().toString())
        }
        val mPath = "users/${Database().currentUserId()}/driveRequests/${item.tripId}/${item.userId}"
        Database().removeFromPath(mPath)
        reloadFragment()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.booking_request_card, parent, false)
        return NotificationViewHolder(view).listen { _, _ ->
            // val item = notify[pos]
            Toast.makeText(context, "Accept or decline user.", Toast.LENGTH_SHORT).show()
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
        var username: TextView = v.findViewById(R.id.booking_username)
        var tripDate: TextView = v.findViewById(R.id.booking_date)
        var tripOriginAndDestination: TextView = v.findViewById(R.id.booking_origin)
        var profilePicture: ImageView = v.findViewById(R.id.booking_pfp)
        var desc1: TextView = v.findViewById(R.id.bookingpref_desc)
        var desc2: TextView = v.findViewById(R.id.bookingpref_desc2)

    }
}