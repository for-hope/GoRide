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
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import me.lamine.goride.dataObjects.ExtendedBookingNotif
import me.lamine.goride.R
import me.lamine.goride.utils.decodeWilaya
import androidx.appcompat.app.AppCompatActivity




class NotificationAdapter(private var context: Context, private var notify:List<ExtendedBookingNotif>): RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {
    private lateinit var database: DatabaseReference
    private var currentUser: FirebaseUser? = null
    private var mAuth: FirebaseAuth? = null
    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        mAuth = FirebaseAuth.getInstance()
        if (mAuth?.currentUser == null) {
            Toast.makeText(context,"LOGIN FIRST",Toast.LENGTH_LONG).show()
            val activity = context as Activity
            activity.finish()
        }else {
            currentUser = mAuth?.currentUser
        }
        database = FirebaseDatabase.getInstance().reference
        holder.username.text = notify[position].username
        holder.tripDate.text =notify[position].date
        val originToDes = "${notify[position].origin} to ${notify[position].dest}"
        holder.tripOriginAndDestination.text = originToDes
        val link = notify[position].profilePic
        Picasso.get().load(link).into(holder.profilePicture)
        holder.itemView.findViewById<CardView>(R.id.option_card_accept).setOnClickListener {
            acceptUser(position,true)

        }
        holder.itemView.findViewById<CardView>(R.id.option_card_decline).setOnClickListener {
            acceptUser(position,false)
        }
    }
    private fun reloadFragment(){
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
    private fun acceptUser(pos:Int,response:Boolean){
        val item = notify[pos]
        val destCode = decodeWilaya(item.dest)
        val originCode = decodeWilaya(item.origin)
        val newRef = database.child("trips").child("${originCode}_$destCode").child(item.tripID)
        if (response){

        newRef.child("bookedUsers").child(item.userID).setValue(1){ databaseError, _ ->
            if (databaseError != null) {
                Log.i("FireBaseEroor",databaseError.message)
                Toast.makeText(context, "Error $databaseError", Toast.LENGTH_LONG).show()}
        }
        database.push()
        }
        val userRef = database.child("users").child(currentUser?.uid!!).child("tripRequests").child(item.tripID).child(item.userID)
        val pendingUsersRef = newRef.child("pendingBookedUsers").child(item.userID)
        pendingUsersRef.removeValue()
        userRef.removeValue()
        reloadFragment()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.booking_request_card, parent, false)
        return NotificationViewHolder(view).listen { _, _ ->
           // val item = notify[pos]
            Toast.makeText(context,"Accept or decline user.",Toast.LENGTH_SHORT).show()
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
            var username:TextView = v.findViewById(R.id.booking_username)
             var tripDate:TextView = v.findViewById(R.id.booking_date)
             var tripOriginAndDestination:TextView = v.findViewById(R.id.booking_origin)
             var profilePicture:ImageView = v.findViewById(R.id.booking_pfp)

    }
}