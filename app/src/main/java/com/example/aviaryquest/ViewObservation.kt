package com.example.aviaryquest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aviaryquest.adapters.BirdObservationsAdapter
import com.example.aviaryquest.databinding.ActivityViewObservationBinding
import com.example.aviaryquest.models.SaveBird
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class ViewObservation : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivityViewObservationBinding
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var recyclerView: RecyclerView
    private lateinit var BirdObservationsAdapter: BirdObservationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewObservationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Authentication and Firestore reference

        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser ?: return // Check if the user is authenticated
        firestore = FirebaseFirestore.getInstance()

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.observationRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        BirdObservationsAdapter = BirdObservationsAdapter()
        recyclerView.adapter = BirdObservationsAdapter

        // Display observations from Firestore
        displayObservations()


        // Initialize the bottom navigation view
        bottomNavigation = binding.bottomObservation

        // Set the bottom navigation view to be selected
        bottomNavigation.selectedItemId = R.id.nav_observations

        // Set click listener for bottom navigation
        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_Home -> {
                    startActivity(Intent(this, MapActivity::class.java))
                    true
                }

                R.id.nav_observations -> {
                    // You're already on this page, no need to do anything here
                    true
                }

                R.id.SavedBirds -> {
                    startActivity(Intent(this, SaveBirdsActivity::class.java))
                    true
                }

                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }

    private fun displayObservations() {
        // Get the user's observations from Firestore
        firestore.collection("users").document(currentUser.uid).collection("observations")
            .get()
            .addOnCompleteListener { documents ->
                val observations = mutableListOf<SaveBird>()
                for (document in documents.result!!) {
                    val species = document.getString("species") ?: ""
                    val location = document.getString("location") ?: ""
                    val date = document.getDate("date")
                    val time = document.getString("time") ?: ""
                    val notes = document.getString("notes") ?: ""
                    val observation = SaveBird(species, location, date, time, notes)
                    observations.add(observation)
                }
                BirdObservationsAdapter.submitList(observations)
            }
            .addOnFailureListener { e ->
                // Log the error
                println("Error getting documents: $e")
            }
    }
}
