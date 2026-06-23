package com.example.tugoapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tugoapp.R
import com.example.tugoapp.models.BookingWithCar
import java.text.SimpleDateFormat
import java.util.Locale

class OwnerBookingsAdapter(
    private var bookingsWithCars: MutableList<BookingWithCar>,
    private val onDeleteButtonClicked: (String) -> Unit,
) : RecyclerView.Adapter<OwnerBookingsAdapter.BookingViewHolder>() {

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ownerCarProfilePhotoImageView: ImageView =
            itemView.findViewById(R.id.owner_car_profile_photo_imageview)
        val ownerConfirmationCodeTextView: TextView =
            itemView.findViewById(R.id.owner_confirmation_code_textview)
        val ownerCarLicensePlateTextView: TextView =
            itemView.findViewById(R.id.owner_car_license_plate_textview)
        val ownerPricePerDayTextView: TextView =
            itemView.findViewById(R.id.owner_price_per_day_textview)
        val renterNameTextView: TextView =
            itemView.findViewById(R.id.renter_name_textview)
        val ownerCarRentDateTextView: TextView =
            itemView.findViewById(R.id.owner_car_rent_date_textview)
        val ownerCancelBookingButton: Button =
            itemView.findViewById(R.id.owner_cancel_booking_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_owner_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val item = bookingsWithCars[position]
        val booking = item.booking
        val car = item.car
        val user = item.user

        holder.ownerConfirmationCodeTextView.text =
            "confirmation code: ${booking.confirmationCode}"

        val displayFormat = SimpleDateFormat("yyyy MMM dd", Locale.getDefault())

        val start = booking.startDate?.toDate()
        val end = booking.endDate?.toDate()

        holder.ownerCarRentDateTextView.text =
            if (start != null && end != null) {
                "${displayFormat.format(start)} - ${displayFormat.format(end)}"
            } else {
                "Invalid dates"
            }

        holder.ownerCarLicensePlateTextView.text = car.licensePlate
        holder.ownerPricePerDayTextView.text = "$${car.pricePerDay}/day"

        Glide.with(holder.itemView.context)
            .load(car.photoUrl ?: "")
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(holder.ownerCarProfilePhotoImageView)

        holder.renterNameTextView.text =
            "Renter: ${user?.firstName ?: "Unknown"} ${user?.lastName ?: ""}"

        holder.ownerCancelBookingButton.setOnClickListener {
            onDeleteButtonClicked(booking.id)
        }
    }

    override fun getItemCount() = bookingsWithCars.size
}