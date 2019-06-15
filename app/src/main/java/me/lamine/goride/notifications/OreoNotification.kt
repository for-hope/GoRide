package me.lamine.goride.notifications

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.os.Build
import org.jetbrains.anko.notificationManager

class OreoNotification(base: Context?) : ContextWrapper(base) {
    private var notificationManager:NotificationManager? = null
    companion object {
        val channel_id = "me.lamine.goride"
        val channel_name = "goride"
    }
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel()
        }
    }
    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel() {
    val channel = NotificationChannel(channel_id, channel_name,NotificationManager.IMPORTANCE_DEFAULT)
        channel.enableLights(false)
        channel.enableVibration(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        getManager().createNotificationChannel(channel)
    }
    public fun getManager():NotificationManager{
      if (notificationManager == null){
          notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      }
        return notificationManager!!
    }
    @TargetApi(Build.VERSION_CODES.O)
   fun getOreoNotification(title:String, body:String, pendingIntent: PendingIntent, soundUri: Uri,icon:String ):Notification.Builder{
        return Notification.Builder(applicationContext, channel_id)
            .setContentIntent(pendingIntent)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(Integer.parseInt(icon))
            .setSound(soundUri)
            .setAutoCancel(true)
    }


}