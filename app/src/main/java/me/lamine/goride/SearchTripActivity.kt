package me.lamine.goride

import android.Manifest
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_search.*

import android.content.Intent
import java.util.*


import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient
import android.R.attr.apiKey
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.beust.klaxon.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.libraries.places.internal.i
import com.google.android.libraries.places.widget.Autocomplete.getStatusFromIntent
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.Autocomplete.getPlaceFromIntent
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class SearchTripActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude:Double = 0.0
    private var longitude:Double = 0.0
    private var AUTOCOMPLETE_REQUEST_CODE = 1
    private var AUTOCOMPLETE_REQUEST_CODE_to = 2
    private var fromLatLng:LatLng? = null
    private var toLatLng: LatLng? = null
    val apiKey:String = "AIzaSyDWbc3KQP6ssBlClf8HSiZWEtMxfwqSYto"
    var fromPlace:String = ""
    var toPlace:String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        setSupportActionBar(findViewById(R.id.search_toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowHomeEnabled(true);

        // we add permissions we need to request location of the users

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
        //val apiKey:String = "AIzaSyDWbc3KQP6ssBlClf8HSiZWEtMxfwqSYto"
        // Initialize Places.
        Places.initialize(applicationContext, apiKey)
        // Create a new Places client instance.
        val placesClient = Places.createClient(this)






        textbox_from.isResponsiveIconColor = false
        textbox_from.setOnClickListener {
            // Set the fields to specify which types of place data to
            // return after the user has made a selection.
            val fields = Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG)
            // Start the autocomplete intent.
            val intent = com.google.android.libraries.places.widget.Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields
            ).setCountry("DZ")
                .build(this)

            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)

        }
        textbox_to.isResponsiveIconColor = false
        textbox_to.setOnClickListener {
            // Set the fields to specify which types of place data to
            // return after the user has made a selection.
            val fields = Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG)

            // Start the autocomplete intent.
            val intent = com.google.android.libraries.places.widget.Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields
            ) .setCountry("DZ")
                .build(this)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_to)
        }
        search_btn.setOnClickListener {
            addMarkersOnMap()

        }

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
                PackageManager.PERMISSION_DENIED -> Toast.makeText(this,"PLEASE GRANT PERMISSION IN SETTINGS",Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 100
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
      //  obtieneLocalizacion()
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(latitude, longitude)
      //  mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
    private fun setFromP(fromP:String) {
        fromPlace = fromP
        if (fromPlace != "") {
            editText_from.setText(fromPlace)
        }
    }
    private fun setToP(toP:String) {
        toPlace = toP
        if (toPlace != "") {
            editText_To.setText(toPlace)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val place = com.google.android.libraries.places.widget.Autocomplete.getPlaceFromIntent(data!!)
                fromLatLng = place.latLng
                val queriedLocation = place.latLng
                if (place.latLng !=null){
                    Toast.makeText(this,"HRLLO",Toast.LENGTH_SHORT).show()
                }

                Log.i("SearchActivity", "Place: " + place.getName() + ", " + place.getId())
                setFromP(place.name!!)


            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                val status = com.google.android.libraries.places.widget.Autocomplete.getStatusFromIntent(data!!)
                Log.i("SearchActivity", status.getStatusMessage())
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE_to) {
            if (resultCode == Activity.RESULT_OK) {
                val place = com.google.android.libraries.places.widget.Autocomplete.getPlaceFromIntent(data!!)
                toLatLng = place.latLng
                Log.i("SearchActivity", "Place: " + place.getName() + ", " + place.getId())
                setToP(place.name!!)


            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                val status = com.google.android.libraries.places.widget.Autocomplete.getStatusFromIntent(data!!)
                Log.i("SearchActivity", status.getStatusMessage())
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
    private fun permCheck(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_ACCESS_FINE_LOCATION)
            return
        }
    }


    @SuppressLint("MissingPermission")
    private fun addMarkersOnMap(){
        mMap.clear()
        if (fromLatLng != null){
        mMap.addMarker(MarkerOptions().position(fromLatLng!!).title("Origin Location"))
        mMap.addMarker(MarkerOptions().position(toLatLng!!).title("Destination Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(fromLatLng))
        }

        val LatLongB = LatLngBounds.Builder()
        val origin = fromLatLng!!
        val dest = toLatLng!!
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
                val parser: Parser = Parser()
                val stringBuilder: StringBuilder = StringBuilder(result)
                val json: JsonObject = parser.parse(stringBuilder) as JsonObject
                // get to the correct element in JsonObject
                val routes = json.array<JsonObject>("routes")
                val points = routes!!["legs"]["steps"][0] as JsonArray<JsonObject>
                // For every element in the JsonArray, decode the polyline string and pass all points to a List
                val polypts = points.flatMap { decodePoly(it.obj("polyline")?.string("points")!!)  }
                // Add  points to polyline and bounds
                options.add(sydney)
                LatLongB.include(sydney)
                for (point in polypts)  {
                    options.add(point)
                    LatLongB.include(point)
                }
                options.add(opera)
                LatLongB.include(opera)
                // build bounds
                val bounds = LatLongB.build()
                // add polyline to the map
                mMap!!.addPolyline(options)
                // show map with route centered
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            }


///end
            /*
        val FetchUrl = FetchUrl()

        // Start downloading json data from Google Directions API
        FetchUrl.execute(url)
        //move map camera
        //mMap!!.moveCamera(CameraUpdateFactory.newLatLng(origin))
       // mMap!!.animateCamera(CameraUpdateFactory.zoomTo(11f))
       */
    }
    }
    @SuppressLint("MissingPermission")
    private fun obtieneLocalizacion(){
        permCheck()
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                latitude = location?.latitude!! //todo ask user to turn location on
                longitude = location.longitude
            }
        Log.i("test", "Latitute: $latitude ; Longitute: $longitude")
        val sydney = LatLng(latitude, longitude)
     //   mMap.addMarker(MarkerOptions().position(sydney).title("Your Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private fun getUrl(from : LatLng, to : LatLng) : String {
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

            val p = LatLng(lat.toDouble() / 1E5,
                lng.toDouble() / 1E5)
            poly.add(p)
        }

        return poly
    }

}