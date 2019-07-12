package me.lamine.goride.inboxActivity


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
import me.lamine.goride.dataObjects.ExtendedBookingNotif
import me.lamine.goride.utils.Database
import me.lamine.goride.utils.decodeWilaya
import me.lamine.goride.utils.wilayaArrayEN
import java.util.*


class NotificationAdapter(private var context: Context, private var notify: List<ExtendedBookingNotif>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {
    private lateinit var mDatabase: Database
    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        mDatabase = Database()
        holder.username.text = notify[position].username
        holder.tripDate.text = notify[position].date
        val origin = wilayaArrayEN[decodeWilaya(notify[position].origin)-1]
        val dest = wilayaArrayEN[decodeWilaya(notify[position].dest)-1]
        val originToDes = "$origin to $dest"
        holder.tripOriginAndDestination.text = originToDes
        val link = notify[position].profilePic
        Picasso.get().load(link).into(holder.profilePicture)
        holder.itemView.findViewById<CardView>(R.id.option_card_accept).setOnClickListener {
            acceptUser(position, true)

        }
        holder.itemView.findViewById<CardView>(R.id.option_card_decline).setOnClickListener {
            acceptUser(position, false)
        }
    }

    private fun reloadFragment() {
        // Reload current fragment
        try{
        val frg: Fragment?
        val fm = (context as AppCompatActivity).supportFragmentManager
        val notificationFragment = fm.fragments[0] as NotificationFragment
        frg = notificationFragment
        val ft = fm.beginTransaction()
        ft.detach(frg)
        ft.attach(frg)
        ft.commit()
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }

    private fun acceptUser(pos: Int, response: Boolean) {
        val item = notify[pos]
        val destCode = decodeWilaya(item.dest)
        val originCode = decodeWilaya(item.origin)
        val otdPath = "${String.format("%02d", originCode)}_${String.format("%02d", destCode)}"
        var path = "trips/$otdPath/${item.tripID}"
        if (response) {
            var mPath = "$path/bookedUsers/${item.userID}"
            mDatabase.addToPath(mPath, otdPath)
            val rootPath = "users/${item.userID}/bookedTrips/${item.tripID}"
            mDatabase.addToPath(rootPath, otdPath)

            mPath = "users/${item.userID}/notifications/acceptedTripRequests/${item.tripID}"
            mDatabase.addToPath("$mPath/otd",item.otd)
            mDatabase.addToPath("$mPath/timestamp", Date().toString())
        } else {
            val mPath = "users/${item.userID}/notifications/declinedTripRequests/${item.tripID}"
            mDatabase.addToPath("$mPath/otd",item.otd)
            mDatabase.addToPath("$mPath/timestamp", Date().toString())

        }
        val mPath = "users/${mDatabase.currentUserId()}/notifications/tripRequests/${item.tripID}/${item.userID}"
        mDatabase.removeFromPath(mPath)
        path = "$path/pendingBookedUsers/${item.userID}"
        mDatabase.removeFromPath(path)
        reloadFragment()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.booking_request_card, parent, false)
        return NotificationViewHolder(view).listen { _, _ ->
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

    }
}