package me.lamine.goride.userActivity

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import me.lamine.goride.R
import me.lamine.goride.R.string.date
import me.lamine.goride.dataObjects.Review
import java.text.SimpleDateFormat
import java.util.*


class ReviewsAdapter(private var reviewsList: List<Review>): RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder>() {
    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.reviewDesc.text = reviewsList[position].reviewDesc
        Log.i("review_Dec",reviewsList[position].reviewDesc )
        holder.username.text = reviewsList[position].reviewUser?.fullName!!
        val date = Date(reviewsList[position].reviewDate.toString().toLong())
        val sdf = SimpleDateFormat("dd MMMM yyy", Locale.US)
        val mDate = sdf.format(date)
        holder.reviewOtdAndDate.text = mDate
        Picasso.get().load(reviewsList[position].reviewUser?.profilePic!!).into(holder.reviewerPfp)
        if (reviewsList[position].reviewUser?.isDriver!!){
            val type = "Driver Review"
            holder.reviewType.text = type
        } else {
            val type = "Passenger Review"
            holder.reviewType.text = type
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.review_layout, parent, false)
        return ReviewViewHolder(view).listen { _, _ ->
           // val item = reviewsList[pos]
        }
    }
    override fun getItemCount(): Int {
       return reviewsList.size
    }
    private fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }
    open class ReviewViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            var username:TextView = v.findViewById(R.id.review_username)
            var reviewType:TextView = v.findViewById(R.id.review_type)
            var reviewerPfp:ImageView = v.findViewById(R.id.review_user_pfp)
            var reviewDesc:TextView = v.findViewById(R.id.review_desc)
            var reviewOtdAndDate:TextView = v.findViewById(R.id.review_otdAndDate)


    }
}