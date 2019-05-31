package me.lamine.goride.tripActivity

import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import me.lamine.goride.R
import me.lamine.goride.dataObjects.TripRequest
import me.lamine.goride.dataObjects.User
import org.jetbrains.anko.backgroundColor
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.gms.maps.GoogleMap
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions






class RequestsAdapter(
    private var context: Context,
    private var tripsList: List<TripRequest>,
    private var userLiteList: List<User>
) : RecyclerView.Adapter<RequestsAdapter.TripViewHolder>() {
    //val items: MutableList<String> = arrayListOf()
    private var organizers = mutableListOf<String>()

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = tripsList[position]
        val userLite = userLiteList[position]
        val mOldFormat = "dd/MM/yyyy"
        val mNewFormat = "EEE, MMM dd"
        val mAgeFormat = "dd, MMM yyyy"
        // val peopleDriven = userLite.peopleDriven
        //   val ratings = userLite.userRating
        val userBirthday = userLite.birthday
        val df = SimpleDateFormat(mAgeFormat, Locale.US)
        val uBirthday = df.parse(userBirthday)
        val cal = Calendar.getInstance()
        cal.time = uBirthday
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)


        val age = getAge(year, month, day)
        var gender = userLite.gender.capitalize()
        gender = if (gender == "F") {
            "Female"
        } else {
            "Male"
        }
        val name = userLite.fullName
        holder.vName.text = name
        val genderAndAge = "$gender, $age"
        holder.uGenderAndAge.text = genderAndAge
        val roundedDouble = BigDecimal(userLite.userRating).setScale(1, RoundingMode.HALF_UP).toString()
        holder.uRatings.text = roundedDouble
        holder.uPeopleDriven.text = userLite.peopleDriven.toString()
        val nbReviews = "${userLite.userReviews.size} reviews"
        holder.numberOfReviews.text = nbReviews


        val newDate: String

        val sdf = SimpleDateFormat(mOldFormat, Locale.US)
        val d = sdf.parse(trip.date)
        sdf.applyPattern(mNewFormat)
        newDate = sdf.format(d)

        val fullDate = "$newDate at ${trip.time}"



        holder.date.text = fullDate
        if (userLite.profilePic == "") {
            userLite.profilePic =
                "https://firebasestorage.googleapis.com/v0/b/ridego-1555252117345.appspot.com/o/user_images%2Fmale_default.png?alt=media&token=658a01b5-ba18-4491-8aa7-e15c30284200"
        }
        Picasso.get().load(userLite.profilePic).into(holder.profilePic)
        holder.origin.text = trip.originSubCity
        holder.destination.text = trip.destSubCity
        val fod = "${trip.tripOrigin}, ${trip.originFullAddress}"
        holder.fullOriginAddress.text = fod
        val fdd = "${trip.tripDestination}, ${trip.destFullAddress}"
        holder.fullDestAddress.text = fdd


        holder.luggageSize.text = trip.luggageSize

        val nbSeats = "${trip.numberOfSeats} seats"
        holder.numberOfSeats.text = nbSeats

        requestViews(holder)

        when {
            DateUtils.isToday(d.time) -> {
                val org = "Today"
                setupOrg(org, holder)
                //tomorrow
            }
            DateUtils.isToday(d.time - DateUtils.DAY_IN_MILLIS) -> {
                val org = "Tomorrow"
                setupOrg(org, holder)
            }
            else -> {
                setupOrg(newDate, holder)
            }
        }

    }

    private fun setupOrg(org: String, holder: TripViewHolder) {
        if (!organizers.contains(org)) {
            holder.tripOrganizer.visibility = View.VISIBLE
            holder.tripOrganizer.text = org
            organizers.add(org)
        } else {
            holder.tripOrganizer.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return tripsList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        // val itemView = LayoutInflater.from(parent.context).inflate(R.layout.trip_card_layout, parent, false)
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.trip_card_layout, parent, false)

        return TripViewHolder(view).listen { pos, _ ->
            val item = tripsList[pos]
            val userItem = userLiteList[pos]

            startNextActivity(item, userItem)


        }
        // return TripViewHolder(itemView)
    }

    private fun getAge(year: Int, month: Int, day: Int): String {
        val dob = Calendar.getInstance()
        val today = Calendar.getInstance()

        dob.set(year, month, day)

        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        val ageInt = age

        return ageInt.toString()
    }

    private fun startNextActivity(clickedTrip: TripRequest, clickedUser: User) {
        val i = Intent(context, TripActivity::class.java)
        i.putExtra("ClickedTrip", clickedTrip)
        i.putExtra("ClickedTripUser", clickedUser)
        i.putExtra("isRequest", true)
        context.startActivity(i)
    }

    private fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.findViewById<CardView>(R.id.trip_card_view).setOnClickListener {
            val item = tripsList[adapterPosition]
            val userItem = userLiteList[adapterPosition]
            startNextActivity(item, userItem)
        }
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }

    private fun requestViews(holder: TripViewHolder) {
        holder.noSmoking.visibility = View.INVISIBLE
        holder.petsAllowed.visibility = View.INVISIBLE
        holder.pricePerSeat.visibility = View.GONE
        holder.bookingPref.visibility = View.INVISIBLE
        holder.origin.setTextColor(ContextCompat.getColor(this.context, R.color.colorPrimaryDark))
        holder.destination.setTextColor(ContextCompat.getColor(this.context, R.color.colorPrimaryDark))
        holder.uRatings.setTextColor(ContextCompat.getColor(this.context, R.color.colorPrimaryDark))
        holder.uPeopleDriven.setTextColor(ContextCompat.getColor(this.context, R.color.colorPrimaryDark))
        holder.numberOfReviews.setTextColor(ContextCompat.getColor(this.context, R.color.colorPrimaryDark))

        holder.cardP1.setTextColor(ContextCompat.getColor(this.context, R.color.colorPrimaryDark))
        holder.cardP2.setTextColor(ContextCompat.getColor(this.context, R.color.colorPrimaryDark))
        holder.div1.backgroundColor = ContextCompat.getColor(this.context, R.color.colorPrimaryDark)
        holder.div2.backgroundColor = ContextCompat.getColor(this.context, R.color.colorPrimaryDark)
        holder.ratingStar.setImageResource(R.drawable.ic_star_orange_24dp)
        holder.peopleDriven.setImageResource(R.drawable.ic_person_pin_circle_orange_24dp)

    }

    open class TripViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var origin: TextView = v.findViewById(R.id.trip_origin_card)
        var destination: TextView = v.findViewById(R.id.trip_destination_card)


        var date: TextView = v.findViewById(R.id.trip_date_card)
        //
       // private var vehicleInfo: TextView = v.findViewById(R.id.trip_vehicleperf_card)
        var luggageSize: TextView = v.findViewById(R.id.trip_luggage_card)
        //
        var noSmoking: ImageView = v.findViewById(R.id.trip_ic_smoke_card)
        var petsAllowed: ImageView = v.findViewById(R.id.trip_ic_pet_card)
        var numberOfSeats: TextView = v.findViewById(R.id.trip_seats_card)
        //
        var pricePerSeat: TextView = v.findViewById(R.id.trip_price_card)
        var bookingPref: ImageView = v.findViewById(R.id.trip_ic_instant_card)
        var vName: TextView = v.findViewById(R.id.trip_fullname_card)
        var uPeopleDriven: TextView = v.findViewById(R.id.trip_driven_card4)
        var uRatings: TextView = v.findViewById(R.id.trip_ratings_card2)
        var uGenderAndAge: TextView = v.findViewById(R.id.trip_gender_age_card)
        var fullOriginAddress: TextView = v.findViewById(R.id.trip_desc1_card)
        var fullDestAddress: TextView = v.findViewById(R.id.trip_desc2_card)
        var profilePic: ImageView = v.findViewById(R.id.trip_profile_image)
        var tripOrganizer: TextView = v.findViewById(R.id.trip_orginazer)
        var numberOfReviews: TextView = v.findViewById(R.id.trip_reviews_card3)
        var div1: View = v.findViewById(R.id.divider_card)
        var div2: View = v.findViewById(R.id.divider2)
        var cardP1: TextView = v.findViewById(R.id.card_point)
        var cardP2: TextView = v.findViewById(R.id.card_point2)
        var ratingStar: ImageView = v.findViewById(R.id.rating_star_card)
        var peopleDriven: ImageView = v.findViewById(R.id.people_driven_card)
        //


    }


}

