package me.lamine.goride.searchActivity


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.beust.klaxon.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete.getPlaceFromIntent
import com.google.android.libraries.places.widget.Autocomplete.getStatusFromIntent
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.android.synthetic.main.activity_search.*
import me.lamine.goride.R
import me.lamine.goride.dataObjects.TripSearchData
import me.lamine.goride.utils.decodeWilaya
import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class SearchTripActivity : AppCompatActivity(), OnMapReadyCallback {
    private var originCity: String = ""
    private var originSubCity: String = ""
    private lateinit var originLatLng: LatLng
    private lateinit var desLatLng: LatLng
    private lateinit var originFullAddress: String
    private var destCity: String = ""
    private var originCode = 0
    private var destinationCode = 0
    private var destSubCity: String = ""
    private var destFullAddress: String = ""
    private var mMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var mAutoRequestCode = 1
    private var mAutoRequestCodeDest = 2
    private var fromLatLng: LatLng? = null
    private var toLatLng: LatLng? = null
    private val apiKey: String = "AIzaSyDWbc3KQP6ssBlClf8HSiZWEtMxfwqSYto"
    private var fromPlace: String = ""
    private var toPlace: String = ""
    private var tripDate= ""
    private lateinit var mGeocoder: Geocoder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setSupportActionBar(findViewById(R.id.search_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // we add permissions we need to request location of the users
        mGeocoder = Geocoder(this, Locale.getDefault())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        async {
            uiThread {
                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.mapView) as SupportMapFragment
                mapFragment.getMapAsync(this@SearchTripActivity)
            }
        }

        switchBtn.setOnClickListener {
            val oci = originSubCity
            val oco = originCode
            originCode = destinationCode
            originSubCity = destSubCity
            destinationCode = oco
            destSubCity = oci

            val text: String = editText_To.text.toString()
            editText_To.setText(editText_from.text.toString())
            editText_from.setText(text)
        }


        //val apiKey:String = "AIzaSyDWbc3KQP6ssBlClf8HSiZWEtMxfwqSYto"
        // Initialize Places.
        Places.initialize(applicationContext, apiKey)
        // Create a new Places client instance.
        Places.createClient(this)
        textbox_from.isResponsiveIconColor = false
        textbox_from.setOnClickListener {
            // Set the fields to specify which types of place data to
            // return after the user has made a selection.

            val fields =
                Arrays.asList(Place.Field.ID, Place.Field.ADDRESS_COMPONENTS, Place.Field.NAME, Place.Field.LAT_LNG)
            // Start the autocomplete intent.
            val intent = com.google.android.libraries.places.widget.Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields
            ).setCountry("DZ")
                .build(this)

            startActivityForResult(intent, mAutoRequestCode)


        }
        textbox_to.isResponsiveIconColor = false
        textbox_to.setOnClickListener {
            // Set the fields to specify which types of place data to
            // return after the user has made a selection.
            val fields =
                Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS_COMPONENTS, Place.Field.LAT_LNG)

            // Start the autocomplete intent.
            val intent = com.google.android.libraries.places.widget.Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields
            ).setCountry("DZ")
                .build(this)
            startActivityForResult(intent, mAutoRequestCodeDest)

        }

        add_datebtn.hasClearButton = true
        add_datebtn.setOnClickListener { Toast.makeText(this,"Click to enter a search date",Toast.LENGTH_SHORT) .show()
            dateField() }
        search_date_edittext.setOnClickListener { dateField() }
        search_date_edittext.setOnClickListener { dateField() }
        search_btn.setOnClickListener {

            val intent = Intent(this, TripsListActivity::class.java)
            //  Log.i("toFrom")
            val tsd =
                TripSearchData(originCode, destinationCode, originSubCity, destSubCity)
            tsd.tripDate = search_date_edittext.text.toString()
            intent.putExtra("tsd", tsd)
            intent.putExtra("to",destSubCity)
            intent.putExtra("from",originSubCity)
            startActivity(intent)

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
        DatePickerDialog(
            this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
            myCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateLabel(myCalendar: Calendar) {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)

        search_date_edittext.setText(sdf.format(myCalendar.time))
     //   tripDate = search_date_edittext.text.toString()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION) {
            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> obtieneLocalizacion()
                PackageManager.PERMISSION_DENIED -> Toast.makeText(
                    this,
                    "PLEASE GRANT PERMISSION IN SETTINGS",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 100
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(latitude, longitude)
        //  mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun fillOrigin(place: Place) {
        originLatLng = place.latLng!!
        originCity = ""
        originSubCity = ""
        val placeName = place.addressComponents?.asList()!![1].name
        //take cords from geolocation
        getCityNameByCoordinates(originLatLng.latitude, originLatLng.longitude, true)
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
        originFullAddress = place.name!!
        val decodedOriginCity = decodeWilaya(originCity)
        originCode = decodedOriginCity
        Log.i("DECODE", decodedOriginCity.toString())

    }

    private fun fillDest(place: Place) {
        desLatLng = place.latLng!!
        destCity = ""
        destSubCity = ""
        val placeName = place.addressComponents?.asList()!![1].name
        //take cords from geolocation
        getCityNameByCoordinates(desLatLng.latitude, desLatLng.longitude, false)
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
        destFullAddress = place.name!!
        val decodedDestCity = decodeWilaya(destCity)
        destinationCode = decodedDestCity

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
        var addresses: MutableList<Address> = mutableListOf()
        try {
            addresses = mGeocoder.getFromLocation(lat, lon, 3)
        } catch (e: IOException) {
            Toast.makeText(this, "Error precessing locations, try again!", Toast.LENGTH_SHORT).show()
        }



        return if (addresses.size > 0) {
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

    private fun setFromP(fromP: String) {
        fromPlace = fromP
        if (fromPlace != "") {
            editText_from.setText(fromPlace)
        }
    }

    private fun setToP(toP: String) {
        toPlace = toP
        if (toPlace != "") {
            editText_To.setText(toPlace)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == mAutoRequestCode) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val place = getPlaceFromIntent(data!!)
                    fromLatLng = place.latLng
                    setFromP(place.name!!)
                    fillOrigin(place)
                    if (editText_To.text.toString() != "") {
                        if (mMap != null){
                            addMarkersOnMap()
                        }

                    }

                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                    val status = getStatusFromIntent(data!!)
                    Log.i("SearchActivity", status.statusMessage)
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
        }
        if (requestCode == mAutoRequestCodeDest) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val place = getPlaceFromIntent(data!!)
                    toLatLng = place.latLng
                    Log.i("SearchActivity", "Place: " + place.name + ", " + place.id)
                    setToP(place.name!!)
                    fillDest(place)
                    if (editText_from.text.toString() != "") {
                        if (mMap != null){
                            addMarkersOnMap()
                        }

                    }

                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                    val status = getStatusFromIntent(data!!)
                    Log.i("SearchActivity", status.statusMessage)
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
        }
    }

    private fun permCheck() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_ACCESS_FINE_LOCATION
            )
            return
        }
    }


    @SuppressLint("MissingPermission")
    private fun addMarkersOnMap() {
        mMap?.clear()
        if (fromLatLng != null) {
            mMap?.addMarker(MarkerOptions().position(fromLatLng!!).title("Origin Location"))
            mMap?.addMarker(MarkerOptions().position(toLatLng!!).title("Destination Location"))
            mMap?.moveCamera(CameraUpdateFactory.newLatLng(fromLatLng))
        }

        val latLongB = LatLngBounds.Builder()
        var origin = LatLng(0.0, 0.0)
        var dest = LatLng(0.0, 0.0)
        if (fromLatLng != null && toLatLng != null) {
            origin = fromLatLng!!
            dest = toLatLng!!
        }
        val sydney = origin
        val opera = dest
        val options = PolylineOptions()
        options.color(Color.RED)
        options.width(5f)
        // Getting URL to the Google Directions API
        // build URL to call API
        val url = getUrl(sydney, opera)

        async {
            // Connect to URL, download content and convert into string asynchronously
            val result = URL(url).readText()
            uiThread {
                // When API call is done, create parser and convert into JsonObjec
                val parser = Parser()
                val stringBuilder: StringBuilder = StringBuilder(result)
                val json: JsonObject = parser.parse(stringBuilder) as JsonObject
                // get to the correct element in JsonObject
                //todo fix
                val routes = json.array<JsonObject>("routes")
                @Suppress("UNCHECKED_CAST") val points = routes!!["legs"]["steps"][0] as JsonArray<JsonObject>
                // For every element in the JsonArray, decode the polyline string and pass all points to a List
                val polypts = points.flatMap { decodePoly(it.obj("polyline")?.string("points")!!) }
                // Add  points to polyline and bounds
                options.add(sydney)
                latLongB.include(sydney)
                for (point in polypts) {
                    options.add(point)
                    latLongB.include(point)
                }
                options.add(opera)
                latLongB.include(opera)
                // build bounds
                val bounds = latLongB.build()
                // add polyline to the map
                mMap?.addPolyline(options)
                // show map with route centered
                mMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun obtieneLocalizacion() {
        permCheck()
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                latitude = location?.latitude!! //todo ask user to turn location on
                longitude = location.longitude
            }
        Log.i("test", "Latitute: $latitude ; Longitute: $longitude")
        val sydney = LatLng(latitude, longitude)
        //   mMap.addMarker(MarkerOptions().position(sydney).title("Your Location"))
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun getUrl(from: LatLng, to: LatLng): String {
        val origin = "origin=" + from.latitude + "," + from.longitude
        val dest = "destination=" + to.latitude + "," + to.longitude
        val sensor = "sensor=false"
        val params = "$origin&$dest&$sensor"
        return "https://maps.googleapis.com/maps/api/directions/json?$params&key=$apiKey"
    }

    //decode Poly to draw line
    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(p)
        }

        return poly
    }

}