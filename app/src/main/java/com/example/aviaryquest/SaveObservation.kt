package com.example.aviaryquest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.aviaryquest.models.SaveBird
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class SaveObservation : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Location? = null

    private lateinit var etSpecies: EditText
    private lateinit var etLocation: EditText
    private lateinit var etNotes: EditText
    private lateinit var datePicker: CalendarView
    private lateinit var timePicker: TimePicker
    private lateinit var btnSaveObservation: Button
    private lateinit var btnViewObservation: Button
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var firebaseAuth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private lateinit var db: FirebaseFirestore

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_observation)

        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser

        db = FirebaseFirestore.getInstance()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        etSpecies = findViewById(R.id.etSpecies)
        etLocation = findViewById(R.id.etLocation)
        etNotes = findViewById(R.id.etNotes)
        datePicker = findViewById(R.id.datePicker)
        timePicker = findViewById(R.id.timePicker)
        btnSaveObservation = findViewById(R.id.btn_Observation_Save)
        btnViewObservation = findViewById(R.id.btn_view_observation)
        bottomNavigation = findViewById(R.id.bottom_save_observation)


        if (checkLocationPermission()) {
            fetchLastLocation()
        }

        btnSaveObservation.setOnClickListener {
            saveObservationToFirestore()
        }

        btnViewObservation.setOnClickListener {
            startActivity(Intent(this, ViewObservation::class.java))
        }

        // Set the bottom navigation view to be selected
        bottomNavigation.selectedItemId = R.id.nav_observations

        // force the bottom navigation bar to always show icon and title
        bottomNavigation.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED

        // Set bottom navigation to selected item
        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_Home -> {
                    startActivity(Intent(this, MapActivity::class.java))
                    true
                }
                R.id.nav_observations -> {
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

    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return false
        }
        return true
    }


    private fun fetchLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Consider calling
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                currentLocation = it
                val latitude = it.latitude
                val longitude = it.longitude
                etLocation.setText("$latitude, $longitude")
            }
        }
    }

    private fun saveObservationToFirestore() {
        val species = etSpecies.text.toString()
        val location = etLocation.text.toString()
        val date = Date(datePicker.date)
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val notes = etNotes.text.toString()

        val user = firebaseAuth.currentUser

        if (user != null) {
            if (species.isNotEmpty() && location.isNotEmpty()) {
                val observation = SaveBird(species, location, date, time, notes)

                // Get the user's unique ID
                val userId = user.uid

                db.collection("users").document(userId).collection("observations")
                    .add(observation)
                    .addOnSuccessListener { documentReference ->
                        Toast.makeText(
                            this,
                            "Observation successfully saved!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error adding observation: $e", Toast.LENGTH_SHORT)
                            .show()
                    }
            } else {
                Toast.makeText(
                    this,
                    "Species and Location fields are required.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
