package com.example.tugoapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tugoapp.models.UserSession
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var goToSignupTextView: TextView
    private lateinit var failLoginMessage: TextView

    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindWidgets()
        wireUpEvents()
    }

    private fun bindWidgets() {
        emailEditText = findViewById(R.id.login_email_edittext)
        passwordEditText = findViewById(R.id.login_password_edittext)
        loginButton = findViewById(R.id.login_button)
        goToSignupTextView = findViewById(R.id.go_to_register_textview)
        failLoginMessage = findViewById(R.id.fail_login_message_textview)
    }

    private fun wireUpEvents() {
        loginButton.setOnClickListener { loginUser() }

        goToSignupTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showError("Enter email and password")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->

                val uid = result.user?.uid
                if (uid == null) {
                    showError("Invalid session")
                    return@addOnSuccessListener
                }

                db.collection("users").document(uid).get()
                    .addOnSuccessListener { doc ->

                        if (!doc.exists()) {
                            showError("User profile missing")
                            return@addOnSuccessListener
                        }

                        UserSession.uid = uid
                        UserSession.firstName = doc.getString("firstName")
                        UserSession.lastName = doc.getString("lastName")
                        UserSession.email = doc.getString("email")
                        UserSession.userType = doc.getString("userType")

                        val intent = if (UserSession.isOwner()) {
                            Intent(this, MyListingsActivity::class.java)
                        } else {
                            Intent(this, SearchActivity::class.java)
                        }

                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener {
                        showError("Failed loading profile")
                    }

            }
            .addOnFailureListener {
                showError("Invalid credentials")
            }
    }

    private fun showError(msg: String) {
        failLoginMessage.text = msg
        failLoginMessage.visibility = View.VISIBLE
    }
}