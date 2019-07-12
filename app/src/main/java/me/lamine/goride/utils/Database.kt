package me.lamine.goride.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import me.lamine.goride.dataObjects.Chat
import me.lamine.goride.dataObjects.User
import me.lamine.goride.interfaces.OnGetDataListener
import me.lamine.goride.signActivity.LoginActivity
import kotlin.collections.HashMap

class Database {
    private var database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null

    init {
        mAuth = FirebaseAuth.getInstance()
        if (mAuth?.currentUser == null) {
            currentUser = null
        } else {
            currentUser = mAuth?.currentUser
        }
    }
    fun getFirebaseUser():FirebaseUser?{
        return currentUser
    }
    fun fetchFromTrip(tripId: String, otd: String, child: String, listener: OnGetDataListener) {
        listener.onStart()
        val ref = database.child("trips").child(otd).child(tripId).child(child)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listener.onSuccess(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException() // don't ignore errors
            }
        }
        ref.addListenerForSingleValueEvent(eventListener)
    }

    fun fetchUser(userId: String, listener: OnGetDataListener) {
        listener.onStart()
        val userRef = database.child("users").child(userId)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listener.onSuccess(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException() // don't ignore errors

            }
        }
        userRef.addListenerForSingleValueEvent(eventListener)
    }

    fun fetchTrip(tripId: String, otd: String, listener: OnGetDataListener) {
        listener.onStart()
        val userRef = database.child("trips").child(otd).child(tripId)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listener.onSuccess(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException() // don't ignore errors

            }
        }
        userRef.addListenerForSingleValueEvent(eventListener)
    }

    fun fetchTripsList(otd: String, listener: OnGetDataListener) {
        listener.onStart()
        val userRef = database.child("trips").child(otd)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listener.onSuccess(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException() // don't ignore errors

            }
        }
        userRef.addListenerForSingleValueEvent(eventListener)
    }

    fun fetchTripsList(otd: String, orderByChild: String, orderByValue: String, listener: OnGetDataListener) {
        listener.onStart()
        val userRef = database.child("trips").child(otd).orderByChild(orderByChild).startAt(orderByValue)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listener.onSuccess(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException() // don't ignore errors

            }
        }
        userRef.addListenerForSingleValueEvent(eventListener)
       // userRef.addValueEventListener(eventListener)
    }

    fun fetchTripRequestsList(otd: String, orderByChild: String, orderByValue: String, listener: OnGetDataListener) {
        listener.onStart()
        val userRef = database.child("tripRequests").child(otd).orderByChild(orderByChild).startAt(orderByValue)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listener.onSuccess(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException() // don't ignore errors

            }
        }
        userRef.addListenerForSingleValueEvent(eventListener)
    }
    fun fetchFromNotifications(child: String, listener: OnGetDataListener){
        listener.onStart()
        val ref = database.child("users").child(currentUser?.uid!!).child("notifications").child(child)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listener.onSuccess(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException()
            }
        }
        ref.addListenerForSingleValueEvent(eventListener)
    }
    fun fetchFromCurrentUser(child: String, listener: OnGetDataListener) {
        listener.onStart()
        val ref = database.child("users").child(currentUser?.uid!!).child(child)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listener.onSuccess(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException()
            }
        }
        ref.addListenerForSingleValueEvent(eventListener)
    }

    fun fetchTripRequest(tripRequestId: String, otd: String, listener: OnGetDataListener) {
        listener.onStart()
        val userRef = database.child("tripRequests").child(otd).child(tripRequestId)
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listener.onSuccess(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException() // don't ignore errors
            }
        }
        userRef.addListenerForSingleValueEvent(eventListener)
    }

    fun checkUserSession(activity: Activity?) {
        Log.i("initPcheck", "STARTEd")
        mAuth = FirebaseAuth.getInstance()
        if (mAuth?.currentUser == null) {
            activity?.finish()
            val intent = Intent(activity?.applicationContext,LoginActivity::class.java)
            activity?.startActivity(intent)

        } else {
            currentUser = mAuth?.currentUser
        }
    }

    fun signUserIn(){

    }

    fun addToPath(path: String, value: Any?) {
        val values = path.split("/")
        var ref = database.child(values[0])
        for ((index, child) in values.withIndex()) {
            if (index > 0) {
                ref = ref.child(child)
            }
                if (index == values.lastIndex) {
                    if (value != null){
                        ref.setValue(value)
                    }

                }


        }

    }
    fun fetchReport(listener: OnGetDataListener){
        listener.onStart()
        val ref = database.child("reports")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listener.onSuccess(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                listener.onFailed(databaseError)
                throw databaseError.toException() // don't ignore errors
            }
        }
        ref.addListenerForSingleValueEvent(eventListener)
    }
    fun removeFromPath(path: String) {
        val values = path.split("/")
        var ref = database.child(values[0])
        for ((index, child) in values.withIndex()) {
            if (index > 0) {
                ref = ref.child(child)
                if (index == values.lastIndex) {
                    ref.removeValue()
                }
            }

        }
    }
    fun pushKey(path: String): String {
        val values = path.split("/")
        var ref = database.child(values[0])
        for ((index, child) in values.withIndex()) {
            if (index > 0) {
                ref = ref.child(child)
            }
            if (index == values.lastIndex) {
                ref = ref.push()
            }

        }
        return ref.key!!
    }
    fun getReference(ref:String,listener: OnGetDataListener){
        val values = ref.split("/")
        var reference = database.child(values[0])
        for ((index, child) in values.withIndex()) {
            if (index > 0) {
                reference = reference.child(child)
            }
                if (index == values.lastIndex) {
                    listener.onStart()
                   // val reference = FirebaseDatabase.getInstance().getReference(ref)
                    reference.addValueEventListener(object:ValueEventListener{
                        override fun onDataChange(data: DataSnapshot) {
                            listener.onSuccess(data)
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                            listener.onFailed(databaseError)
                        }

                    })
                }


        }

    }
    fun sendMessage(sender:String,receiver:String,message:String){
        val chat = Chat(sender,receiver,message)
        val hashMap:HashMap<String,Any> = hashMapOf()
        hashMap["sender"] = sender
        hashMap["receiver"] = receiver
        hashMap["message"] = message
        hashMap["timestamp"] = System.currentTimeMillis()
        hashMap["isseen"] = false

        var path = "chats/"
        val key = pushKey(path)
        path = "chats/$key"
        Log.i("Path = ", path)
        this.addToPath(path,hashMap)
        updateChatMetadata(chat,receiver)
        /////
        //
        //

    }
    fun updateChatMetadata(chat: Chat,userId: String){
        val path = "chatlist/${this.currentUserId()}/$userId"
        this.addToPath(path,chat)
    }
    fun currentUserId(): String {
        return if (currentUser == null){
            if (FirebaseAuth.getInstance() == null){
                Log.i("NullObj", "Auth is null")
            }
            if (FirebaseAuth.getInstance().currentUser == null){
                Log.i("NullObj", "currentUser is null")
            }
            return ""
        } else {
            currentUser?.uid!!
        }

    }

    fun signOut(activity: Activity?) {
        Log.i("initPermissions", "STARTEd")
        if (activity != null) {
            saveUserIsLogged(activity.applicationContext, false)
            AuthUI.getInstance()
                .signOut(activity.applicationContext)
                .addOnCompleteListener {
                    val i = Intent(activity, LoginActivity::class.java)
                    activity.startActivity(i)
                    activity.finish()
                }
        }
    }

     private fun saveUserIsLogged(context: Context, isLogged: Boolean) {
        val mPref = context.getSharedPreferences("UserPref", Context.MODE_PRIVATE)!!
        val prefsEditor = mPref.edit()
        prefsEditor.putBoolean("isLogged", isLogged)
        prefsEditor.apply()
    }
     fun updateUserInApp(context: Context){
        fetchUser(currentUserId(),object:OnGetDataListener{
            override fun onStart() {

            }

            override fun onSuccess(data: DataSnapshot) {
                val user = data.getValue(User::class.java)
                if (user != null){
                saveSharedUser(context,user)
                }

            }

            override fun onFailed(databaseError: DatabaseError) {
                Toast.makeText(context, "Error ${databaseError.message}", Toast.LENGTH_LONG).show()
            }

        })
    }
 /*   fun savePasswordMeta(context: Context, password: String) {
        val mPref = context.getSharedPreferences("userPass", Context.MODE_PRIVATE)!!
        val prefsEditor = mPref.edit()
        prefsEditor.putString("pass",password)
        prefsEditor.apply()
    }*/



}