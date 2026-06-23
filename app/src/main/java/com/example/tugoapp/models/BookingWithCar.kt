package com.example.tugoapp.models


/**
 * Helper data class that combines a Booking and its corresponding Car.
 *
 * Used for displaying information in OwnerBookingsAdapter and RenterBookingsAdapter.
 * Contains references to the booking, the car, and indirectly the owner and renter IDs.
 *
 * Not stored in Firestore, it is constructed in the app after fetching Booking and Car documents.
 */

data class BookingWithCar(
    val booking: Booking,
    val car: Car,
    val user: User? = null
)