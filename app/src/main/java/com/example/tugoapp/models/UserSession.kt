package com.example.tugoapp.models

/**
 * Singleton object that represents the currently logged in User session.
 *
 * Stores User information such as first name, last name, email, UID, and User type.
 * The User can either be a car owner or a renter.
 * This object is also used to check login state and User role across the app.
 */

object UserSession {

    var firstName: String? = null
    var lastName: String? = null
    var email: String? = null
    var uid: String? = null
    var userType: String? = null

    fun resetSession() {
        firstName = null
        lastName = null
        email = null
        uid = null
        userType = null
    }

    fun isLoggedIn(): Boolean = uid != null

    fun isOwner(): Boolean = userType == "owner"
}
