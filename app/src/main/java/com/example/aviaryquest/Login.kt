package com.example.aviaryquest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.text.TextUtils
import android.widget.TextView
import android.widget.Toast
import com.example.aviaryquest.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {
    private lateinit var Email: EditText
    private lateinit var Password: EditText
    private lateinit var SignUp: TextView
    private lateinit var SignIn: Button
    private lateinit var Forgot:TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //Intialize firebase authetication and firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        //Assigning the values
        Email = findViewById(R.id.edt_email_login)
        Password = findViewById(R.id.edt_password_login)
        SignUp = findViewById(R.id.txtSignUp)
        SignIn = findViewById(R.id.btn_sign_in)
        Forgot = findViewById(R.id.txtForgetPassword)

        //User can click the Forgot Password and will enable the user to edit their password
        Forgot.setOnClickListener {
            val intent = Intent(this, ForgottenPassword::class.java)
            startActivity(intent)
        }

        //Registering the user so they're details are stored in the database
        SignUp.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
            Toast.makeText(this, "Signing Up User", Toast.LENGTH_LONG).show()
        }

        //Signing in when the authentication is valid
        SignIn.setOnClickListener {
            val usernameText = Email.text.toString().trim()
            val passwordText = Password.text.toString().trim()

            if (!TextUtils.isEmpty(usernameText) && !TextUtils.isEmpty(passwordText)) {
                db.collection("users").whereEqualTo("username", usernameText).get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val userData = documents.documents[0].toObject(User::class.java)
                            if (userData != null && userData.password == passwordText) {
                                auth.signInWithEmailAndPassword(usernameText, passwordText)
                                    .addOnCompleteListener(this) { task ->
                                        if (task.isSuccessful) {
                                            val message = "Login successful: Username=$usernameText"
                                            val intent = Intent(this, MapActivity::class.java)
                                            startActivity(intent)
                                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                                        } else {
                                            val message = "Login failed: ${task.exception?.message}"
                                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                val message = "Incorrect username or password"
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val message = "User not found"
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { exception ->
                        val message = "Error: ${exception.message}"
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}