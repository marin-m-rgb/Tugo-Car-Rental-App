package com.example.tugoapp

import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tugoapp.models.UserSession
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.util.Locale

class CreateListingActivity : AppCompatActivity() {

    private lateinit var ownerCarListingTitleTextView: TextView
    private lateinit var ownerCarBrandEditText: EditText
    private lateinit var ownerCarModelEditText: EditText
    private lateinit var ownerCarColorEditText: EditText
    private lateinit var ownerCarLicensePlateEditText: EditText
    private lateinit var ownerCarPricePerDayEditText: EditText
    private lateinit var ownerCarCityEditText: EditText
    private lateinit var ownerCarAddressEditText: EditText
    private lateinit var ownerCarPhotoUrlEditText: EditText
    private lateinit var ownerCreateButton: Button

    private val db = Firebase.firestore
    private val auth: FirebaseAuth = Firebase.auth

    private var carId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_listing)

        if (!allowOnlyOwners()) return

        bindWidgets()
        readIntentData()
        wireUpEvents()
    }

    private fun bindWidgets() {
        ownerCarListingTitleTextView = findViewById(R.id.owner_car_listing_title_textview)
        ownerCarBrandEditText = findViewById(R.id.owner_car_brand_edittext)
        ownerCarModelEditText = findViewById(R.id.owner_car_model_edittext)
        ownerCarColorEditText = findViewById(R.id.owner_car_color_edittext)
        ownerCarLicensePlateEditText = findViewById(R.id.owner_car__license_plate_edittext)
        ownerCarPricePerDayEditText = findViewById(R.id.owner_car_price_per_day_edittext)
        ownerCarCityEditText = findViewById(R.id.owner_car_city_edittext)
        ownerCarAddressEditText = findViewById(R.id.owner_car_address_edittext)
        ownerCarPhotoUrlEditText = findViewById(R.id.owner_car_photo_url_edittext)
        ownerCreateButton = findViewById(R.id.owner_create_button)
    }

    private fun wireUpEvents() {
        ownerCreateButton.setOnClickListener {
            saveCarListing()
        }
    }

    private fun allowOnlyOwners(): Boolean {
        if (!UserSession.isLoggedIn() || !UserSession.isOwner()) {
            finish()
            return false
        }
        return true
    }

    private fun readIntentData() {
        carId = intent.getStringExtra("carId")

        if (carId == null) {
            ownerCarListingTitleTextView.text = "Add Car Listing"
        } else {
            ownerCarListingTitleTextView.text = "Edit Car Listing"
            loadCarListingForEditing(carId!!)
        }
    }

    private fun loadCarListingForEditing(carId: String) {
        db.collection("cars")
            .document(carId)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    Toast.makeText(this, "Car not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                ownerCarBrandEditText.setText(document.getString("brand"))
                ownerCarModelEditText.setText(document.getString("model"))
                ownerCarColorEditText.setText(document.getString("color"))
                ownerCarLicensePlateEditText.setText(document.getString("licensePlate"))
                ownerCarPricePerDayEditText.setText(document.getDouble("pricePerDay")?.toString())
                ownerCarCityEditText.setText(document.getString("city"))
                ownerCarAddressEditText.setText(document.getString("address"))
                ownerCarPhotoUrlEditText.setText(document.getString("photoUrl"))
            }
            .addOnFailureListener {
                Toast.makeText(this, "Load failed", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun getLatLngFromAddress(address: String): LatLng? {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val results = geocoder.getFromLocationName(address, 1)

            if (!results.isNullOrEmpty()) {
                LatLng(results[0].latitude, results[0].longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun saveCarListing() {

        val brand = ownerCarBrandEditText.text.toString().trim()
        val model = ownerCarModelEditText.text.toString().trim()
        val color = ownerCarColorEditText.text.toString().trim()
        val licensePlate = ownerCarLicensePlateEditText.text.toString().trim()
        val pricePerDay = ownerCarPricePerDayEditText.text.toString().toDoubleOrNull()
        val city = ownerCarCityEditText.text.toString().trim()
        val address = ownerCarAddressEditText.text.toString().trim()
        val photoUrl = ownerCarPhotoUrlEditText.text.toString().trim()

        if (brand.isEmpty() || model.isEmpty() || licensePlate.isEmpty() ||
            pricePerDay == null || city.isEmpty() || address.isEmpty() || photoUrl.isEmpty()
        ) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val latLng = getLatLngFromAddress(address)

        if (latLng == null) {
            Toast.makeText(
                this,
                "Invalid address. Cannot locate car on map.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val carData = hashMapOf(
            "ownerId" to (auth.currentUser?.uid ?: ""),
            "brand" to brand,
            "model" to model,
            "color" to color,
            "licensePlate" to licensePlate,
            "city" to city,
            "address" to address,
            "pricePerDay" to pricePerDay,
            "photoUrl" to photoUrl,
            "latitude" to latLng.latitude,
            "longitude" to latLng.longitude
        )

        if (carId == null) {
            db.collection("cars")
                .add(carData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Car created", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Create failed", Toast.LENGTH_SHORT).show()
                }
        } else {
            db.collection("cars")
                .document(carId!!)
                .set(carData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Car updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                }
        }
    }
}