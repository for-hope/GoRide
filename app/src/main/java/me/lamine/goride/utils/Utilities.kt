package me.lamine.goride.utils

import android.content.Context
import android.view.View
import android.widget.ProgressBar
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.gson.Gson
import me.lamine.goride.dataObjects.User
import me.lamine.goride.interfaces.OnGetDataListener

import android.net.NetworkInfo
import androidx.core.content.ContextCompat.getSystemService
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


private const val apiKey: String = "AIzaSyDWbc3KQP6ssBlClf8HSiZWEtMxfwqSYto"
fun checkNotifications(listener: OnGetDataListener) {
    listener.onStart()
    Database().fetchFromCurrentUser("notifications", object : OnGetDataListener {
        override fun onSuccess(data: DataSnapshot) {

                listener.onSuccess(data)

        }
        override fun onStart() {

        }

        override fun onFailed(databaseError: DatabaseError) {

        }

    })
}
fun setPb(emptyView: View?, progressBar: ProgressBar, grayLayout: View, visibility: Int) {

    if (visibility == 1) {
        if (emptyView != null) {
            emptyView.visibility = View.GONE
        }
        progressBar.visibility = View.VISIBLE
        grayLayout.visibility = View.VISIBLE

    } else {
        if (emptyView != null) {
            emptyView.visibility = View.VISIBLE
        }
        progressBar.visibility = View.GONE
        grayLayout.visibility = View.GONE
        // trip_ac_submit_btn.visibility = View.VISIBLE

    }
}
fun getUrl(from: LatLng, to: LatLng): String {
    val origin = "origin=" + from.latitude + "," + from.longitude
    val dest = "destination=" + to.latitude + "," + to.longitude
    val sensor = "sensor=false"
    val params = "$origin&$dest&$sensor"
    return "https://maps.googleapis.com/maps/api/directions/json?$params&key=$apiKey"
}

//decode Poly to draw line
fun decodePoly(encoded: String): List<LatLng> {
    val poly = ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].toInt() - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].toInt() - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng

        val p = LatLng(
            lat.toDouble() / 1E5,
            lng.toDouble() / 1E5
        )
        poly.add(p)
    }

    return poly
}
fun getSharedUser(context:Context): User {
        var isDone :Boolean = false
        var mUser: User? = null
        val mPrefs = context.getSharedPreferences("TripsPref", Context.MODE_PRIVATE)!!
        val gson = Gson()
        val json = mPrefs.getString("currentUser", "")
        if (json != "") {
            val obj = gson.fromJson<User>(json, User::class.java)
            return obj
        }else {
            Database().fetchUser(Database().currentUserId(), object : OnGetDataListener{
                override fun onStart() {

                }

                override fun onSuccess(data: DataSnapshot) {
                   isDone = true
                    mUser = data.getValue(User::class.java) as User
                    saveSharedUser(context, mUser!!)
                }

                override fun onFailed(databaseError: DatabaseError) {
                    isDone = true
                }

            })
        }
     return mUser!!

    }
    fun verifyAvailableNetwork(activity: AppCompatActivity):Boolean{
      val connectivityManager=activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
      val networkInfo=connectivityManager.activeNetworkInfo
      return  networkInfo!=null && networkInfo.isConnected
    }
     fun getAge(dobString: String): Int {

    var date: Date? = null
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    try {
        date = sdf.parse(dobString)
    } catch (e: ParseException) {
        e.printStackTrace()
    }

    if (date == null) return 0

    val dob = Calendar.getInstance()
    val today = Calendar.getInstance()

    dob.time = date

    val year = dob.get(Calendar.YEAR)
    val month = dob.get(Calendar.MONTH)
    val day = dob.get(Calendar.DAY_OF_MONTH)

    dob.set(year, month + 1, day)

    var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)

    if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
        age--
    }



    return age
}

fun saveSharedUser(context:Context,user: User){
    val mPrefs = context.getSharedPreferences("TripsPref", Context.MODE_PRIVATE)!!
    val prefsEditor = mPrefs.edit()
    //val nbTrips = mPrefs.getInt("savedTrips",0)
    val gson = Gson()
    val json = gson.toJson(user)
    val currentUserStr = "currentUser"
    prefsEditor.putString(currentUserStr, json)
    prefsEditor.apply()
    saveUserIsLogged(context,true)
}
fun saveUserIsLogged(context:Context,isLogged:Boolean){
    //todo
    val mPref = context.getSharedPreferences("UserPref", Context.MODE_PRIVATE)!!
    val prefsEditor = mPref.edit()
    prefsEditor.putBoolean("isLogged",isLogged)
    prefsEditor.apply()
}
//---------//////
// Request Activity high
// IntroActivity medium
// Google Sign-in low
// Notifications medium


