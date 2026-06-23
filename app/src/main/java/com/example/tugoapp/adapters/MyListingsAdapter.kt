package com.example.tugoapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tugoapp.R
import com.example.tugoapp.models.Car

class MyListingsAdapter(
    private val cars: MutableList<Car>,
    private val onCarClicked: (Car) -> Unit,

) : RecyclerView.Adapter<MyListingsAdapter.ListingsViewHolder>() {

    inner class ListingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ownerCarProfilePhotoListItemImageView: ImageView =
            itemView.findViewById(R.id.owner_car_profile_photo_list_item_imageview)
        val ownerCarBrandModelListTextView: TextView =
            itemView.findViewById(R.id.owner_car_brand_model_list_item_textview)
        val ownerCarPricePerDayListTextView: TextView =
            itemView.findViewById(R.id.owner_car_price_per_day_list_item_textview)
        val ownerCarLicensePlateListTextView: TextView =
            itemView.findViewById(R.id.owner_car_license_plate_list_item_textview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_car_listing, parent, false)
        return ListingsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListingsViewHolder, position: Int) {
        val car = cars[position]

        holder.ownerCarBrandModelListTextView.text = "${car.brand} ${car.model}"
        holder.ownerCarPricePerDayListTextView.text = "$${car.pricePerDay}/day"
        holder.ownerCarLicensePlateListTextView.text = car.licensePlate

        Glide.with(holder.itemView.context)
            .load(car.photoUrl)
            .placeholder(R.drawable.ic_launcher_foreground)
            .into(holder.ownerCarProfilePhotoListItemImageView)

        holder.itemView.setOnClickListener { onCarClicked(car) }
    }

    override fun getItemCount() = cars.size
}