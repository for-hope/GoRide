package me.lamine.goride

import android.content.Context

import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.Nullable

import androidx.recyclerview.widget.RecyclerView
import com.otaliastudios.autocomplete.AutocompletePresenter
import com.otaliastudios.autocomplete.RecyclerViewPresenter

import java.util.ArrayList


class UserPresenter(context: Context) : RecyclerViewPresenter<User>(context) {

    protected lateinit var adapter: Adapter

    override fun getPopupDimensions(): AutocompletePresenter.PopupDimensions {
        val dims = AutocompletePresenter.PopupDimensions()
        dims.width = 600
        dims.height = ViewGroup.LayoutParams.WRAP_CONTENT
        return dims
    }

    override fun instantiateAdapter(): RecyclerView.Adapter<*> {
        adapter = Adapter()
        return adapter
    }

    override fun onQuery(@Nullable query: CharSequence?) {
        var query = query
        val carList = ArrayList<User>()







        val all = User.USERS
        if (TextUtils.isEmpty(query)) {
            adapter.setData(all)
        } else {
            query = query!!.toString().toLowerCase()
            val list = ArrayList<User>()
            for (u in all) {
                if (u.carMake!!.toLowerCase().contains(query) || u.carModel!!.toLowerCase().contains(query)) {
                    list.add(u)
                }
            }
            adapter.setData(list)
            Log.e("UserPresenter", "found " + list.size + " users for query " + query)
        }
        adapter.notifyDataSetChanged()
    }

    inner class Adapter : RecyclerView.Adapter<Adapter.Holder>() {

        private var data: List<User>? = null

        private val isEmpty: Boolean
            get() = data == null || data!!.isEmpty()

        inner class Holder(val root: View) : RecyclerView.ViewHolder(root) {
            val fullname: TextView
            val username: TextView

            init {
                fullname = root.findViewById(R.id.fullname) as TextView
                username = root.findViewById(R.id.username) as TextView
            }
        }

        fun setData(data: List<User>) {
            this.data = data
        }

        override fun getItemCount(): Int {
            return if (isEmpty) 1 else data!!.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return Holder(LayoutInflater.from(context).inflate(R.layout.user, parent, false))
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            if (isEmpty) {
                holder.fullname.text = "No user here!"
                holder.username.text = "Sorry!"
                holder.root.setOnClickListener(null)
                return
            }
            val user = data!![position]
            holder.fullname.text = user.carMake
            holder.username.text = "@" + user.carModel!!
            holder.root.setOnClickListener { dispatchClick(user) }
        }
    }
}