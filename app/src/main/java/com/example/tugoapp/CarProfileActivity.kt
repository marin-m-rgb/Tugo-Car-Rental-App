package com.example.tugoapp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.tugoapp.models.Car
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CarProfileActivity : AppCompatActivity() {

    private lateinit var renterCarPlateTextView: TextView
    private lateinit var renterCarProfilePhotoImageView: ImageView
    private lateinit var renterCarBrandModelTextView: TextView
    private lateinit var renterCarColorTextView: TextView
    private lateinit var renterCarPricePerDayTextView: TextView
    private lateinit var ownerCarNameTextView: TextView
    private lateinit var renterTotalCarPriceTextView: TextView
    private lateinit var renterStartDateView: TextView
    private lateinit var renterEndDateView: TextView
    private lateinit var renterBookNowButton: Button

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private var car: Car? = null
    private var start: String? = null
    private var end: String? = null

    private val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_profile)

        bind()
        wire()
        loadCar()
    }

    private fun bind() {
        renterCarPlateTextView = findViewById(R.id.renter_car_plate_textview)
        renterCarProfilePhotoImageView = findViewById(R.id.renter_car_profile_photo_imageview)
        renterCarBrandModelTextView = findViewById(R.id.renter_car_brand_model_textview)
        renterCarColorTextView = findViewById(R.id.renter_car_color_textview)
        renterCarPricePerDayTextView = findViewById(R.id.renter_car_price_per_day_textview)
        ownerCarNameTextView = findViewById(R.id.owner_car_name_textview)
        renterTotalCarPriceTextView = findViewById(R.id.renter_total_car_price_textview)
        renterStartDateView = findViewById(R.id.renter_start_date_edittext)
        renterEndDateView = findViewById(R.id.renter_end_date_edittext)
        renterBookNowButton = findViewById(R.id.renter_book_now_button)
    }

    private fun wire() {
        renterStartDateView.setOnClickListener {
            pickDate { start = it; renterStartDateView.text = it }
        }

        renterEndDateView.setOnClickListener {
            pickDate { end = it; renterEndDateView.text = it }
        }

        renterBookNowButton.setOnClickListener { book() }
    }

    private fun loadCar() {

        val carId = intent.getStringExtra("carId")

        if (carId.isNullOrEmpty()) {
            Toast.makeText(this, "Missing car ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db.collection("cars")
            .document(carId)
            .get()
            .addOnSuccessListener { doc ->

                if (!doc.exists()) {
                    Toast.makeText(this, "Car not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                car = Car(
                    id = doc.id,
                    ownerId = doc.getString("ownerId") ?: "",
                    brand = doc.getString("brand") ?: "",
                    model = doc.getString("model") ?: "",
                    color = doc.getString("color") ?: "",
                    licensePlate = doc.getString("licensePlate") ?: "",
                    city = doc.getString("city") ?: "",
                    address = doc.getString("address") ?: "",
                    pricePerDay = doc.getDouble("pricePerDay") ?: 0.0,
                    photoUrl = doc.getString("photoUrl") ?: "",
                    latitude = doc.getDouble("latitude") ?: 0.0,
                    longitude = doc.getDouble("longitude") ?: 0.0
                )

                bindCar(car!!)
                loadOwner(car!!.ownerId)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load car", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadOwner(ownerId: String) {

        db.collection("users")
            .document(ownerId)
            .get()
            .addOnSuccessListener { doc ->
                val name =
                    "${doc.getString("firstName") ?: ""} ${doc.getString("lastName") ?: ""}"
                ownerCarNameTextView.text = "Owner: $name"
            }
    }

    private fun bindCar(c: Car) {
        renterCarPlateTextView.text = c.licensePlate
        renterCarBrandModelTextView.text = "${c.brand} ${c.model}"
        renterCarColorTextView.text = c.color
        renterCarPricePerDayTextView.text = "${c.pricePerDay}/day"

        Glide.with(this)
            .load(c.photoUrl)
            .into(renterCarProfilePhotoImageView)
    }

    private fun pickDate(on: (String) -> Unit) {
        val cal = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, y, m, d ->
                on(String.format("%04d-%02d-%02d", y, m + 1, d))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun book() {

        val c = car
        if (c == null) {
            Toast.makeText(this, "Car not loaded", Toast.LENGTH_SHORT).show()
            return
        }

        if (start == null || end == null) {
            Toast.makeText(this, "Select start and end dates", Toast.LENGTH_SHORT).show()
            return
        }

        val sd = try {
            format.parse(start!!)
        } catch (e: Exception) {
            null
        }

        val ed = try {
            format.parse(end!!)
        } catch (e: Exception) {
            null
        }

        if (sd == null || ed == null) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show()
            return
        }

        val renterId = auth.currentUser?.uid
        if (renterId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("bookings")
            .whereEqualTo("carId", c.id)
            .get()
            .addOnSuccessListener { result ->

                val conflict = result.documents.any { doc ->

                    val existingStart =
                        doc.getTimestamp("startDate")?.toDate() ?: return@any false
                    val existingEnd =
                        doc.getTimestamp("endDate")?.toDate() ?: return@any false

                    !(ed.before(existingStart) || sd.after(existingEnd))
                }

                if (conflict) {
                    Toast.makeText(
                        this,
                        "This car is already booked for these dates",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                val booking = hashMapOf(
                    "carId" to c.id,
                    "ownerId" to c.ownerId,
                    "renterId" to renterId,
                    "startDate" to Timestamp(sd),
                    "endDate" to Timestamp(ed)
                )

                db.collection("bookings")
                    .add(booking)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Booking confirmed", Toast.LENGTH_SHORT).show()

                        startActivity(
                            Intent(this, MyBookingsActivity::class.java)
                        )

                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Booking failed: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error checking availability: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}