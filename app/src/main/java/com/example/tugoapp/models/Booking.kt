package com.example.tugoapp.models

import com.google.firebase.Timestamp

/**
 * Data class that represents a single booking of a car by a renter.
 * Includes references to the car, the owner, and the renter,
 * as well as the rental period and a confirmation code.
 * Stored in Firestore under "bookings".
 */

data class Booking(
    val id: String,
    val carId: String,
    val ownerId: String,
    val renterId: String,
    val startDate: Timestamp,
    val endDate:Timestamp,
    val confirmationCode: String
)