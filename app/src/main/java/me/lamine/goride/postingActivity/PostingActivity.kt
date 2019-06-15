package me.lamine.goride.postingActivity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.location.Geocoder
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
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
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.jaredrummler.materialspinner.MaterialSpinner
import com.tooltip.Tooltip
import com.wajahatkarim3.easyvalidation.core.rules.*
import com.wajahatkarim3.easyvalidation.core.view_ktx.validator
import kotlinx.android.synthetic.main.activity_posting.*
import kotlinx.android.synthetic.main.view_instant_book.*
import kotlinx.android.synthetic.main.view_luggage.*
import kotlinx.android.synthetic.main.view_preferences.*
import kotlinx.android.synthetic.main.view_price_seat.*
import kotlinx.android.synthetic.main.view_request_book.*
import kotlinx.android.synthetic.main.view_ride_schedule.*
import kotlinx.android.synthetic.main.view_seats.*
import kotlinx.android.synthetic.main.view_vehicle_form.*
import me.lamine.goride.R
import me.lamine.goride.dataObjects.Trip
import me.lamine.goride.dataObjects.TripRequest
import me.lamine.goride.utils.Database
import me.lamine.goride.utils.decodeWilaya
import studio.carbonylgroup.textfieldboxes.ExtendedEditText
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class PostingActivity : AppCompatActivity() {

    private lateinit var mGeocoder: Geocoder
    private var originCode = 0
    private var destinationCode = 0
    private var isImageSet = false
    private val mapsApiKey: String = "AIzaSyDWbc3KQP6ssBlClf8HSiZWEtMxfwqSYto"
    private val mGALLERY = 1
    private val mCAMERA = 2
    private var originCity: String = ""
    private var originSubCity: String = ""
    private lateinit var originFullAddress: String
    private var destCity: String = ""
    private var destSubCity: String = ""
    private var destFullAddress: String = ""
    private val mAutoCompleteRequestCode = 10
    private val mAutoCompleteRequestCodeDest = 20
    private var downloadUrl: String = ""
    private lateinit var originText: String
    private lateinit var destinationText: String
    private var stops: ArrayList<String> = ArrayList()
    private var vehicleModel: String = ""
    private var vehicleType: String = ""
    private var vehicleColor: String = ""
    private var vehicleYear: Int = 0
    private var licensePlate: String = ""
    private var tripPrice: Int = 0
    private lateinit var tripDescription: String
    private lateinit var tripDate: String
    private lateinit var tripTime: String
    private var numberOfStops = 0
    private var luggageOption: Int = -1
    private val otherOptions = arrayListOf(0, 0)
    private var bookingOption = -1
    private var seatOption = -1
    private var vehicleSkipped = false
    private var stopsList: ArrayList<View> = ArrayList()
    private lateinit var mStorageRef: StorageReference
    private lateinit var mDatabase: Database
    private var originLatLng: LatLng? = null
    private var desLatLng: LatLng? = null
    private lateinit var tripToModify: Trip
    private var isModifyMode = false
     private var isAcceptedTrip = false
    private lateinit var tripRequest:TripRequest
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_posting)
        mStorageRef = FirebaseStorage.getInstance().reference
        Places.initialize(applicationContext, mapsApiKey)
        mGeocoder = Geocoder(this, Locale.getDefault())
        mDatabase = Database()
        // Create a new Places client instance.
        Places.createClient(this)

        origin_label.setOnClickListener {
            showToolTip(edittext_origin, "Where are you taking off from?")
            autoCompleteFieldOrigin()
        }
        destination_label.setOnClickListener {

            showToolTip(edittext_destination, "Where are you going to?")
            autoCompleteFieldDes()

        }
        isModifyMode = intent.getBooleanExtra("modifyTrip", false)
        if (isModifyMode) {
            tripToModify = intent.getSerializableExtra("tripToModify") as Trip
        }
        if (isModifyMode) {
            modifyTrip(tripToModify)
        }
        isAcceptedTrip = intent.getBooleanExtra("acceptedTrip", false)
        if (isAcceptedTrip){
            tripRequest = intent.getSerializableExtra("requestInfo") as TripRequest
            setupRequestFields(tripRequest)
        }



        addStops()
        vehicleTypeSpinner()
        vehicleColorSpinner()
        fillBookingPref()
        fillSeatOptions()
        fillLuggageOptions()
        fillOtherOptions()
        dateField()
        timeField()
        skipVehicleOption()
        showPictureDialog()
        trip_ac_submit_btn.setOnClickListener {
            fillAllForm()
        }


    }
    private fun setupRequestFields(tRequest: TripRequest){
        setOrigin(tRequest.originCity)
        setDestination(tRequest.destCity)
        search_date_edittext.setText(tripRequest.date)
        editText_time.setText(tripRequest.time)



    }
    private fun modifyTrip(trip: Trip) {
        Log.i("modifyTrip", isModifyMode.toString())
        setOrigin(trip.origin)
        setDestination(trip.destination)
        edittext_tripdesc.setText(trip.description)
        edittext_price.setText(trip.pricePerSeat.toString())

    }

    private fun setPb(visibility: Int) {
        if (visibility == 1) {
            progressBar1.visibility = View.VISIBLE
            greout_layout.visibility = View.VISIBLE
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        } else {
            progressBar1.visibility = View.GONE
            greout_layout.visibility = View.GONE
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == mAutoCompleteRequestCode) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    fillOrigin(place)
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                    val status = Autocomplete.getStatusFromIntent(data!!)
                    Log.i("PostingActivity", status.statusMessage)
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
        }
        //
        if (requestCode == mAutoCompleteRequestCodeDest) {
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
        if (requestCode == mGALLERY) {
            if (data != null) {
                val contentURI = data.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    //val path = saveImage(bitmap)
                    Toast.makeText(this, "Image Saved!", Toast.LENGTH_SHORT).show()
                    carBtn.setImageBitmap(bitmap)
                    isImageSet = true

                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
                }

            }

        } else if (requestCode == mCAMERA) {
            val thumbnail = data!!.extras!!.get("data") as Bitmap
            carBtn.setImageBitmap(thumbnail)
            saveImage(thumbnail)
            Toast.makeText(this, "Image Saved!", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun getSubCityNameByName(name: String): String? {
        val addresses = mGeocoder.getFromLocationName(name, 1)
        return if (addresses != null && addresses.size > 0) {
            if (addresses[0].locality != null) {
                addresses[0].locality
            } else {
                "EMPTY"
            }
        } else null
    }

    @Throws(IOException::class)
    private fun getCityNameByName(name: String): String? {
        val addresses = mGeocoder.getFromLocationName(name, 1)
        return if (addresses != null && addresses.size > 0) {
            if (addresses[0].adminArea != null) {
                addresses[0].adminArea
            } else {
                "EMPTY"
            }

        } else null
    }

    @Throws(IOException::class)
    private fun getCityNameByCoordinates(lat: Double, lon: Double, isOrigin: Boolean): String? {


        val addresses = mGeocoder.getFromLocation(lat, lon, 3)




        return if (addresses != null && addresses.size > 0) {
            // Here are some results you can geocode
            val city: String
            val state: String
            if (addresses[0].locality != null) {
                city = addresses[0].locality
                if (isOrigin) {
                    originSubCity = city
                } else {
                    destSubCity = city
                }

                Log.d("city", city)
            }
            if (addresses[0].adminArea != null) {
                state = addresses[0].adminArea
                if (isOrigin) {
                    originCity = state
                } else {
                    destCity = state
                }

                Log.d("state", state)
            }
            addresses[0].countryName
        } else null
    }

    private fun autoCompleteFieldOrigin() {
        val fields = Arrays.asList(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.ADDRESS_COMPONENTS,
            Place.Field.LAT_LNG
        )
        // Start the autocomplete intent.
        val intent = com.google.android.libraries.places.widget.Autocomplete.IntentBuilder(
            AutocompleteActivityMode.FULLSCREEN, fields
        ).setCountry("DZ")
            .build(this)

        startActivityForResult(intent, mAutoCompleteRequestCode)
    }

    private fun autoCompleteFieldDes() {
        val fields = Arrays.asList(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS_COMPONENTS,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )
        // Start the autocomplete intent.
        val intent = com.google.android.libraries.places.widget.Autocomplete.IntentBuilder(
            AutocompleteActivityMode.FULLSCREEN, fields
        ).setCountry("DZ")
            .build(this)

        startActivityForResult(intent, mAutoCompleteRequestCodeDest)
    }

    private fun addStops() {
        var co = -1
        add_field_label.setOnClickListener {
            val parentView = findViewById<LinearLayout>(R.id.relative_layout_form)
            val inflater = LayoutInflater.from(this)
            val viewer = inflater.inflate(R.layout.textfield_form, parentView, false)
            val id = View.generateViewId()
            viewer.id = id
            parentView.addView(viewer)
            numberOfStops++
            val myList = ArrayList<TextFieldBoxes>()
            if (parentView.childCount > 0) {
                val exitView = parentView.findViewById(id) as TextFieldBoxes
                myList.add(exitView)
                //add stops to list


                exitView.endIconImageButton.setOnClickListener {
                    for (box in myList) {
                        Toast.makeText(this, "clicked " + box.id, Toast.LENGTH_SHORT).show()
                        Toast.makeText(this, "y pressed on " + box.id, Toast.LENGTH_SHORT).show()
                        //todo       removeStopFromList(findViewById(box.id))
                        parentView.removeView(findViewById(box.id))
                        Log.i("Remove Tag", "Removed $box.id")

                        co -= 1
                        numberOfStops--


                    }
                }

            }

            if (co >= 0) {
                setup(parentView.getChildAt(co))
                //todo addStopToList(parentView.getChildAt(co))
            }
            co += 1

            for (stop in stops) {
                Log.i("List of Stops", "$stop and ${stops.size}")
            }
        }

    }

    private fun fillAllForm() {
        var correctForm = true
        Log.i("fillAllForms", correctForm.toString())
        if (edittext_origin.text.toString() == "" || edittext_origin.text == null) {
            correctForm = false
            origin_label.setError("Invalid", true)
        } else {
            originText = edittext_origin.text.toString()
            origin_label.removeError()
        }
        if (edittext_destination.text.toString() == "" || edittext_destination.text == null) {
            correctForm = false
            destination_label.setError("Invalid", true)
        } else {
            destinationText = edittext_destination.text.toString()
            destination_label.removeError()
        }


        if (!vehicleSkipped) {

            //vehicle model
            vehicleModel = editText_car_model.text.toString()
            if (year_EditText.text.toString() != "" && year_EditText.text != null) {
                //vehicle year parsing
                try {
                    vehicleYear = year_EditText.text.toString().toInt()
                } catch (e: NumberFormatException) {
                    e.stackTrace
                }
                val validatorYear = year_EditText.validator()
                val isValidYear = validatorYear.addRule(ValidNumberRule())
                    .addRule(MaxLengthRule(4))
                    .addRule(MinLengthRule(4))
                    .addRule(GreaterThanRule(1900))
                    .addRule(LessThanOrEqualRule(2019))
                    .addRule(OnlyNumbersRule())
                    .check()
                if (!isValidYear) {
                    correctForm = false
                    year_TextField.error = "Invalid Year"
                } else {
                    year_TextField.error = ""
                    year_TextField.isErrorEnabled = false
                }
            }
            if (licenseplate_EditText.text.toString() != "" && licenseplate_EditText.text != null) {
                licensePlate = licenseplate_EditText.text.toString()
                val validatorLicense = licenseplate_EditText.validator()
                val isValidLicense = validatorLicense.addRule(ValidNumberRule())
                    .addRule(MaxLengthRule(10))
                    .addRule(MinLengthRule(10))
                    .addRule(OnlyNumbersRule())
                    .check()
                if (!isValidLicense) {
                    correctForm = false
                    license_TextField.error = "Invalid license plate"
                } else {
                    license_TextField.error = ""
                    license_TextField.isErrorEnabled = false
                }
            }
        }

        try {
            tripPrice = edittext_price.text.toString().toInt()
        } catch (e: NumberFormatException) {
            e.stackTrace
        }

        val validatorPrice = edittext_price.validator()
        val isValidPrice = validatorPrice.addRule(ValidNumberRule())
            .addRule(LessThanRule(10000))
            .addRule(OnlyNumbersRule())
            .check()
        if (!isValidPrice) {
            correctForm = false
            price_label.setError("Invalid Price", false)
        } else {
            price_label.removeError()
        }



        tripDescription = edittext_tripdesc.text.toString()
        tripDate = search_date_edittext.text.toString()
        val isValidDate = isValidDate(tripDate)


        if (!isValidDate) {
            correctForm = false
            add_datebtn.setError("Please enter valid date at least after 24h", false)
        } else {
            add_datebtn.removeError()
        }
        // val validatorDate = editText_date.validator()
        //var isValidDate = validatorDate.addRule()
        tripTime = editText_time.text.toString()
        val isValidTime = isValidTime(tripTime)
        if (!isValidTime) {
            correctForm = false
            add_timebtn.setError("Please enter a valid time", false)
        } else {
            add_timebtn.removeError()
        }

        if (numberOfStops > 0) {
            addStopViewToList()
            fillStops()
        }
        Log.i("finish uploading lol", correctForm.toString())
        if (correctForm) {

            if (!this.vehicleSkipped) {
                if (isImageSet) {
                    Log.i("SAVE PHotos", correctForm.toString())
                    savePhotoToDatabase(carBtn)
                } else {
                    Log.i("ComPELTE ", correctForm.toString())
                    finishUploading()
                }

            } else {
                Log.i("DONE?", correctForm.toString())
                finishUploading()
            }

            //start activity
        } else {
            Toast.makeText(this, "Wrong Information, check and try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun finishUploading() {
        Log.i("finish uploading method", "TRUE")
        val luggageOption = fillLuggageOptions()
        val noSmoking = fillOtherOptions()[0] == 1
        val petsAllowed = fillOtherOptions()[1] == 1
        val seatOption = fillSeatOptions()
        val bookingPref = fillBookingPref()


        val carPhoto = downloadUrl
        val trip = Trip(
            originText,
            destinationText,
            stops,
            tripDate,
            luggageOption,
            tripTime,
            seatOption,
            tripPrice,
            bookingPref
        )
        if (!vehicleSkipped) {
            trip.addVehicleInfo(vehicleModel, vehicleType, vehicleColor, vehicleYear, licensePlate, carPhoto)
        }
        trip.addPreferences(noSmoking, petsAllowed)
        trip.addDescription(tripDescription)
        trip.originFullAddress = originFullAddress
        trip.destFullAddress = destFullAddress
        trip.destSubCity = destSubCity
        trip.originSubCity = originSubCity
        trip.originCity = originCity
        trip.destCity = destCity
        //trip.tripID = UUID.randomUUID().toString()
        trip.userID = mDatabase.currentUserId()
        trip.addTripDestinations(originCity, originSubCity, originFullAddress, destCity, destSubCity, destFullAddress)
        trip.printAllInfo()
        /*
        val intent = Intent(this, TripsListActivity::class.java)
        intent.putExtra("PostingActivity",trip)
        startActivity(intent) */
        Log.i("DB", trip.origin)

        saveToDB(trip)


    }

    private fun saveData(trip: Trip) {
        val mPrefs = this.getSharedPreferences("TripsPref", Context.MODE_PRIVATE)!!
        val prefsEditor = mPrefs.edit()
        val nbTrips = mPrefs.getInt("savedTrips", 0)
        val gson = Gson()
        val json = gson.toJson(trip)
        val tripID = "TripID$nbTrips"
        prefsEditor.putString(tripID, json)
        prefsEditor.putInt("savedTrips", nbTrips)
        prefsEditor.apply()
        //showDoneDialog()
    }

    private fun showDoneDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Trip Saved!")
            .setMessage("You can view and edit your trip in home screen!")

            // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton("Done") { _, _ ->

                Toast.makeText(this, "Trip Posted!", Toast.LENGTH_SHORT).show()
                finish()
            }

            // A null listener allows the button to dismiss the dialog and take no further action.
            //.setNegativeButton("Done!", null)
            .setIcon(R.drawable.ic_done_green_24dp)
            .show()
    }


    private fun isValidDate(dateString: String): Boolean {
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
            Log.i("PostingActivity", c.time.toString())
            date.after(c.time)

        } else {
            false
        }

    }

    private fun isValidTime(timeString: String): Boolean {
        var date: Date? = null
        val myFormat = "HH:mm"
        try {
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            date = sdf.parse(timeString)
            if (timeString != sdf.format(date)) {
                date = null
            }
        } catch (ex: ParseException) {
            ex.printStackTrace()
        }
        return date != null
    }

    private fun convertDPtoInt(myDP: Float): Int {
        val r = resources
        // == return px
        return Math.round(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, myDP, r.displayMetrics
            )
        )
    }

    private fun fillBookingPref(): Int {
        book_instantly_card.setOnClickListener {
            var imgBackground = instantbook_layout.background as GradientDrawable
            //   imgBackground.setColor(ContextCompat.getColor(this,R.color.colorAccent))
            imgBackground.setStroke(
                convertDPtoInt(1f), ContextCompat.getColor(
                    this,
                    R.color.colorAccent
                )
            )

            imgBackground = requestbook_layout.background as GradientDrawable
            imgBackground.setStroke(convertDPtoInt(1f), ContextCompat.getColor(this, R.color.btnBlack))

            //   imgBackground.setColor(ContextCompat.getColor(this,R.color.whiteColor))

            bookingOption = 1
        }
        request_to_book_card.setOnClickListener {

            var imgBackground = requestbook_layout.background as GradientDrawable
            imgBackground.setStroke(
                convertDPtoInt(1f), ContextCompat.getColor(
                    this,
                    R.color.colorAccent
                )
            )

            imgBackground = instantbook_layout.background as GradientDrawable
            imgBackground.setStroke(convertDPtoInt(1f), ContextCompat.getColor(this, R.color.btnBlack))
            bookingOption = 0
        }
        return bookingOption
    }

    private fun setOrigin(origin: String) {
        edittext_origin.setText(origin)
    }

    private fun setDestination(destination: String) {
        edittext_destination.setText(destination)
    }

    private fun fillOrigin(place: Place) {
        originCity = ""
        originSubCity = ""
        originLatLng = place.latLng
        if (place.addressComponents?.asList()!!.size == 1) {
            Toast.makeText(this, "Pick a valid place.", Toast.LENGTH_SHORT).show()
        } else {
            val placeName = place.addressComponents?.asList()!![1].name
            //take cords from geolocation
            getCityNameByCoordinates(originLatLng?.latitude!!, originLatLng?.longitude!!, true)
            //check if they're init
            if (originCity == "") {
                originCity = getCityNameByName(placeName)!!
            }
            if (originSubCity == "") {
                originSubCity = getSubCityNameByName(placeName)!!
                if (originSubCity == "EMPTY") {
                    originSubCity = originCity
                }
            }
            originFullAddress = place.address!!
            Log.i("state", originCity)
            Log.i("city", originSubCity)
            Log.i("address", originFullAddress)

            if (place.address != null) {
                Log.i("PLACE ADDRESS", place.address)
            } else {
                Log.i("PLACE ADDRESS", "NU::")
            }
            val decodedOriginCity = decodeWilaya(originCity)
            originCode = decodedOriginCity

            Log.i("DECODE", decodedOriginCity.toString())
            setOrigin(place.name!!)
            Log.i("SetOrigin", originSubCity)
            // setOrigin("$place.name!!")
        }


    }

    private fun fillDest(place: Place) {
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
        getCityNameByCoordinates(desLatLng?.latitude!!, desLatLng?.longitude!!, false)
        //check if they're init
        if (destCity == "") {
            destCity = getCityNameByName(placeName)!!
        }
        if (destSubCity == "") {
            destSubCity = getSubCityNameByName(placeName)!!
            if (destSubCity == "EMPTY") {
                destSubCity = destCity
            }
        }
        destFullAddress = place.address!!
        Log.i("state", destCity)
        Log.i("city", destSubCity)
        Log.i("address", destFullAddress)


        if (place.address != null) {
            Log.i("PLACE ADDRESS", place.address)
        } else {
            Log.i("PLACE ADDRESS", "NUll")
        }
        val decodedDestCity = decodeWilaya(destCity)
        destinationCode = decodedDestCity
        setDestination(place.name!!)


    }

    private fun fillLuggageOptions(): Int {
        val luggageBtns = arrayListOf<LinearLayout>(pref_btn_no, pref_btn_s, pref_btn_m, pref_btn_l)
        for ((index, btn) in luggageBtns.withIndex()) {
            btn.setOnClickListener {
                luggageOption = index
                //change to global variable
                Toast.makeText(this, "index is $luggageOption", Toast.LENGTH_SHORT).show()
                btn.setBackgroundColor(ContextCompat.getColor(this, R.color.btnBlack))
                val btnText = btn.getChildAt(1) as TextView
                val btnImg = btn.getChildAt(0) as ImageView
                btnText.setTextColor(ContextCompat.getColor(this, R.color.backgroundColor))
                btnImg.setImageResource(R.drawable.ic_work_white_24dp)
                for (otherBtn in luggageBtns) {
                    if (otherBtn != btn) {
                        otherBtn.setBackgroundColor(
                            ContextCompat.getColor(
                                this,
                                R.color.backgroundColor
                            )
                        )
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

    private fun fillSeatOptions(): Int {
        val seatBtns = arrayListOf<TextView>(seat1, seat2, seat3, seat4, seat5, seat6, seat7)
        for ((indexSeat, seatBtn) in seatBtns.withIndex()) {
            seatBtn.setOnClickListener {
                seatOption = indexSeat + 1
                seatBtn.setTextColor(ContextCompat.getColor(this, R.color.backgroundColor))
                val imgBackground = seatBtn.background as GradientDrawable
                imgBackground.setColor(ContextCompat.getColor(this, R.color.btnBlack))

                for (sBtn in seatBtns) {
                    if (sBtn != seatBtn) {
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

    private fun vehicleColorSpinner() {
        val spinner = findViewById<MaterialSpinner>(R.id.spinner_color)
        spinner.setItems(
            "Black",
            "White",
            "Blue",
            "Red",
            " Light Gray",
            "Dark Gray",
            "Yellow",
            "Green",
            "Gold",
            "Beige"
        )
        spinner.setOnItemSelectedListener { view, _, _, item ->
            vehicleColor = item.toString()
            Snackbar.make(
                view,
                "Saved $item!",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun vehicleTypeSpinner() {
        val spinner = findViewById<MaterialSpinner>(R.id.spinner_type)
        spinner.setItems("Car", "Bus", "Taxi", "Van", "Minivan", "Truck", "Helix")
        spinner.setOnItemSelectedListener { view, _, _, item ->
            vehicleType = item.toString()
            Snackbar.make(
                view,
                "Saved $item !",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun fillOtherOptions(): ArrayList<Int> {
        val otherBtns = arrayListOf<LinearLayout>(other_btn_smoke, other_btn_pet)
        val imgIconList = arrayListOf(
            R.drawable.ic_smoke_free_white_24dp,
            R.drawable.ic_pets_white_24dp,
            R.drawable.ic_smoke_free_black_24dp,
            R.drawable.ic_pets_black_24dp
        )
        val colorList = arrayListOf(
            R.color.btnBlack,
            R.color.backgroundColor,
            R.color.backgroundColor,
            R.color.btnBlack
        )
        for ((indexOther, otherOptionBtn) in otherBtns.withIndex()) {
            //todo fix colors
            var otherBtnClicked = 0
            otherOptionBtn.setOnClickListener {

                val imgBackground = otherOptionBtn.background as GradientDrawable
                val btnText = otherOptionBtn.getChildAt(1) as TextView
                val otherBtnImg = otherOptionBtn.getChildAt(0) as ImageView
                imgBackground.setColor(ContextCompat.getColor(this, colorList[otherBtnClicked]))
                btnText.setTextColor(ContextCompat.getColor(this, colorList[otherBtnClicked + 1]))

                if (otherBtnImg.contentDescription.contains("smoke_icon")) {
                    otherBtnImg.setImageResource(imgIconList[otherBtnClicked])
                } else if (otherBtnImg.contentDescription.contains("pet_icon")) {
                    otherBtnImg.setImageResource(imgIconList[otherBtnClicked + 1])
                }
                otherBtnClicked = if (otherBtnClicked == 0) {
                    2
                } else {
                    0
                }
                otherOptions[indexOther] = otherBtnClicked - 1


            }

            //  other.background = getDrawable(R.drawable.custom_border)


        }
        return otherOptions
    }

    private fun saveImage(myBitmap: Bitmap): String {
        val bytes = ByteArrayOutputStream()
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val wallpaperDirectory = File(
            (Environment.getExternalStorageDirectory()).toString() + IMAGE_DIRECTORY
        )
        // have the object build the directory structure, if needed.
        Log.d("fee", wallpaperDirectory.toString())
        if (!wallpaperDirectory.exists()) {

            wallpaperDirectory.mkdirs()
        }

        try {
            Log.d("heel", wallpaperDirectory.toString())
            val f = File(
                wallpaperDirectory, ((Calendar.getInstance()
                    .timeInMillis).toString() + ".jpg")
            )
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(
                this,
                arrayOf(f.path),
                arrayOf("image/jpeg"), null
            )
            fo.close()
            Log.d("TAG", "File Saved::--->" + f.absolutePath)

            return f.absolutePath
        } catch (e1: IOException) {
            e1.printStackTrace()
        }

        return ""
    }

    companion object {
        private const val IMAGE_DIRECTORY = "/RideGO_Images"
    }

    private fun showPictureDialog() {
        carBtn.setOnClickListener {
            val pictureDialog = AlertDialog.Builder(this)
            pictureDialog.setTitle("Select Action")
            val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
            pictureDialog.setItems(
                pictureDialogItems
            ) { _, which ->
                when (which) {
                    0 -> choosePhotoFromGallery()
                    1 -> takePhotoFromCamera()
                }
            }
            pictureDialog.show()
        }
        imgTextSelectCar.visibility = View.GONE

    }

    private fun choosePhotoFromGallery() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        startActivityForResult(galleryIntent, mGALLERY)
    }

    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, mCAMERA)
    }

    private fun savePhotoToDatabase(imageView: ImageView) {
        // val file = Uri.fromFile(File("path/to/images/rivers.jpg"))
        setPb(1)
        val uniqueId = UUID.randomUUID().toString()
        val riversRef = mStorageRef.child("car_images/$uniqueId.jpg")
// Get the data from an ImageView as bytes

        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = riversRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
            Log.i("UPLOAD", "FAILED")
        }.addOnSuccessListener { it1 ->
            //todo add download url
            val result = it1.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener {
                downloadUrl = it.toString()
                //downloadUrl = uniqueId
                Log.i("UPLOAD", downloadUrl)
                finishUploading()
            }

        }

    }

    private fun dateField() {
        val myCalendar = Calendar.getInstance()
        val date = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel(myCalendar)
        }
        add_datebtn.setOnClickListener {
            DatePickerDialog(
                this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun timeField() {

        val myCalendar = Calendar.getInstance()
        val time = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            myCalendar.set(Calendar.MINUTE, minute)
            updateTimeLabel(myCalendar)
        }

        add_timebtn.setOnClickListener {
            //   TimePickerDialog(this,myCalendar.get(Calendar.HOUR_OF_DAY),myCalendar.get(Calendar.MINUTE))
            TimePickerDialog(
                this,
                time,
                myCalendar.get(Calendar.HOUR_OF_DAY),
                myCalendar.get(Calendar.MINUTE),
                true
            ).show()
            // DatePickerDialog(this,date,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),
            //      myCalendar.get(Calendar.DAY_OF_MONTH)).show()

        }

    }

    private fun skipVehicleOption(): Boolean {
        skip_vehicle_check.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                vehicleSkipped = true
                vehicle_linearlayout.visibility = View.GONE
            } else {
                vehicleSkipped = false
                vehicle_linearlayout.visibility = View.VISIBLE
            }
        }
        return vehicleSkipped
    }

    private fun updateTimeLabel(myCalendar: Calendar) {
        val myFormat = "HH:mm"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        editText_time.setText(sdf.format(myCalendar.time))
    }

    private fun updateLabel(myCalendar: Calendar) {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        search_date_edittext.setText(sdf.format(myCalendar.time))
    }

    private fun showToolTip(editText: EditText, text: String) {

        Tooltip.Builder(editText)
            .setText(text)
            .setCornerRadius(15f)
            .setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent))
            .setTextColor(ContextCompat.getColor(this, R.color.whiteColor))
            .setCancelable(true)
            .show()


    }

    private fun setup(v: View) {

        val edit = v.findViewById<ExtendedEditText>(R.id.edittext_label)
        Toast.makeText(this, edit.text.toString(), Toast.LENGTH_SHORT).show()
    }

    private fun addStopViewToList() {
        val parentView = findViewById<LinearLayout>(R.id.relative_layout_form)
        var i = 0
        while (i < parentView.childCount) {
            stopsList.add(parentView.getChildAt(i))
            i++
        }
    }

    private fun fillStops() {
        for (stopView in stopsList) {
            val edit = stopView.findViewById<ExtendedEditText>(R.id.edittext_label)
            Toast.makeText(this, edit.text.toString() + stopView.id, Toast.LENGTH_SHORT).show()
            stops.add(edit.text.toString())
        }

    }

    private fun saveToDB(trip: Trip) {
        setPb(1)
        val otdPath = "${String.format("%02d", originCode)}_${String.format("%02d", destinationCode)}"
        if (isAcceptedTrip){
            //todo check
            val path = "trips/$otdPath/${tripRequest.tripID}/"
            trip.userID = mDatabase.currentUserId()
            trip.bookedUsers[tripRequest.poster?.userId!!] = 1
            mDatabase.addToPath(path,trip)
            mDatabase.addToPath("users/${tripRequest.userID}/bookedTrips/${trip.tripID}",otdPath)
        }else {
        if (!isModifyMode) {
            var path = "trips/$otdPath"
            val tripID = mDatabase.pushKey(path)
            trip.tripID = tripID
            path = "$path/$tripID"
            trip.userID = mDatabase.currentUserId()
            mDatabase.addToPath(path, trip)
        } else {
            val path = "trips/$otdPath/${tripToModify.tripID}"

            trip.tripID = tripToModify.tripID
            trip.userID = mDatabase.currentUserId()
            mDatabase.addToPath(path, trip)
            for (user in trip.bookedUsers) {
                val userPath = "users/${user.key}/notifications/modifiedTrips/${trip.tripID}"
                mDatabase.addToPath(userPath, otdPath)
            }

        }
        }
        ///
        //
        val root = "users/${mDatabase.currentUserId()}"
        var mPath = "$root/trips/${trip.tripID}"
        mDatabase.addToPath(mPath, otdPath)
        mPath = "$root/activeTrips/${trip.tripID}"
        mDatabase.addToPath(mPath, otdPath)
        setPb(0)
        saveData(trip)
        showDoneDialog()


    }

    /* private fun getSharedUser(): User {
         val mPrefs = this.getSharedPreferences("TripsPref", Context.MODE_PRIVATE)!!
         val gson = Gson()
         val json = mPrefs.getString("currentUser", "")
         val obj = gson.fromJson<User>(json, User::class.java)
         return obj
     }*/

}