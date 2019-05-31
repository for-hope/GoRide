package me.lamine.goride.utils

import android.view.View
import android.widget.ProgressBar
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import me.lamine.goride.interfaces.OnGetDataListener
import java.util.ArrayList

private val apiKey: String = "AIzaSyDWbc3KQP6ssBlClf8HSiZWEtMxfwqSYto"
fun checkNotifications(listener: OnGetDataListener) {
    listener.onStart()
    Database().fetchFromCurrentUser("notifications", object : OnGetDataListener {
        override fun onSuccess(data: DataSnapshot) {
            if (data.childrenCount.toInt() != 0) {
                listener.onSuccess(data)
            } else {
                Database().fetchFromCurrentUser("tripRequests", object : OnGetDataListener {
                    override fun onSuccess(data: DataSnapshot) {
                        if (data.childrenCount.toInt() != 0) {
                            listener.onSuccess(data)
                        } else {
                            Database().fetchFromCurrentUser("driveRequests", object : OnGetDataListener {
                                override fun onSuccess(data: DataSnapshot) {
                                    listener.onSuccess(data)
                                }

                                override fun onStart() {

                                }

                                override fun onFailed(databaseError: DatabaseError) {

                                }

                            })
                        }
                    }

                    override fun onStart() {

                    }

                    override fun onFailed(databaseError: DatabaseError) {

                    }

                })
            }
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
