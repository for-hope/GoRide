package me.lamine.goride.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import me.lamine.goride.inboxActivity.MessageActivity
import com.google.android.libraries.places.internal.e



class MyFirebaseMessaging: FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)

        val sented: String = remoteMessage?.data?.get("sented")!!
        val mUser:FirebaseUser? = FirebaseAuth.getInstance().currentUser
        if (mUser != null && sented == mUser.uid){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                sendOreoNotification(remoteMessage)
            } else {
            sendNotifications(remoteMessage)}

        }
    }

    private fun sendOreoNotification(remoteMessage: RemoteMessage) {
        val user: String? = remoteMessage.data["user"]
        val icon: String? = remoteMessage.data["icon"]
        Log.i("icon_notif", icon)
        val title: String? = remoteMessage.data["title"]
//        Log.i("icon_notif", title)
        val body: String? = remoteMessage.data["body"]
        Log.i("icon_notif", body)

        //val notification:RemoteMessage.Notification = remoteMessage.notification!!
        val j:Int = Integer.parseInt(user?.replace("[^0-9]".toRegex(),"")!!)
        val intent = Intent(this,MessageActivity::class.java)
        val bundle = Bundle()
        bundle.putString("userId",user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this,j,intent,PendingIntent.FLAG_ONE_SHOT)
        Log.i("ServiceNotif","Msg Received")
        val defaultSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

       val oreoNotification = OreoNotification(this)
        val builder:Notification.Builder? = oreoNotification.getOreoNotification("New Message!",body!!,pendingIntent,defaultSound,icon!!)

        var i = 0
        if (j > 0){
            i=j
        }
        oreoNotification.getManager().notify(i,builder?.build())
    }

    private fun sendNotifications(remoteMessage: RemoteMessage) {
        val user: String? = remoteMessage.data["user"]
        val icon: String? = remoteMessage.data["icon"]
        val title: String? = remoteMessage.data["title"]
        val body: String? = remoteMessage.data["body"]

        //val notification:RemoteMessage.Notification = remoteMessage.notification!!
        val j:Int = Integer.parseInt(user?.replace("[^0-9]".toRegex(),"")!!)
        val intent = Intent(this,MessageActivity::class.java)
        val bundle = Bundle()
        bundle.putString("userId",user)
        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this,j,intent,PendingIntent.FLAG_ONE_SHOT)
        Log.i("ServiceNotif","Msg Received")
       val defaultSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder:NotificationCompat.Builder = NotificationCompat.Builder(this)
            .setSmallIcon(Integer.parseInt(icon!!))
            .setContentTitle("New Message!")
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSound)
            .setContentIntent(pendingIntent)
        val notif:NotificationManager= getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var i = 0
        if (j > 0){
            i=j
        }
       notif.notify(i,builder.build())
    }

    override fun onNewToken(s: String?) {
        super.onNewToken(s)
        Log.e("NEW_TOKEN", s)
    }
}