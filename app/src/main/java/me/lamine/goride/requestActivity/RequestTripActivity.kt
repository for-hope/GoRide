package me.lamine.goride.requestActivity

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.tooltip.Tooltip
import kotlinx.android.synthetic.main.activity_request_trip.*
import kotlinx.android.synthetic.main.activity_request_trip.add_datebtn
import kotlinx.android.synthetic.main.activity_request_trip.add_timebtn
import kotlinx.android.synthetic.main.activity_request_trip.destination_label
import kotlinx.android.synthetic.main.activity_request_trip.editText_time
import kotlinx.android.synthetic.main.activity_request_trip.origin_label
import kotlinx.android.synthetic.main.activity_request_trip.pref_btn_l
import kotlinx.android.synthetic.main.activity_request_trip.pref_btn_m
import kotlinx.android.synthetic.main.activity_request_trip.pref_btn_no
import kotlinx.android.synthetic.main.activity_request_trip.pref_btn_s
import kotlinx.android.synthetic.main.activity_request_trip.search_date_edittext
import kotlinx.android.synthetic.main.activity_request_trip.seat1
import kotlinx.android.synthetic.main.activity_request_trip.seat2
import kotlinx.android.synthetic.main.activity_request_trip.seat3
import kotlinx.android.synthetic.main.activity_request_trip.seat4
import kotlinx.android.synthetic.main.activity_request_trip.seat5
import kotlinx.android.synthetic.main.activity_request_trip.seat6
import kotlinx.android.synthetic.main.activity_request_trip.seat7
import me.lamine.goride.R
import me.lamine.goride.dataObjects.Trip
import me.lamine.goride.dataObjects.TripRequest
import me.lamine.goride.utils.decodeWilaya
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class RequestTripActivity:AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null
    private lateinit var database: DatabaseReference
    private var luggageOption: Int = -1
    private var seatOption = -1
    private lateinit var originText:String
    private lateinit var destinationText:String
    private var originLatLng: LatLng? = null
    private var desLatLng: LatLng? = null
    private lateinit var mGeocoder: Geocoder
    private var originCode = 0
    private var destinationCode = 0
    private val mapsApiKey: String = "AIzaSyDWbc3KQP6ssBlClf8HSiZWEtMxfwqSYto"
    private var originCity:String = ""
    private var originSubCity:String = ""
    private lateinit var originFullAddress:String
    private var destCity:String = ""
    private var destSubCity:String = ""
    private var destFullAddress:String = ""
    private var isModifyMode = false
    private var AUTOCOMPLETE_REQUEST_CODE = 10
    private var AUTOCOMPLETE_REQUEST_CODE_DES = 20
    private lateinit var tripDate:String
    private lateinit var tripTime:String
    private lateinit var tripDescription:String
    private lateinit var tripToModify: Trip
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_trip)
        Places.initialize(applicationContext, mapsApiKey)
        mGeocoder = Geocoder(this, Locale.getDefault())
        // Create a new Places client instance.
        val placesClient = Places.createClient(this)
        origin_label.setOnClickListener {
            showToolTip(edittext_origin_req,"Where are you taking off from?")
            autoCompleteFieldOrigin()
        }
        destination_label.setOnClickListener {

            showToolTip(edittext_destination_req,"Where are you going to?")
            autoCompleteFieldDes()

        }
        database = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        if (mAuth?.currentUser == null) {
            Toast.makeText(this,"LOGIN FIRST",Toast.LENGTH_LONG).show()
            finish()
        }else {
            currentUser = mAuth?.currentUser
        }

        isModifyMode = intent.getBooleanExtra("modifyTrip",false)
        if (isModifyMode){
            tripToModify = intent.getSerializableExtra("tripToModify") as Trip
        }
        if (isModifyMode){
          //  modifyTrip(tripToModify)
        }
        fillLuggageOptions()
        fillSeatOptions()
        dateField()
        timeField()
        trip_ac_submit_btn.setOnClickListener {  fillAllForm()  }

    }
    private fun showToolTip(editText: EditText, text:String) {
        val tooltip = Tooltip.Builder(editText)
            .setText(text)
            .setCornerRadius(15f)
            .setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
            .setTextColor(ContextCompat.getColor(this, R.color.whiteColor))
            .setCancelable(true)
            .show()
    }
    private fun autoCompleteFieldOrigin(){
        val fields = Arrays.asList(
            Place.Field.ID, Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.ADDRESS_COMPONENTS, Place.Field.LAT_LNG)
        // Start the autocomplete intent.
        val intent = com.google.android.libraries.places.widget.Autocomplete.IntentBuilder(
            AutocompleteActivityMode.FULLSCREEN, fields
        ).setCountry("DZ")
            .build(this)

        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }
    private fun autoCompleteFieldDes(){
        val fields = Arrays.asList(
            Place.Field.ID, Place.Field.NAME,
            Place.Field.ADDRESS_COMPONENTS,
            Place.Field.ADDRESS, Place.Field.LAT_LNG)
        // Start the autocomplete intent.
        val intent = com.google.android.libraries.places.widget.Autocomplete.IntentBuilder(
            AutocompleteActivityMode.FULLSCREEN, fields
        ).setCountry("DZ")
            .build(this)

        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_DES)
    }
    @Throws(IOException::class)
    private fun getSubCityNameByName(name:String): String? {
        val addresses = mGeocoder.getFromLocationName(name,1)
        return if (addresses != null && addresses.size > 0) {
            if (addresses[0].locality != null) {
                addresses[0].locality }
            else{
                "EMPTY"
            }
        } else null
    }
    @Throws(IOException::class)
    private fun getCityNameByName(name:String): String? {
        val addresses = mGeocoder.getFromLocationName(name,1)
        return if (addresses != null && addresses.size > 0) {
            if (addresses[0].adminArea != null) {
                addresses[0].adminArea }
            else{
                "EMPTY"
            }

        } else null
    }
    @Throws(IOException::class)
    private fun getCityNameByCoordinates(lat: Double, lon: Double,isOrigin:Boolean): String? {


        val addresses = mGeocoder.getFromLocation(lat, lon, 3)




        return if (addresses != null && addresses.size > 0) {
            // Here are some results you can geocode
            val city: String
            val state: String
            if (addresses[0].locality != null) {
                city = addresses[0].locality
                if (isOrigin){
                    originSubCity = city
                } else {
                    destSubCity = city
                }

                Log.d("city", city)
            }
            if (addresses[0].adminArea != null) {
                state = addresses[0].adminArea
                if(isOrigin){
                    originCity = state
                } else {
                    destCity = state
                }

                Log.d("state", state)
            }
            addresses[0].countryName
        } else null
    }
    private fun setOrigin(origin:String){
        edittext_origin_req.setText(origin)
    }
    private fun setDestination(destination:String){
        edittext_destination_req.setText(destination)
    }
    private fun fillOrigin(place: Place){
        originCity = ""
        originSubCity = ""
        originLatLng = place.latLng
        if (place.addressComponents?.asList()!!.size == 1){
            Toast.makeText(this,"Pick a valid place.", Toast.LENGTH_SHORT).show()
        } else {
            val placeName = place.addressComponents?.asList()!![1].name
            //take cords from geolocation
            getCityNameByCoordinates(originLatLng?.latitude!!,originLatLng?.longitude!!,true)
            //check if they're init
            if (originCity == ""){
                originCity = getCityNameByName(placeName)!!
            }
            if (originSubCity == ""){
                originSubCity = getSubCityNameByName(placeName)!!
                if (originSubCity=="EMPTY"){
                    originSubCity = originCity
                }
            }
            originFullAddress = place.address!!
            Log.i("state", originCity)
            Log.i("city", originSubCity)
            Log.i("address", originFullAddress)

            if (place.address != null){
                Log.i("PLACE ADDRESS", place.address)
            } else {
                Log.i("PLACE ADDRESS", "NU::")
            }
            val decodedOriginCity = decodeWilaya(originCity)
            originCode = decodedOriginCity
            Log.i("DECODE",decodedOriginCity.toString())
            setOrigin(place.name!!)
            Log.i("SetOrigin",originSubCity)
            // setOrigin("$place.name!!")
        }


    }
    private fun fillDest(place: Place){
        destCity = ""
        destSubCity = ""
        Log.i("PActivity", place.name!!)
        desLatLng = place.latLng
        if (place.address != null) {
            Log.i("PostActivity", place.address)
        } else {
            Log.i("PostActivity", "NUUULL")
        }
        Log.i("PostActivity", "${desLatLng?.latitude!!},${desLatLng?.longitude!!}")
        val placeName = place.addressComponents?.asList()!![1].name
        //take cords from geolocation
        getCityNameByCoordinates(desLatLng?.latitude!!,desLatLng?.longitude!!,false)
        //check if they're init
        if (destCity == ""){
            destCity = getCityNameByName(placeName)!!
        }
        if (destSubCity == ""){
            destSubCity = getSubCityNameByName(placeName)!!
            if (destSubCity=="EMPTY"){
                destSubCity = destCity
            }
        }
        destFullAddress = place.address!!
        Log.i("state", destCity)
        Log.i("city", destSubCity)
        Log.i("address", destFullAddress)


        if (place.address != null){
            Log.i("PLACE ADDRESS", place.address)
        } else {
            Log.i("PLACE ADDRESS", "NUll")
        }
        val decodedDestCity = decodeWilaya(destCity)
        destinationCode = decodedDestCity
        setDestination(place.name!!)


    }
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    fillOrigin(place)
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                    val status = Autocomplete.getStatusFromIntent(data!!)
                    Log.i("RequestActivity", status.statusMessage)
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
        }
        //
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE_DES) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    fillDest(place)


                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                    val status = Autocomplete.getStatusFromIntent(data!!)
                    Log.i("SearchActivity", status.statusMessage)
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
        }


        ////
        if (resultCode == Activity.RESULT_CANCELED) {
            return
        }
    }


    //
    private fun isValidDate(dateString: String):Boolean {
        var date: Date? = null
        val myFormat = "dd/MM/yyyy"
        val c = Calendar.getInstance()


        try {
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            date = sdf.parse(dateString)
            if (dateString != sdf.format(date)) {
                date = null
            }
        } catch (ex: ParseException) {
            ex.printStackTrace()
        }
        return if (date != null) {
            Log.i("PostingActivity",c.time.toString())
            date.after(c.time)

        } else {
            false
        }

    }
    private fun isValidTime(timeString: String):Boolean {
        var date: Date? = null
        val myFormat = "HH:mm"
        try {
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            date = sdf.parse(timeString)
            if (!timeString.equals(sdf.format(date))) {
                date = null
            }
        } catch (ex: ParseException) {
            ex.printStackTrace()
        }
        return date != null
    }
    //
    private fun dateField() {
        val myCalendar = Calendar.getInstance()
        val date = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel(myCalendar)
        }
        add_datebtn.setOnClickListener {
            DatePickerDialog(this,date,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show() }
    }
    private fun timeField() {

        val myCalendar = Calendar.getInstance()
        val time = TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            myCalendar.set(Calendar.HOUR_OF_DAY,hourOfDay)
            myCalendar.set(Calendar.MINUTE,minute)
            updateTimeLabel(myCalendar)
        }

        add_timebtn.setOnClickListener {
            //   TimePickerDialog(this,myCalendar.get(Calendar.HOUR_OF_DAY),myCalendar.get(Calendar.MINUTE))
            TimePickerDialog(this,time,myCalendar.get(Calendar.HOUR_OF_DAY),myCalendar.get(Calendar.MINUTE),true).show()
            // DatePickerDialog(this,date,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),
            //      myCalendar.get(Calendar.DAY_OF_MONTH)).show()

        }

    }
    //
    private fun updateTimeLabel(myCalendar: Calendar){
        val myFormat = "HH:mm"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        editText_time.setText(sdf.format(myCalendar.time))
    }
    private fun updateLabel(myCalendar:Calendar) {
        val myFormat = "dd/MM/yyyy"
        val sdf  = SimpleDateFormat(myFormat,Locale.US)
        search_date_edittext.setText(sdf.format(myCalendar.time))
    }
    //
    private  fun fillLuggageOptions(): Int {
        val luggageBtns = arrayListOf<LinearLayout>(pref_btn_no,pref_btn_s,pref_btn_m,pref_btn_l)
        for ((index, btn) in luggageBtns.withIndex()){
            btn.setOnClickListener {
                luggageOption=index
                //change to global variable
                Toast.makeText(this, "index is $luggageOption", Toast.LENGTH_SHORT).show()
                btn.setBackgroundColor(ContextCompat.getColor(this, R.color.btnBlack))
                val btnText = btn.getChildAt(1) as TextView
                val btnImg = btn.getChildAt(0) as ImageView
                btnText.setTextColor(ContextCompat.getColor(this, R.color.backgroundColor))
                btnImg.setImageResource(R.drawable.ic_work_white_24dp)
                for (otherBtn in luggageBtns ){
                    if (otherBtn != btn){
                        otherBtn.setBackgroundColor(ContextCompat.getColor(this,
                            R.color.backgroundColor
                        ))
                        val otherBtnText = otherBtn.getChildAt(1) as TextView
                        val otherBtnImg = otherBtn.getChildAt(0) as ImageView
                        otherBtnText.setTextColor(ContextCompat.getColor(this, R.color.btnBlack))
                        otherBtnImg.setImageResource(R.drawable.ic_work_black_24dp)
                        otherBtn.background = getDrawable(R.drawable.custom_border)
                    }
                }
            }

        }
        return luggageOption
    }
    private  fun fillSeatOptions(): Int {
        val seatBtns = arrayListOf<TextView>(seat1,seat2,seat3,seat4,seat5,seat6,seat7)
        for ((indexSeat,seatBtn) in seatBtns.withIndex()){
            seatBtn.setOnClickListener {
                seatOption = indexSeat+1
                seatBtn.setTextColor(ContextCompat.getColor(this, R.color.backgroundColor))
                val imgBackground = seatBtn.background as GradientDrawable
                imgBackground.setColor(ContextCompat.getColor(this, R.color.btnBlack))

                for (sBtn in seatBtns ){
                    if (sBtn != seatBtn){
                        //sBtn.setBackgroundColor(ContextCompat.getColor(this,R.color.backgroundColor))
                        val sImgBackground = sBtn.background as GradientDrawable
                        sImgBackground.setColor(ContextCompat.getColor(this, R.color.backgroundColor))
                        sBtn.setTextColor(ContextCompat.getColor(this, R.color.btnBlack))
                    }
                }
            }

        }
        return seatOption
    }
    //
    private fun fillAllForm(){
        var correctForm = true
        Log.i("fillAllForms",correctForm.toString())
        if (edittext_origin_req.text.toString() == "" || edittext_origin_req.text == null){
            correctForm = false
            origin_label.setError("Invalid",true)
        } else {
            originText = edittext_origin_req.text.toString()
            origin_label.removeError()
        }
        if (edittext_destination_req.text.toString() == "" || edittext_destination_req.text == null){
            correctForm = false
            destination_label.setError("Invalid",true)
        } else {
            destinationText = edittext_destination_req.text.toString()
            destination_label.removeError()
        }




        tripDescription = edittext_tripdesc.text.toString()
        tripDate=search_date_edittext.text.toString()
        val isValidDate = isValidDate(tripDate)
        if (!isValidDate){
            correctForm = false
            add_datebtn.setError("Please enter valid date at least after 24h",false)
        } else {
            add_datebtn.removeError()
        }
        // val validatorDate = editText_date.validator()
        //var isValidDate = validatorDate.addRule()
        tripTime=editText_time.text.toString()
        val isValidTime = isValidTime(tripTime)
        if (!isValidTime){
            correctForm = false
            add_timebtn.setError("Please enter a valid time",false)
        } else {
            add_timebtn.removeError()
        }
        if(correctForm){
                finishUploading()
            //start activity
        } else {
            Toast.makeText(this,"Wrong Information, check and try again.",Toast.LENGTH_SHORT).show()
        }
    }
    private fun finishUploading(){
        val luggageOption = fillLuggageOptions()
        val seatOption = fillSeatOptions()

        val trip = TripRequest(
            originText,
            destinationText,
            tripDate,
            luggageOption,
            tripTime,
            seatOption
        )
        trip.addDescription(tripDescription)
        trip.originFullAddress = originFullAddress
        trip.destFullAddress = destFullAddress
        trip.destSubCity = destSubCity
        trip.originSubCity = originSubCity
        trip.originCity = originCity
        trip.destCity = destCity
        //trip.tripID = UUID.randomUUID().toString()
        trip.userID = currentUser?.uid!!
        trip.addTripDestinations(originCity,originSubCity,originFullAddress,destCity,destSubCity,destFullAddress)

        saveToDB(trip)



    }
    private fun saveToDB(trip: TripRequest) {
        setPb(1)
        if (!isModifyMode){
            val otdPath = "${String.format("%02d", originCode)}_${String.format("%02d", destinationCode)}"
            val childName = "${originCode}_$destinationCode"
            val newRef = database.child("tripRequests").child(otdPath).push()
            val tripID = newRef.key
            trip.tripID = tripID!!
            newRef.setValue(trip) { databaseError, _ ->
                if (databaseError != null) {
                    Log.i("FireBaseEroor", databaseError.message)
                    Toast.makeText(this, "Error $databaseError", Toast.LENGTH_LONG).show()
                }
            }
            newRef.child("userID").setValue(currentUser?.uid)
        } else {
            val otdPath = "${String.format("%02d", originCode)}_${String.format("%02d", destinationCode)}"
            val childName = "${originCode}_$destinationCode"
            val newRef = database.child("tripRequests").child(otdPath).child(tripToModify.tripID)
            trip.tripID = tripToModify.tripID
            newRef.setValue(trip) { databaseError, _ ->
                if (databaseError != null) {
                    Log.i("FireBaseEroor", databaseError.message)
                    Toast.makeText(this, "Error $databaseError", Toast.LENGTH_LONG).show()
                }
            }
            newRef.child("userID").setValue(currentUser?.uid)
        }
        if (currentUser != null) {
            val childName = "${originCode}_$destinationCode"
            val otdPath = "${String.format("%02d", originCode)}_${String.format("%02d", destinationCode)}"
            database.child("users").child(currentUser?.uid!!).child("tripsRequested").child(trip.tripID).setValue(otdPath)
            database.child("users").child(currentUser?.uid!!).child("activeTripRequests").child(trip.tripID).setValue(otdPath)
        }
        database.push()
        setPb(0)
        showDoneDialog()


    }
    private fun setPb(visibility: Int){
        if (visibility == 1) {
            progressBar1.visibility = View.VISIBLE
            greout_layout.visibility = View.VISIBLE
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            progressBar1.visibility = View.GONE
            greout_layout.visibility = View.GONE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }
    private fun showDoneDialog(){
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Trip Saved!")
            .setMessage("You can view and edit your trip in home screen!")

            // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton("Done") { dialog, which ->

                Toast.makeText(this,"Trip Posted!", Toast.LENGTH_SHORT).show()
                finish()
            }

            // A null listener allows the button to dismiss the dialog and take no further action.
            //.setNegativeButton("Done!", null)
            .setIcon(R.drawable.ic_done_green_24dp)
            .show()
    }
}