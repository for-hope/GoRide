package me.lamine.goride.notificationActivity

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_notifications.*
import me.lamine.goride.*
import me.lamine.goride.dataObjects.BookingNotification
import me.lamine.goride.dataObjects.ExtendedBookingNotif
import me.lamine.goride.dataObjects.LiteUser
import me.lamine.goride.interfaces.OnGetDataListener


/**
 * A simple [Fragment] subclass.
 */
class MessagesFragment : androidx.fragment.app.Fragment() {
    private var user: FirebaseUser? = null
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        user = FirebaseAuth.getInstance().currentUser
        if (user==null) {
            this.activity?.finish()
        }

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_messages, container, false)

    }
    private fun setEmptyUI(){
       empty_list_notif.visibility = View.VISIBLE
        scrolling.visibility = View.GONE
        setPb(0)
    }
    private fun setPb(visibility: Int){
        val mProgressBar = activity!!.findViewById<ProgressBar>(R.id.pb_notif)
        val mLayout = activity!!.findViewById<LinearLayout>(R.id.greyout_notif)
        if (visibility == 1) {
            mProgressBar.visibility = View.VISIBLE
            mLayout.visibility = View.VISIBLE

        } else {
            mProgressBar.visibility = View.GONE
            mLayout.visibility = View.GONE

        }
    }
}