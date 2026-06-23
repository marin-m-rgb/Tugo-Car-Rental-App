package com.example.tugoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tugoapp.adapters.MyListingsAdapter
import com.example.tugoapp.models.Car
import com.example.tugoapp.models.UserSession
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class MyListingsActivity : AppCompatActivity() {

    private lateinit var ownerCreateListingButton: Button
    private lateinit var ownerManageBookingsButton: Button
    private lateinit var ownerLogoutButton: Button
    private lateinit var ownerCarListingRecyclerView: RecyclerView


    private lateinit var myListingsAdapter: MyListingsAdapter


    private val cars = mutableListOf<Car>()


    private val db = Firebase.firestore


    private val auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_listings)

        if (!allowOnlyOwners()) return
        bindWidgets()
        setUpRecyclerView()
        wireUpEvents()
        loadCarsFromFirestore()
    }

    private fun bindWidgets() {
        ownerCarListingRecyclerView =  findViewById(R.id.owner_car_listing_recyclerview)
        ownerCreateListingButton = findViewById(R.id.owner_create_listing_button)
        ownerLogoutButton = findViewById(R.id.owner_logout_button)
        ownerManageBookingsButton = findViewById(R.id.owner_manage_bookings_button)
    }

    private fun setUpRecyclerView() {
        myListingsAdapter = MyListingsAdapter(
            cars,
           { car ->
                val intent = Intent(this, CreateListingActivity::class.java)
                intent.putExtra("carId", car.id)
                startActivity(intent)
            }
        )
        ownerCarListingRecyclerView.layoutManager = LinearLayoutManager(this)
        ownerCarListingRecyclerView.adapter = myListingsAdapter

    }

    private fun allowOnlyOwners(): Boolean {
        if (!UserSession.isLoggedIn() || !UserSession.isOwner()) {
            finish()
            return false
        }
        return true
    }

    private fun wireUpEvents() {
        ownerCreateListingButton.setOnClickListener {
            val intent = Intent(this, CreateListingActivity::class.java)
            startActivity(intent)
        }

        ownerManageBookingsButton.setOnClickListener {
            val intent = Intent(this, ManageBookingsActivity::class.java)
            startActivity(intent)
        }

        ownerLogoutButton.setOnClickListener {
            logoutUser()
        }
    }

    private fun loadCarsFromFirestore() {
        db.collection("cars")
            .whereEqualTo("ownerId", UserSession.uid)
            .get()
            .addOnSuccessListener { result ->

                cars.clear()

                for (document in result) {
                    val car = Car(
                        id = document.id,
                        ownerId = document.getString("ownerId") ?: "",
                        brand = document.getString("brand") ?: "",
                        model = document.getString("model") ?: "",
                        color = document.getString("color") ?: "",
                        licensePlate = document.getString("licensePlate") ?: "",
                        city = document.getString("city") ?: "",
                        address = document.getString("address") ?: "",
                        pricePerDay = (document.getDouble("pricePerDay") ?: 0.0),
                        photoUrl = document.getString("photoUrl") ?: "",
                        latitude = document.getDouble("latitude") ?: 0.0,
                        longitude = document.getDouble("longitude") ?: 0.0
                    )

                    cars.add(car)
                }

                myListingsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("MyListings", "Error loading cars: ", exception)
            }

    }

    private fun logoutUser(){
        auth.signOut()
        UserSession.resetSession()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadCarsFromFirestore()
    }
}