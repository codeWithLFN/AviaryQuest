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
        btnChangePassword = findViewById(R.id.btnChangePassword)

        // Pressing this image view will take you back to the main page
        Back.setOnClickListener{
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        btnChangePassword.setOnClickListener {
            val username = etUsername.text.toString().trim()

            if (username.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Send password reset email using Firebase
            auth.sendPasswordResetEmail(username)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Password reset email sent to $username",
                            Toast.LENGTH_LONG
                        ).show()
                        // Navigate to login or wherever appropriate
                        val intent = Intent(this, Login::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this,
                            "Failed to send password reset email. Check your email address.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
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
