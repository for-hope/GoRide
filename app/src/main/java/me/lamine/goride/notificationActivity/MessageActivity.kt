package me.lamine.goride.notificationActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message.*
import me.lamine.goride.R
import me.lamine.goride.dataObjects.Chat
import me.lamine.goride.dataObjects.User
import me.lamine.goride.interfaces.OnGetDataListener
import me.lamine.goride.utils.Database


class MessageActivity : AppCompatActivity() {
     private lateinit var mDatabase: Database
    private lateinit var messageAdapter: MessageAdapter
    private var mChat:MutableList<Chat> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        setSupportActionBar(findViewById(R.id.chat_toolbar))
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        mDatabase = Database()
        val userId:String = intent.getStringExtra("userId")
        var mUser : User?
        mDatabase.fetchUser(userId,object: OnGetDataListener{
            override fun onStart() {
               // setPb()
            }

            override fun onSuccess(data: DataSnapshot) {
                mUser = data.getValue(User::class.java)
                if (mUser != null){
                    msg_username.text = mUser?.fullName!!
                    Picasso.get().load(mUser?.profilePic).into(toolbar_icon)
                }
                readMessages(mDatabase.currentUserId(),userId,mUser?.profilePic!!)

            }

            override fun onFailed(databaseError: DatabaseError) {
              Toast.makeText(this@MessageActivity,"Error $databaseError", Toast.LENGTH_LONG).show()
                finish()
            }

        })
        chat_recycler_view.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        chat_recycler_view.layoutManager = linearLayoutManager




        //send msg
        msg_send_btn.setOnClickListener {
            val txt = msg_send_text.text.toString()
            if (txt != ""){
                mDatabase.sendMessage(mDatabase.currentUserId(),userId,txt)
                msg_send_text.text.clear()
            } else {
                Toast.makeText(this,"Type something first.",Toast.LENGTH_SHORT).show()
            }
        }



    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    private fun readMessages(myId:String,userId:String,imageUrl:String){
        mChat = mutableListOf()
        mDatabase.getReference("chats/",object :OnGetDataListener{
            override fun onStart() {

            }

            override fun onSuccess(data: DataSnapshot) {
                mChat.clear()
                for (snapshot in data.children){
                    val chat =snapshot.getValue(Chat::class.java)
                    var lastChat:Chat? = null
                        if (chat?.receiver == userId && chat.sender == myId || chat?.receiver == myId && chat.sender ==userId){
                            lastChat = chat
                            mChat.add(chat)
                    }
                    if(snapshot.key == data.children.last().key && mChat.size > 0){
                        if (lastChat != null) {
                            mDatabase.updateChatMetadata(lastChat,userId)
                            mDatabase.addToPath("chatlist/$userId/${mDatabase.currentUserId()}",lastChat)
                        }

                    }
                    messageAdapter = MessageAdapter(this@MessageActivity,mChat,imageUrl)
                    chat_recycler_view.adapter = messageAdapter
                }
            }

            override fun onFailed(databaseError: DatabaseError) {

            }

        })

    }


}
