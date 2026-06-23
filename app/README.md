# TugoApp 

TugoApp is a car rental mobile application built with Android (Kotlin) and Firebase.  
It allows car owners to list vehicles and renters to browse, view details, and book cars in real time.

---

## Features

### Authentication
- User registration and login
- Role-based system (Owner / Renter)

### Car Listings (Owner)
- Create car listings
- Edit existing listings
- Upload car details (brand, model, price, location, image URL)
- Geolocation support (address → coordinates)

### Search & Map (Renter)
- View cars on Google Maps
- Search cars by city
- Price-based map markers
- Click marker → car details page

### Car Profile
- Full car details
- Owner information
- Image display (Glide)
- Booking system with date selection

### Booking System
- Select start/end dates
- Prevent overlapping bookings
- Total price calculation
- Store bookings in Firestore

---

## Tech Stack

- Kotlin (Android)
- Firebase Authentication
- Firebase Firestore
- Google Maps SDK
- Glide (image loading)

---

## Setup Instructions

1. Clone the repository
2. Open in Android Studio
3. Create a Firebase project
4. Enable:
    - Authentication (Email/Password)
    - Firestore Database
5. Download `google-services.json` and place it in `/app`
6. Sync Gradle
7. Run on emulator or device

---

## Important Notes

- This app requires a Firebase backend to function
- Firestore collections used:
    - `cars`
    - `users`
    - `bookings`
- Google Maps API key must be configured in `AndroidManifest.xml`

---

## Project Structure

- `models/` → Data classes (Car, User, Booking)
- `activities/` → UI screens
- `adapters/` → Recycler/adapters
- Firebase handles backend logic

---

*Built by Mayerly Marin Giraldo*