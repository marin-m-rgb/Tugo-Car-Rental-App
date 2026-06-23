package com.example.tugoapp.models

/**
 * Data class that represents a user within the app.
 * Stores basic profile information and indicates the role the user holds.
 * Saved in Firestore under "users" and used across the app for authentication
 * and user-specific features.
 */

data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val userType: String
)
