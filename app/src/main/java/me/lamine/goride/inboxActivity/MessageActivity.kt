package me.lamine.goride.inboxActivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*

import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.chat_item_left.*
import me.lamine.goride.R
import me.lamine.goride.dataObjects.Chat
import me.lamine.goride.dataObjects.User
import me.lamine.goride.interfaces.OnGetDataListener
import me.lamine.goride.notifications.*
import me.lamine.goride.userActivity.UserActivity
import me.lamine.goride.utils.Database
import me.lamine.goride.utils.getSharedUser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MessageActivity : AppCompatActivity() {
     private lateinit var mDatabase: Database
    private lateinit var messageAdapter: MessageAdapter
    lateinit var apiService:APIService
    private var mChat:MutableList<Chat> = mutableListOf()
    private var notify = false
    private var mUser:User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        setSupportActionBar(findViewById(R.id.chat_toolbar))
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        apiService = Client.getClient("https://fcm.googleapis.com/")?.create(APIService::class.java)!!
        mDatabase = Database()
        val userId:String = intent.getStringExtra("userId")

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
            notify=  true
            val txt = msg_send_text.text.toString()
            if (txt != "" && txt.length < 600){
                mDatabase.sendMessage(mDatabase.currentUserId(),userId,txt)
                val msg = txt
                val mUserObject = getSharedUser(this)
                if (notify){
                    if (mUserObject != null) {
                        sendNotification(userId,mUserObject.fullName,msg)
                    } else {
                        Toast.makeText(this, "Check your internet connection", Toast.LENGTH_SHORT).show()
                        mDatabase.signOut(this)
                    }
                }
                notify = false
                msg_send_text.text.clear()
            } else {
                if (txt == ""){
                Toast.makeText(this,"Type something first.",Toast.LENGTH_SHORT).show()
                } else {
                Toast.makeText(this,"Message too long.",Toast.LENGTH_SHORT).show()
                }
            }
        }



    }

    private fun sendNotification(userId: String, fullName: String, msg: String) {
        Log.i("NotificationCompact_MA", "SendNotification")
        val tokens:DatabaseReference = FirebaseDatabase.getInstance().getReference("Tokens")
        val query:Query = tokens.orderByKey().equalTo(userId)
        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.i("NotificationCompact_MA", "Query")
                for (data in dataSnapshot.children){
                    Log.i("NotificationCompact_MA", "token")
                    val token: Token = data.getValue(Token::class.java)!!
                    val data: Data = Data(mDatabase.currentUserId(),R.mipmap.ic_launcher, "$fullName : $msg", userId)
                    val sender: Sender = Sender(data,token.token)
                    apiService.sendNotification(sender).enqueue(object : Callback<MyResponse> {
                        override fun onResponse(call: Call<MyResponse>, response: Response<MyResponse>) {
                            Log.i("NotificationCompact_MA", "API")
                            if (response.code() == 200){
                            if (response.body()?.success != 1){
                                Toast.makeText(this@MessageActivity,"Failed", Toast.LENGTH_SHORT).show()

                            }
                            }

                        }

                        override fun onFailure(call: Call<MyResponse>, t: Throwable) {
                          Toast.makeText(this@MessageActivity.applicationContext,"Internet Error ${t.localizedMessage}", Toast.LENGTH_LONG).show()
                        }

                    })
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                    Log.i("MessageActivity", databaseError.message)
            }

        })

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
                    messageAdapter = MessageAdapter(this@MessageActivity,mChat,imageUrl,mUser!!)
                    chat_recycler_view.adapter = messageAdapter

                }
            }

            override fun onFailed(databaseError: DatabaseError) {

            }

        })

    }


}
