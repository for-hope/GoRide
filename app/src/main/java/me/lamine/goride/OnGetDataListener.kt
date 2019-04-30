package me.lamine.goride

import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot



interface OnGetDataListener {
    fun onStart()
    fun onSuccess(data: DataSnapshot)
    fun onFailed(databaseError: DatabaseError)
}