package com.example.tugoapp.models

/**
 * Data class that represents a car available for rental.
 * This includes all information the owner provides about their car.
 * Stored in Firestore under "carListings" and displayed to renters.
 */

data class Car(
    val id: String,
    val ownerId: String,
    val brand: String,
    val model: String,
    val color: String,
    val licensePlate: String,
    val city: String,
    val address: String,
    val pricePerDay: Double,
    val photoUrl: String,
    val latitude: Double,
    val longitude: Double
)
