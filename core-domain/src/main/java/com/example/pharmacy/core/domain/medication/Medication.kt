package com.example.pharmacy.core.domain.medication

data class Medication(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val imageUrl: String? = null,
    val description: String? = null,
)
