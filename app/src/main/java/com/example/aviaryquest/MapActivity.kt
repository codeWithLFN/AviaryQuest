package com.example.aviaryquest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap
    private lateinit var bottomNavigation: BottomNavigationView
    private var userLocation: LatLng? = null
    private var userLocationMarker: Marker? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var firebaseAuth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private lateinit var db: FirebaseFirestore
    private val hotspots: MutableList<Pair<String, LatLng>> = mutableListOf()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser
        db = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Method to check location permission
        checkLocationPermission()

        // Initialize bottom navigation
        bottomNavigation = findViewById(R.id.bottomBar)

        // Set the bottom navigation view to be selected
        bottomNavigation.selectedItemId = R.id.nav_Home

        // force the bottom navigation bar to always show icon and title
        bottomNavigation.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED

        // Set bottom navigation to selected item
        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_Home -> {
                    // Handle Saved Observations menu item click
                    true
                }
                R.id.nav_observations -> {
                    startActivity(Intent(this, SaveObservation::class.java))
                    true
                }
                R.id.SavedBirds -> {
                    startActivity(Intent(this, SaveBirdsActivity::class.java))
                    true
                }

                R.id.nav_settings -> {
                    // Handle settings menu item click
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }

        // btn where user can select map type
        map.mapType = GoogleMap.MAP_TYPE_SATELLITE

        //zoom (+/-) on map
        map.uiSettings.isZoomControlsEnabled = true

        //zoom in on map
        map.uiSettings.isZoomGesturesEnabled = true

        //zoom to south africa
        val southAfrica = LatLng(-30.5595, 22.9375)
        map.moveCamera(CameraUpdateFactory.newLatLng(southAfrica))

        //Method to fetch bird hotspots from eBird API
        fetchBirdHotspots()

        //stores the marker location do ne by the user
        fetchObservations()

    }

    private fun fetchBirdHotspots() {
        val eBirdAPIKey = "ci1hchanvu0t"
        val url = "https://api.ebird.org/v2/data/obs/ZA/recent?hotspot=true&locale=en_US&fmt=json&key=$eBirdAPIKey"

        val request = Request.Builder()
            .url(url)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        // Handle unsuccessful response
                        return
                    }

                    val jsonData = response.body?.string()
                    val jsonArray = JSONArray(jsonData)

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)

                        if (jsonObject.has("lat") && jsonObject.has("lng")) {
                            val lat = jsonObject.getDouble("lat")
                            val lng = jsonObject.getDouble("lng")

                            val hotspotName = if (jsonObject.has("locName")) jsonObject.getString("locName") else ""

                            hotspots.add(Pair(hotspotName, LatLng(lat, lng)))

                            runOnUiThread {
                                val hotspotLocation = LatLng(lat, lng)
                                map.addMarker(MarkerOptions().position(hotspotLocation).title(hotspotName))
                            }
                        }
                    }
                }
            }
        })
        //
        map.setOnMarkerClickListener(this)
    }

    override fun onMarkerClick(marker: com.google.android.gms.maps.model.Marker): Boolean {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${marker.position.latitude},${marker.position.longitude}")
        )
        startActivity(intent)
        return true
    }

    //Method to fetch max distance from Firestore
    private fun fetchMaxDistance(): Double {
        val user = firebaseAuth.currentUser
        val userUid = user?.uid
        var maxDistance = 0.0

        if (userUid != null) {
            db.collection("users")
                .document(userUid)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    // Retrieve the maxDistance field from the user's document
                    maxDistance = documentSnapshot.getDouble("maxDistance") ?: 0.0
                }
                .addOnFailureListener { exception ->
                    // Handle any errors in fetching data from Firestore
                }
        }

        return maxDistance
    }

    //Fetch observations location from Firestore
    private fun fetchObservations() {
        val user = firebaseAuth.currentUser
        val userUid = user?.uid

        if (userUid != null) {
            db.collection("users")
                .document(userUid)
                .collection("observations")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val locationString = document.getString("location")
                        val latLong = locationString?.split(",") // Split string into latitude and longitude

                        if (latLong?.size == 2) {
                            val latitude = latLong[0].toDouble() // Extract latitude
                            val longitude = latLong[1].toDouble() // Extract longitude

                            val observationLocation = LatLng(latitude, longitude)
                            val markerOptions = MarkerOptions().position(observationLocation)

                            // marker title and color
                            markerOptions.title("Observation").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

                            map.addMarker(markerOptions)


                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle any errors in fetching data from Firestore
                }
        }
        // Add this line to enable marker click event
        map.setOnMarkerClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            // Permission has already been granted

        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onMapReady(map)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

}
