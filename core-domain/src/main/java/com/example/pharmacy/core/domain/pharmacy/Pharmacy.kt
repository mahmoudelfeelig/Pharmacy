package com.example.pharmacy.core.domain.pharmacy

data class Pharmacy(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val address: String? = null,
    val rating: Double? = null
)
