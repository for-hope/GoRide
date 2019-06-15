package me.lamine.goride.tripActivity

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.widget.ImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.content.Intent
import android.text.format.DateUtils
import android.util.Log
import androidx.cardview.widget.CardView
import com.squareup.picasso.Picasso
import me.lamine.goride.R
import me.lamine.goride.dataObjects.Trip
import me.lamine.goride.dataObjects.User
import java.math.BigDecimal
import java.math.RoundingMode


class TripAdapter(private var context: Context, private var tripsList: List<Trip>, private var userLiteList: List<User>): RecyclerView.Adapter<TripAdapter.TripViewHolder>() {
    //val items: MutableList<String> = arrayListOf()
    private var organizers = mutableListOf<String>()

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {

        Log.i("NumberOfTrips", tripsList.size.toString() + "TRIPS")
        val trip = tripsList[position]
        Log.i("TRIP-ID", trip.tripID + " TRIPS")
        val userLite = userLiteList[position]
        //val name = "Lamine Fet"
        val mOldFormat = "dd/MM/yyyy"
        val mNewFormat = "EEE, MMM dd"
        val mAgeFormat = "dd, MMM yyyy"

        //val peopleDriven = userLite.peopleDriven
        //val ratings = userLite.userRating
        val userBirthday = userLite.birthday
        val df = SimpleDateFormat(mAgeFormat, Locale.US)
        val uBirthdate = df.parse(userBirthday)
        val cal = Calendar.getInstance()
        cal.time = uBirthdate
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH)
        val day = cal.get(Calendar.DAY_OF_MONTH)


        val age = getAge(year, month, day)
        var gender = userLite.gender.capitalize()
        gender = if (gender=="F"){
            "Female"
        } else {
            "Male"
        }
        val name = userLite.fullName
        holder.vName.text = name
        val genderAndAge = "$gender, $age"
        holder.uGenderAndAge.text = genderAndAge
        val roundedDouble =  BigDecimal(userLite.userRating).setScale(1, RoundingMode.HALF_UP).toString()
        holder.uRatings.text =roundedDouble
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
        if (userLite.profilePic == ""){
            userLite.profilePic = "https://firebasestorage.googleapis.com/v0/b/ridego-1555252117345.appspot.com/o/user_images%2Fmale_default.png?alt=media&token=658a01b5-ba18-4491-8aa7-e15c30284200"
        }
        Picasso.get().load(userLite.profilePic).into(holder.profilePic)
        holder.origin.text = trip.originSubCity
        holder.destination.text = trip.destSubCity
        val fod = "${trip.origin}, ${trip.originFullAddress}"
        holder.fullOriginAddress.text = fod
        val fdd = "${trip.destination}, ${trip.destFullAddress}"
        holder.fullDestAddress.text = fdd
        var vehiclePref = "${trip.vehicleModel} ${trip.vehicleColor} '${trip.vehicleYear}"

        if (trip.hasVehicleInfo){
            if (trip.vehicleYear == 0){
                vehiclePref = "${trip.vehicleModel} ${trip.vehicleColor}"
            }
            holder.vehicleInfo.text = vehiclePref

        }
        holder.luggageSize.text = trip.luggageSize
        if (trip.noSmoking){
            holder.noSmoking.visibility = View.VISIBLE
        } else {
            holder.noSmoking.visibility = View.GONE
        }
        if (trip.petsAllowed) {
            holder.petsAllowed.visibility = View.VISIBLE
        } else {
            holder.petsAllowed.visibility = View.GONE
        }

        val nbSeats = "${trip.numberOfSeats - trip.bookedUsers.size} seats left"
        holder.numberOfSeats.text = nbSeats
        val seatPrice = "${trip.pricePerSeat} DZD"
        holder.pricePerSeat.text = seatPrice
        if (trip.bookingPref == 0){
            holder.bookingPref.visibility = View.GONE
        } else {
            holder.bookingPref.visibility = View.VISIBLE
        }
        when {
            DateUtils.isToday(d.time) -> {
                val org = "Today"
                setupOrg(org,holder)
                //tomorrow
            }
            DateUtils.isToday(d.time - DateUtils.DAY_IN_MILLIS) -> {
                val org = "Tomorrow"
                setupOrg(org,holder)
            }
            else -> {
                setupOrg(newDate,holder)
            }
        }

    }
    private fun setupOrg(org:String,holder: TripViewHolder){
        if (!organizers.contains(org)){
            holder.tripOrganizer.visibility = View.VISIBLE
            holder.tripOrganizer.text = org
            organizers.add(org)
        } else {
            holder.tripOrganizer.visibility = View.GONE
        }
    }
    override fun getItemCount(): Int {
       return  tripsList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
       // val itemView = LayoutInflater.from(parent.context).inflate(R.layout.trip_card_layout, parent, false)
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.trip_card_layout, parent, false)

        return TripViewHolder(view).listen { pos, _ ->
            val item = tripsList[pos]
            val userItem = userLiteList[pos]

            startNextActivity(item,userItem)



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
    private fun startNextActivity(clickedTip: Trip, clickedUser: User){
        val i = Intent(context, TripActivity::class.java)
        i.putExtra("ClickedTrip", clickedTip )
        i.putExtra("ClickedTripUser",clickedUser)
        i.putExtra("isRequest",false)
        context.startActivity(i)
    }

    private fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.findViewById<CardView>(R.id.trip_card_view).setOnClickListener {
            val item = tripsList[adapterPosition]
            val userItem = userLiteList[adapterPosition]
            startNextActivity(item,userItem) }
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }


    open class TripViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var origin: TextView = v.findViewById(R.id.trip_origin_card)
        var destination: TextView = v.findViewById(R.id.trip_destination_card)
        var stops:ArrayList<String> = ArrayList()
        var numberOfStops = 0
        var date:TextView = v.findViewById(R.id.trip_date_card)
        var vehicleInfo:TextView = v.findViewById(R.id.trip_vehicleperf_card)
        var luggageSize:TextView = v.findViewById(R.id.trip_luggage_card)
        var noSmoking:ImageView = v.findViewById(R.id.trip_ic_smoke_card)
        var petsAllowed:ImageView = v.findViewById(R.id.trip_ic_pet_card)
        var numberOfSeats:TextView = v.findViewById(R.id.trip_seats_card)
        var pricePerSeat:TextView = v.findViewById(R.id.trip_price_card)
        var bookingPref:ImageView = v.findViewById(R.id.trip_ic_instant_card)
        var vName: TextView = v.findViewById(R.id.trip_fullname_card)
        var uPeopleDriven:TextView = v.findViewById(R.id.trip_driven_card4)
        var uRatings:TextView = v.findViewById(R.id.trip_ratings_card2)
        var uGenderAndAge:TextView = v.findViewById(R.id.trip_gender_age_card)
        var fullOriginAddress: TextView = v.findViewById(R.id.trip_desc1_card)
        var fullDestAddress: TextView = v.findViewById(R.id.trip_desc2_card)
        var profilePic:ImageView = v.findViewById(R.id.trip_profile_image)
        var tripOrganizer:TextView = v.findViewById(R.id.trip_orginazer)
        var numberOfReviews:TextView = v.findViewById(R.id.trip_reviews_card3)
        //  var vDestination: TextView
         //var vDate: TextView
        // var vOrigin: TextView

    }
}

