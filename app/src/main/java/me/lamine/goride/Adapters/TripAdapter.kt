package me.lamine.goride.Adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.content.Intent
import me.lamine.goride.LiteUser
import me.lamine.goride.R
import me.lamine.goride.Trip
import me.lamine.goride.TripActivity


class TripAdapter(private var context: Context, private var tripsList: List<Trip>, private var userLiteList: List<LiteUser>): RecyclerView.Adapter<TripAdapter.TripViewHolder>() {
    //val items: MutableList<String> = arrayListOf()
    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = tripsList[position]
        val userLite = userLiteList[position]
        //val name = "Lamine Fet"
        val OLD_FORMAT = "dd/MM/yyyy"
        val NEW_FORMAT = "EEE, MMM dd"
        val age_format = "dd, MMM yyyy"
        val peopleDriven = userLite.peopleDriven
        val ratings = userLite.ratings
        val userBirthday = userLite.birthday
        val df = SimpleDateFormat(age_format, Locale.US)
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
        val name = userLite.name
        holder.vName.text = name
        val genderAndAge = "$gender, $age"
        holder.uGenderAndAge.text = genderAndAge
        holder.uRatings.text = userLite.ratings.toString()
        holder.uPeopleDriven.text = userLite.peopleDriven.toString()


        val newDate: String

        val sdf = SimpleDateFormat(OLD_FORMAT, Locale.US)
        val d = sdf.parse(trip.date)
        sdf.applyPattern(NEW_FORMAT)
        newDate = sdf.format(d)

        val fullDate = "$newDate at ${trip.time}"

        holder.date.text = fullDate

        holder.origin.text = trip.originSubCity
        holder.destination.text = trip.destSubCity
        val fod = "${trip.origin}, ${trip.originFullAddress}"
        holder.fullOriginAddress.text = fod
        val fdd = "${trip.destination}, ${trip.destFullAddress}"
        holder.fullDestAddress.text = fdd
        val vehiclePref = "${trip.vehicleModel} ${trip.vehicleColor} '${trip.vehicleYear}"
        if (trip.hasVehicleInfo){
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
        val nbSeats = "${trip.numberOfSeats} seats left"
        holder.numberOfSeats.text = nbSeats
        val seatPrice = "${trip.pricePerSeat} DZD"
        holder.pricePerSeat.text = seatPrice
        if (trip.bookingPref == 0){
            holder.bookingPref.visibility = View.GONE
        } else {
            holder.bookingPref.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int {
       return  tripsList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
       // val itemView = LayoutInflater.from(parent.context).inflate(R.layout.trip_card_layout, parent, false)
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.trip_card_layout, parent, false)
        return TripViewHolder(view).listen { pos, type ->
            val item = tripsList[pos]
            val userItem = userLiteList[pos]
            Toast.makeText(view.context,"item is ${item.destination}", Toast.LENGTH_SHORT).show()
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
    private fun startNextActivity(clickedTip: Trip, clickedUser: LiteUser){
        val i = Intent(context, TripActivity::class.java)
        i.putExtra("ClickedTrip", clickedTip )
        i.putExtra("ClickedTripUser",clickedUser)
        context.startActivity(i)
    }
    private fun <T : RecyclerView.ViewHolder> T.listen(event: (position: Int, type: Int) -> Unit): T {
        itemView.setOnClickListener {
            event.invoke(adapterPosition, itemViewType)
        }
        return this
    }


    open class TripViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var origin: TextView
        var destination: TextView
        var stops:ArrayList<String> = ArrayList()
        var numberOfStops = 0
        var date:TextView
        var vehicleInfo:TextView
        var luggageSize:TextView
        var noSmoking:ImageView
        var petsAllowed:ImageView
        var numberOfSeats:TextView
        var pricePerSeat:TextView
        var bookingPref:ImageView
        var vName: TextView
        var uPeopleDriven:TextView
        var uRatings:TextView
        var uGenderAndAge: TextView
        var fullOriginAddress: TextView
        var fullDestAddress: TextView

       //  var vDestination: TextView
         //var vDate: TextView
        // var vOrigin: TextView

        init {

            uGenderAndAge = v.findViewById(R.id.trip_gender_age_card)
            uRatings = v.findViewById(R.id.trip_ratings_card2)
            uPeopleDriven = v.findViewById(R.id.trip_driven_card4)
            vName = v.findViewById(R.id.trip_fullname_card)
            date = v.findViewById(R.id.trip_date_card)
            origin = v.findViewById(R.id.trip_origin_card)
            destination = v.findViewById(R.id.trip_destination_card)
            vehicleInfo = v.findViewById(R.id.trip_vehicleperf_card)
            luggageSize = v.findViewById(R.id.trip_luggage_card)
            noSmoking = v.findViewById(R.id.trip_ic_smoke_card)
            petsAllowed = v.findViewById(R.id.trip_ic_pet_card)
            numberOfSeats = v.findViewById(R.id.trip_seats_card)
            pricePerSeat = v.findViewById(R.id.trip_price_card)
            bookingPref = v.findViewById(R.id.trip_ic_instant_card)
            fullOriginAddress = v.findViewById(R.id.trip_desc1_card)
            fullDestAddress = v.findViewById(R.id.trip_desc2_card)
        }
    }
}

