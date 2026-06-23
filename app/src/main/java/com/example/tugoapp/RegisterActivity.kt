package com.example.tugoapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var userTypeRadioGroup: RadioGroup
    private lateinit var registerButton: Button
    private lateinit var goToLoginTextView: TextView
    private lateinit var errorTextView: TextView
    private lateinit var progressBar: ProgressBar

    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        bindWidgets()
        wireUpEvents()
    }

    private fun bindWidgets() {
        firstNameEditText = findViewById(R.id.register_first_name_edittext)
        lastNameEditText = findViewById(R.id.register_last_name_edittext)
        emailEditText = findViewById(R.id.register_email_edittext)
        passwordEditText = findViewById(R.id.register_password_edittext)
        userTypeRadioGroup = findViewById(R.id.register_user_type_radiogroup)
        registerButton = findViewById(R.id.register_button)
        goToLoginTextView = findViewById(R.id.go_to_login_textview)

        errorTextView = findViewById(R.id.register_error_textview)
        progressBar = findViewById(R.id.register_progress_bar)
    }

    private fun wireUpEvents() {
        registerButton.setOnClickListener {
            registerUser()
        }

        goToLoginTextView.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val checkedRadioButtonId = userTypeRadioGroup.checkedRadioButtonId

        errorTextView.visibility = View.GONE

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("All fields required")
            return
        }

        if (password.length < 6) {
            showError("Password must be at least 6 characters")
            return
        }

        if (checkedRadioButtonId == -1) {
            showError("Select user type")
            return
        }

        val userType = findViewById<RadioButton>(checkedRadioButtonId)
            .text.toString()
            .lowercase()

        setLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->

                val uid = result.user?.uid
                if (uid == null) {
                    setLoading(false)
                    showError("Failed to create user")
                    return@addOnSuccessListener
                }

                val profileData = hashMapOf(
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "email" to email,
                    "userType" to userType,
                    "createdAt" to FieldValue.serverTimestamp()
                )

                db.collection("users")
                    .document(uid)
                    .set(profileData)
                    .addOnSuccessListener {
                        setLoading(false)

                        Toast.makeText(
                            this,
                            "Account created",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        setLoading(false)
                        showError("Profile save failed")
                    }
            }
            .addOnFailureListener {
                setLoading(false)
                showError("Registration failed")
            }
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        registerButton.isEnabled = !isLoading
    }

    private fun showError(msg: String) {
        errorTextView.text = msg
        errorTextView.visibility = View.VISIBLE
    }
}