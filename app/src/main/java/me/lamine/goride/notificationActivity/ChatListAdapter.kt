package me.lamine.goride.notificationActivity

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import me.lamine.goride.R
import me.lamine.goride.utils.Database
import android.app.Activity
import android.text.format.DateUtils
import android.util.Log
import me.lamine.goride.dataObjects.ChatListInfo
import java.lang.NumberFormatException
import java.util.*


class ChatListAdapter(private var context: Context, private var chatList: List<ChatListInfo>): RecyclerView.Adapter<ChatListAdapter.MessagesViewHolder>() {
    private lateinit var mDatabase:Database
    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
        mDatabase = Database()
        val chatUser = chatList[position].mUser
        val chatSession = chatList[position]
        holder.username.text = chatUser.fullName
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = chatSession.timestamp
       /* val mYear = calendar.get(Calendar.YEAR)
        val mMonth = calendar.get(Calendar.MONTH)
        val mDay = calendar.get(Calendar.DAY_OF_MONTH)*/

        val timezone = TimeZone.getDefault()
        val cal = GregorianCalendar(timezone)
        cal.timeInMillis = chatSession.timestamp
       // val monthName = cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())//Locale.US);
        val dayName = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault())//Locale.US);
        val hourOfDay = cal.get(Calendar.HOUR_OF_DAY)
        val minuteOfDay = cal.get(Calendar.MINUTE)
        val time = calendar.time

        var dayString = DateUtils.getRelativeTimeSpanString(time.time,Date().time,DateUtils.DAY_IN_MILLIS).toString()
        var daysAgo = -1
        try {
            daysAgo = Integer.parseInt(dayString.replace(" days ago", ""))
        } catch (e:NumberFormatException){
            e.stackTrace
            Log.i("ErrorDate", e.localizedMessage)
        }
        if (dayString == "Today"){
            dayString = ""
        }
        var date = "$dayString $hourOfDay:$minuteOfDay"
        Log.i("Date","normal $date")
        if(daysAgo in 2..7) {
            dayString = dayName
            date = "$dayString $hourOfDay:$minuteOfDay"
            Log.i("Date","dayOfWeek")
        } else if((dayString != "" && dayString!= "Yesterday") || dayString.contains(" days ago")){
            date = dayString
            Log.i("Date","restOfDays")
        }
        holder.timestamp.text = date
        holder.lastMsg.text = chatSession.lastMsg
        val link = chatUser.profilePic
        Picasso.get().load(link).into(holder.profilePicture)
        holder.itemView.setOnClickListener {
            val intent = Intent(context,MessageActivity::class.java)
            intent.putExtra("userId",chatUser.userId)
            context.startActivity(intent)
            Log.i("ActivityAdapter","Finished")
       //     (context as Activity).finish()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.user_msg_layout, parent, false)
        return ChatListAdapter.MessagesViewHolder(view).listen { position, type ->

        }
    }
    override fun getItemCount(): Int {
       return chatList.size
    }




    private fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }

    open class MessagesViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val username: TextView = v.findViewById(R.id.chat_username)
        val timestamp : TextView = v.findViewById(R.id.chat_timestamp)
        val profilePicture:ImageView = v.findViewById(R.id.chat_user_pfp)
        val lastMsg:TextView = v.findViewById(R.id.chat_last_msg)

    }
}