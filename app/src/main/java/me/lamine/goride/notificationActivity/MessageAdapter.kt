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
import me.lamine.goride.dataObjects.Chat
import me.lamine.goride.utils.Database

class MessageAdapter(private var context: Context, private var mChat: List<Chat>, private var imageUrl:String): RecyclerView.Adapter<MessageAdapter.MessagesViewHolder>() {
    private lateinit var mDatabase:Database
    companion object {
        var msgTypeLeft:Int = 0
        var msgTypeRight:Int = 1
    }
    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
        mDatabase = Database()
        val chat = mChat[position]
        holder.showMessage.text = chat.message
        val link = imageUrl
        if (imageUrl!=""){
            Picasso.get().load(link).into(holder.profileimage)
        }
    }

    override fun getItemViewType(position: Int): Int {
        mDatabase = Database()
        return if (mChat[position].sender == mDatabase.currentUserId()){
            msgTypeRight
        } else {
            msgTypeLeft
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
        return if (viewType == msgTypeRight){
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.chat_item_right, parent, false)
            MessageAdapter.MessagesViewHolder(view).listen { position, type ->

            }
        } else {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.chat_item_left, parent, false)
            MessageAdapter.MessagesViewHolder(view).listen { position, type ->

            }
        }

    }
    override fun getItemCount(): Int {
        return mChat.size
    }




    private fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }

    open class MessagesViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val profileimage = v.findViewById<ImageView>(R.id.profile_img)
        val showMessage = v.findViewById<TextView>(R.id.show_msg)

    }
}