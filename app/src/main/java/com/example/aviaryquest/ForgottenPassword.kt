package com.example.aviaryquest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.aviaryquest.models.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ForgottenPassword : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var btnChangePassword: Button
    private lateinit var Back : ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgotten_password)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        Back = findViewById(R.id.img_forgotten_password)
        etUsername = findViewById(R.id.ProfileUsername)
        etCurrentPassword = findViewById(R.id.ProfileCurrentPassword)
        etNewPassword = findViewById(R.id.ProfileNewPassword)
        btnChangePassword = findViewById(R.id.btnChangePassword)

        // Pressing this image view will take you back to the main page
        Back.setOnClickListener{
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        btnChangePassword.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val currentPassword = etCurrentPassword.text.toString().trim()
            val newPassword = etNewPassword.text.toString().trim()

            // Check if the username exists and matches the current password
            firestore.collection("users").document(username).get().addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                if (user != null && user.password == currentPassword) {
                    // Perform change password logic using Firebase Auth
                    val email = username // Change to your domain
                    val userAuth = auth.currentUser
                    if (userAuth != null) {
                        val credential = EmailAuthProvider.getCredential(email, currentPassword)
                        userAuth.reauthenticate(credential).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                userAuth.updatePassword(newPassword).addOnCompleteListener {task ->
                                    if (task.isSuccessful) {
                                        // Update the password in Firestore
                                        updatePasswordInFirestore(username, newPassword)
                                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(this, "Error updating password", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                Toast.makeText(this, "Authentication failed", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Please log in first", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Invalid username or password", Toast.LENGTH_LONG).show()
                }
            }
            etUsername.text.clear()
            etCurrentPassword.text.clear()
            etNewPassword.text.clear()
        }
    }

    private fun updatePasswordInFirestore(username: String, newPassword: String) {
        // Update the user's password in Firestore
        firestore.collection("users").document(username)
            .update("password", newPassword)
            .addOnSuccessListener {
                // Password updated in Firestore
            }
            .addOnFailureListener { e ->
                // Handle any errors
            }
    }
}
