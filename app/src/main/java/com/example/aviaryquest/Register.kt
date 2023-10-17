package com.example.aviaryquest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Register : AppCompatActivity() {

    private lateinit var Email: EditText
    private lateinit var Password: EditText
    private lateinit var SignUp: Button
    private lateinit var Back: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //Intialize firebase authetication and firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        //Assigning the values
        Email = findViewById(R.id.edt_reg_email)
        Password = findViewById(R.id.edt_reg_pass)
        SignUp = findViewById(R.id.btn_Register)
        Back = findViewById(R.id.img_sign_up)

        //Pressing this image view it will take you back to the main page
        Back.setOnClickListener{
            val intent = Intent(this,Login::class.java)
            startActivity(intent)
        }

        //Signing Up users and storing details within the database
        SignUp.setOnClickListener {
            val username = Email.text.toString()
            val password = Password.text.toString()

            if (TextUtils.isEmpty(username)  || TextUtils.isEmpty(password) ) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val message =
                                "Registration successful: Username=$username, Password=$password"
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                            val currentUser = auth.currentUser
                            val user = hashMapOf(
                                "username" to username,
                                "password" to password
                            )

                            if (currentUser != null) {
                                db.collection("users").document(currentUser.uid)
                                    .set(user)
                                    .addOnSuccessListener {
                                        val intent = Intent(this, Login::class.java)
                                        startActivity(intent)
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            this,
                                            "Failed to add user to Firestore",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        } else {
                            val exception = task.exception
                            Toast.makeText(this, exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}