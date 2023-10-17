package com.example.aviaryquest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aviaryquest.models.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var user: User

    private lateinit var radioGroupUnits: RadioGroup
    private lateinit var radioMetric: RadioButton
    private lateinit var radioImperial: RadioButton
    private lateinit var editMaxDistance: EditText
    private lateinit var btnSaveSettings: Button
    private lateinit var editCurrentPassword: EditText
    private lateinit var editNewPassword: EditText
    private lateinit var editDeleteAccountPassword: EditText
    private lateinit var btnChangePassword: Button
    private lateinit var btnDeleteAccount: Button
    private lateinit var btnLogout: Button
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize UI elements
        radioGroupUnits = findViewById(R.id.radioGroupUnits)
        radioMetric = findViewById(R.id.radioMetric)
        radioImperial = findViewById(R.id.radioImperial)
        editMaxDistance = findViewById(R.id.editMaxDistance)
        btnSaveSettings = findViewById(R.id.btnSaveSettings)
        editCurrentPassword = findViewById(R.id.editCurrentPassword)
        editNewPassword = findViewById(R.id.editNewPassword)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        editDeleteAccountPassword = findViewById(R.id.editDeleteAccountPassword)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)
        btnLogout = findViewById(R.id.btnLogout)
        bottomNavigation = findViewById(R.id.bottomNavigationView)

        // Load user settings from Firestore
        loadUserSettings()

        // Set click listeners for buttons
        btnSaveSettings.setOnClickListener { saveUserSettings() }
        btnChangePassword.setOnClickListener {
            // Implement password change functionality
            val currentPassword = editCurrentPassword.text.toString().trim()
            val newPassword = editNewPassword.text.toString().trim()

            // Perform password change logic using Firebase Auth
            val user = auth.currentUser
            val credential = EmailAuthProvider.getCredential(user!!.email!!, currentPassword)

            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Password changed successfully",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    startActivity(Intent(this, Login::class.java))
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Password could not be changed",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_LONG).show()
                    }
                }

            editNewPassword.text.clear()
        }
        btnDeleteAccount.setOnClickListener {
            // Implement account deletion functionality
            // Get the user's current password
            val currentPassword = editDeleteAccountPassword.text.toString().trim()

            // Check if the password is correct
            val user = auth.currentUser
            val credential = EmailAuthProvider.getCredential(user!!.email!!, currentPassword)

            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Password correct, deleting account",
                            Toast.LENGTH_LONG
                        ).show()

                        // Perform delete account logic using Firebase Auth
                        auth.currentUser?.delete()
                            ?.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_LONG)
                                        .show()
                                    startActivity(Intent(this, Login::class.java))
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Account could not be deleted",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }

                        // delete user from firestore
                        val userRef = db.collection("users").document(auth.currentUser!!.uid)
                        userRef.delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_LONG)
                                    .show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "Account could not be deleted",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        editNewPassword.text.clear()
                    } else {
                        Toast.makeText(this, "Password incorrect", Toast.LENGTH_LONG).show()
                    }
                }
        }

        btnLogout.setOnClickListener {
            // Perform logout logic using Firebase Auth
            auth.signOut()

            // Notify the user and start the login activity
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, Login::class.java))
        }

        // Set the bottom navigation view to be selected
        bottomNavigation.selectedItemId = R.id.nav_settings

        // force the bottom navigation bar to always show icon and title
        bottomNavigation.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED

        // Set click listener for bottom navigation
        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_Home -> {
                    startActivity(Intent(this, MapActivity::class.java))
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
                    //Do nothing, we're already in the settings activity
                    true
                }
                else -> false
            }
        }
    }

    private fun loadUserSettings() {
        // Implement code to load user settings from Firestore and populate UI elements
        val userRef = db.collection("users").document(auth.currentUser!!.uid)
        userRef.get()
            .addOnSuccessListener { documentSnapshot ->
                user = documentSnapshot.toObject(User::class.java)!!
                radioMetric.isChecked = user.isMetric
                radioImperial.isChecked = !user.isMetric
                editMaxDistance.setText(user.maxDistance.toString())
            }

    }

    private fun saveUserSettings() {
        // Save user settings to Firestore
        val isMetric = radioMetric.isChecked
        val maxDistance = editMaxDistance.text.toString().toDoubleOrNull() ?: 0.0

        user.isMetric = isMetric
        user.maxDistance = maxDistance

        // Implement code to update user settings in Firestore

        val userRef = db.collection("users").document(auth.currentUser!!.uid)
        userRef.set(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Settings could not be saved", Toast.LENGTH_LONG).show()
            }
    }
}
