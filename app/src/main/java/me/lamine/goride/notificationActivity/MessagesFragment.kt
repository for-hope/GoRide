package me.lamine.goride.notificationActivity

import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.fragment_messages.*
import kotlinx.android.synthetic.main.fragment_notifications.*
import kotlinx.android.synthetic.main.fragment_notifications.scrolling
import kotlinx.android.synthetic.main.fui_activity_invisible.*
import me.lamine.goride.*
import me.lamine.goride.dataObjects.Chat
import me.lamine.goride.dataObjects.ChatListInfo
import me.lamine.goride.dataObjects.User
import me.lamine.goride.interfaces.OnGetDataListener
import me.lamine.goride.utils.Database


/**
 * A simple [Fragment] subclass.
 */

class MessagesFragment : Fragment() {
    private lateinit var mDatabase: Database
    private var listOfUserIds:MutableList<String> = mutableListOf()
    private var listOfUser:MutableList<User> = mutableListOf()
    private var listOfChats:MutableList<ChatListInfo> = mutableListOf()
    private var isDestroyed=false
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        user_messages_list_res_view.setHasFixedSize(true)
        user_messages_list_res_view.layoutManager = LinearLayoutManager(this.context)
        listOfUserIds = mutableListOf()
        mDatabase = Database()
        Log.i("Fragment","CREATED")
        mDatabase.getReference("chatlist/${mDatabase.currentUserId()}",object :OnGetDataListener{
            override fun onStart() {
            setPb(1)
            }

            override fun onSuccess(data: DataSnapshot) {
                listOfChats = mutableListOf()
                Log.i("Added adapter","Trigger2")
                if (!isDestroyed){
                    listOfUserIds.clear()
                    val mData = data
                    for(snapshot in mData.children){
                        Log.i("data",mData.childrenCount.toString())
                        mDatabase.fetchUser(snapshot.key!!,object :OnGetDataListener{
                            override fun onStart() {

                            }

                            override fun onSuccess(data: DataSnapshot) {
                                val chatListInfo = ChatListInfo(snapshot.key!!)
                                chatListInfo.mUser = data.getValue(User::class.java)!!
                                chatListInfo.lastMsg = snapshot.child("message").value as String
                                chatListInfo.timestamp = snapshot.child("timestamp").value as Long
                                chatListInfo.isSender = snapshot.child("sender").value as String == mDatabase.currentUserId()
                                if (!listOfChats.contains(chatListInfo)){
                                    listOfChats.add(chatListInfo)
                                }

                                if (snapshot.key == mData.children.last().key && user_messages_list_res_view != null){
                                    Log.i("Added adapter","Trigger")
                                    val userAdapter = ChatListAdapter(this@MessagesFragment.context!!, listOfChats)
                                    user_messages_list_res_view.adapter = userAdapter
                                    setPb(0)
                                }

                            }

                            override fun onFailed(databaseError: DatabaseError) {

                            }

                        })
                    }
                }
            }

            override fun onFailed(databaseError: DatabaseError) {

            }

        })


    }

/*    private fun readChats(){
       listOfUser = mutableListOf()
        Log.i("listOfUsersIds",listOfUserIds.toString())
        for (userId in listOfUserIds){
            mDatabase.fetchUser(userId,object :OnGetDataListener{
                override fun onStart() {

                }

                override fun onSuccess(data: DataSnapshot) {
                    val mUser = data.getValue(User::class.java)
                   if (mUser != null){
                       listOfUser.add(mUser)
                   }
                    if (userId == listOfUserIds.last()){
                        val userAdapter = ChatListAdapter(this@MessagesFragment.context!!,listOfUser)
                        user_messages_list_res_view.adapter = userAdapter
                    }


                }

                override fun onFailed(databaseError: DatabaseError) {

                }

            })
        }

        //mDatabase.fetchUser()
    }*/
    override fun onDestroy() {
        super.onDestroy()
        isDestroyed = true
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_messages, container, false)

    }

    private fun setEmptyUI() {
        empty_list_notif.visibility = View.VISIBLE
        scrolling.visibility = View.GONE
        //setPb(0)
    }

    private fun setPb(visibility: Int) {
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