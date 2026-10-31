package com.example.gpstracker.feature.places.nearby

import androidx.annotation.DrawableRes
import com.example.gpstracker.R
import com.example.gpstracker.core.places.NearbyCategory

@DrawableRes
fun NearbyCategory.iconRes(): Int = when (this) {
    NearbyCategory.Hospital -> R.drawable.ic_nearby_hospital
    NearbyCategory.Hotels -> R.drawable.ic_nearby_hotels
    NearbyCategory.Restaurant -> R.drawable.ic_nearby_restaurant
    NearbyCategory.Airport -> R.drawable.ic_nearby_airport
    NearbyCategory.Bank -> R.drawable.ic_nearby_bank
    NearbyCategory.Atm -> R.drawable.ic_nearby_atm
    NearbyCategory.PostOffice -> R.drawable.ic_nearby_post_office
    NearbyCategory.School -> R.drawable.ic_nearby_school
    NearbyCategory.University -> R.drawable.ic_nearby_university
    NearbyCategory.BusStation -> R.drawable.ic_nearby_bus
    NearbyCategory.PoliceStation -> R.drawable.ic_nearby_police
    NearbyCategory.Doctor -> R.drawable.ic_nearby_doctor
    NearbyCategory.Theater -> R.drawable.ic_nearby_theater
    NearbyCategory.Shopping -> R.drawable.ic_nearby_shopping
    NearbyCategory.PetrolPump -> R.drawable.ic_nearby_petrol
    NearbyCategory.CcdCafe -> R.drawable.ic_nearby_cafe
    NearbyCategory.Mosques -> R.drawable.ic_nearby_mosque
    NearbyCategory.Church -> R.drawable.ic_nearby_church
    NearbyCategory.Gym -> R.drawable.ic_nearby_gym
    NearbyCategory.Spa -> R.drawable.ic_nearby_sport
    NearbyCategory.Garage -> R.drawable.ic_nearby_garage
}
