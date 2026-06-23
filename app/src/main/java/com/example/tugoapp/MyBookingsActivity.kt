package com.example.tugoapp

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tugoapp.adapters.RenterBookingsAdapter
import com.example.tugoapp.models.Booking
import com.example.tugoapp.models.BookingWithCar
import com.example.tugoapp.models.Car
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class MyBookingsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var adapter: RenterBookingsAdapter

    private val bookings = mutableListOf<BookingWithCar>()

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        bind()
        setup()
        load()
    }

    private fun bind() {
        recyclerView = findViewById(R.id.renter_bookings_recyclerview)
        emptyText = findViewById(R.id.renter_empty_bookings_textview)

        adapter = RenterBookingsAdapter(bookings) { id ->
            delete(id)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setup() {}

    private fun load() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("bookings")
            .whereEqualTo("renterId", uid)
            .get()
            .addOnSuccessListener { result ->

                bookings.clear()

                emptyText.visibility =
                    if (result.isEmpty) View.VISIBLE else View.GONE

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

                            bookings.add(BookingWithCar(booking, car))
                            adapter.notifyDataSetChanged()
                        }
                }
            }
    }

    private fun delete(id: String) {
        db.collection("bookings").document(id)
            .delete()
            .addOnSuccessListener { load() }
    }
}