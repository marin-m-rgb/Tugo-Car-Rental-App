package com.example.tugoapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tugoapp.adapters.OwnerBookingsAdapter
import com.example.tugoapp.models.Booking
import com.example.tugoapp.models.BookingWithCar
import com.example.tugoapp.models.Car
import com.example.tugoapp.models.User
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class ManageBookingsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OwnerBookingsAdapter

    private val bookings = mutableListOf<BookingWithCar>()

    private val db = Firebase.firestore
    private val auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_bookings)

        bind()
        setup()
        load()
    }

    private fun bind() {
        recyclerView = findViewById(R.id.owner_bookings_recyclerview)

        adapter = OwnerBookingsAdapter(bookings) { bookingId ->
            deleteBooking(bookingId)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setup() {}

    private fun load() {
        val ownerId = auth.currentUser?.uid ?: return

        db.collection("bookings")
            .whereEqualTo("ownerId", ownerId)
            .get()
            .addOnSuccessListener { result ->

                bookings.clear()

                for (doc in result) {

                    val booking = Booking(
                        id = doc.id,
                        carId = doc.getString("carId") ?: "",
                        ownerId = doc.getString("ownerId") ?: "",
                        renterId = doc.getString("renterId") ?: "",
                        startDate = doc.getTimestamp("startDate") ?: Timestamp.now(),
                        endDate = doc.getTimestamp("endDate") ?: Timestamp.now(),
                        confirmationCode = doc.getString("confirmationCode") ?: ""
                    )

                    db.collection("cars").document(booking.carId).get()
                        .addOnSuccessListener { carDoc ->

                            val car = Car(
                                id = carDoc.id,
                                ownerId = carDoc.getString("ownerId") ?: "",
                                brand = carDoc.getString("brand") ?: "",
                                model = carDoc.getString("model") ?: "",
                                color = carDoc.getString("color") ?: "",
                                licensePlate = carDoc.getString("licensePlate") ?: "",
                                city = carDoc.getString("city") ?: "",
                                address = carDoc.getString("address") ?: "",
                                pricePerDay = carDoc.getDouble("pricePerDay") ?: 0.0,
                                photoUrl = carDoc.getString("photoUrl") ?: "",
                                latitude = carDoc.getDouble("latitude") ?: 0.0,
                                longitude = carDoc.getDouble("longitude") ?: 0.0
                            )

                            db.collection("users").document(booking.renterId).get()
                                .addOnSuccessListener { userDoc ->

                                    val user = User(
                                        id = userDoc.id,
                                        firstName = userDoc.getString("firstName") ?: "",
                                        lastName = userDoc.getString("lastName") ?: "",
                                        email = userDoc.getString("email") ?: "",
                                        userType = userDoc.getString("userType") ?: ""
                                    )

                                    bookings.add(BookingWithCar(booking, car, user))
                                    adapter.notifyDataSetChanged()
                                }
                        }
                }
            }
    }

    private fun deleteBooking(id: String) {
        db.collection("bookings").document(id)
            .delete()
            .addOnSuccessListener {
                load()
            }
    }
}