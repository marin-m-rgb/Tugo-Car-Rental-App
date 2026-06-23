package com.example.tugoapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tugoapp.R
import com.example.tugoapp.models.BookingWithCar
import java.text.SimpleDateFormat
import java.util.Locale

class RenterBookingsAdapter(
    private var bookingsWithCars: MutableList<BookingWithCar>,
    private val onDeleteButtonClicked: (String) -> Unit,
) : RecyclerView.Adapter<RenterBookingsAdapter.RentingViewHolder>() {

    inner class RentingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val renterConfirmationCodeTextView: TextView =
            itemView.findViewById(R.id.renter_confirmation_code_textview)
        val renterCarLicensePlateTextView: TextView =
            itemView.findViewById(R.id.renter_car_license_plate_textview)
        val renterCarBrandModelTextView: TextView =
            itemView.findViewById(R.id.renter_car_brand_model_textview)
        val renterCarColorTextView: TextView =
            itemView.findViewById(R.id.renter_car_color_textview)
        val renterCarAddressCityTextView: TextView =
            itemView.findViewById(R.id.renter_car_address_city_textview)
        val renterStartEndRentTextView: TextView =
            itemView.findViewById(R.id.renter_start_end_rent_textview)
        val renterCancelBookingButton: Button =
            itemView.findViewById(R.id.renter_cancel_booking_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RentingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_renter_booking, parent, false)
        return RentingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RentingViewHolder, position: Int) {
        val item = bookingsWithCars[position]
        val booking = item.booking
        val car = item.car

        holder.renterConfirmationCodeTextView.text =
            "confirmation code: ${booking.confirmationCode}"

        val displayFormat = SimpleDateFormat("yyyy MMM dd", Locale.getDefault())

        val start = booking.startDate?.toDate()
        val end = booking.endDate?.toDate()

        holder.renterStartEndRentTextView.text =
            if (start != null && end != null) {
                "${displayFormat.format(start)} - ${displayFormat.format(end)}"
            } else {
                "Invalid dates"
            }

        holder.renterCarLicensePlateTextView.text = car.licensePlate
        holder.renterCarBrandModelTextView.text = "${car.brand} ${car.model}"
        holder.renterCarColorTextView.text = car.color
        holder.renterCarAddressCityTextView.text = car.address

        holder.renterCancelBookingButton.setOnClickListener {
            onDeleteButtonClicked(booking.id)
        }
    }

    override fun getItemCount() = bookingsWithCars.size
}
