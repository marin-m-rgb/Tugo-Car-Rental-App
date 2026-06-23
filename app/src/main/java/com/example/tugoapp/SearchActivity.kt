package com.example.tugoapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class SearchActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var renterSearchCityEdittext: EditText
    private lateinit var renterSearchButton: View
    private lateinit var renterBookingsButton: View
    private lateinit var renterLogoutButton: View

    private lateinit var mMap: GoogleMap
    private var mapReady = false

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        bindViews()
        setupMap()
        wireButtons()
    }

    private fun bindViews() {
        renterSearchCityEdittext = findViewById(R.id.renter_search_city_edittext)
        renterSearchButton = findViewById(R.id.renter_search_button)
        renterBookingsButton = findViewById(R.id.renter_bookings_button)
        renterLogoutButton = findViewById(R.id.renter_logout_button)
    }

    private fun setupMap() {
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.renter_search_map)
                    as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    private fun wireButtons() {

        renterSearchButton.setOnClickListener {
            searchCars(renterSearchCityEdittext.text.toString().trim())
        }

        renterBookingsButton.setOnClickListener {
            startActivity(
                Intent(this, MyBookingsActivity::class.java)
            )
        }

        renterLogoutButton.setOnClickListener {

            Firebase.auth.signOut()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mapReady = true

        mMap.uiSettings.isZoomControlsEnabled = true

        mMap.setOnMarkerClickListener { marker ->
            val carId = marker.tag as? String ?: return@setOnMarkerClickListener true

            startActivity(
                Intent(this, CarProfileActivity::class.java)
                    .putExtra("carId", carId)
            )
            true
        }

        loadAllCars()
    }

    private fun loadAllCars() {
        if (!mapReady) return

        db.collection("cars")
            .get()
            .addOnSuccessListener { result ->

                mMap.clear()

                if (result.isEmpty) return@addOnSuccessListener

                val bounds = LatLngBounds.builder()

                for (doc in result) {

                    val lat = doc.getDouble("latitude") ?: continue
                    val lng = doc.getDouble("longitude") ?: continue

                    val pos = LatLng(lat, lng)

                    val marker = mMap.addMarker(
                        MarkerOptions()
                            .position(pos)
                            .icon(createPriceMarker(doc.getDouble("pricePerDay") ?: 0.0))
                    )

                    marker?.tag = doc.id
                    bounds.include(pos)
                }

                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(bounds.build(), 120)
                )
            }
    }

    private fun searchCars(city: String) {
        if (city.isEmpty()) return

        db.collection("cars")
            .whereEqualTo("city", city)
            .get()
            .addOnSuccessListener { result ->

                mMap.clear()

                if (result.isEmpty) return@addOnSuccessListener

                val bounds = LatLngBounds.builder()

                for (doc in result) {

                    val lat = doc.getDouble("latitude") ?: continue
                    val lng = doc.getDouble("longitude") ?: continue

                    val pos = LatLng(lat, lng)

                    val marker = mMap.addMarker(
                        MarkerOptions()
                            .position(pos)
                            .icon(createPriceMarker(doc.getDouble("pricePerDay") ?: 0.0))
                    )

                    marker?.tag = doc.id
                    bounds.include(pos)
                }

                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(bounds.build(), 120)
                )
            }
    }

    private fun createPriceMarker(price: Double): BitmapDescriptor {

        val tv = TextView(this).apply {
            text = "$$price"
            setTextColor(android.graphics.Color.WHITE)
            textSize = 13f
            setPadding(24, 12, 24, 12)
            setBackgroundColor(android.graphics.Color.parseColor("#CC000000"))
        }

        tv.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )

        tv.layout(0, 0, tv.measuredWidth, tv.measuredHeight)

        val bitmap = Bitmap.createBitmap(
            tv.measuredWidth,
            tv.measuredHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        tv.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}