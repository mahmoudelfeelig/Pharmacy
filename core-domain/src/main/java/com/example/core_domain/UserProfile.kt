package com.example.pharmacy.core.domain
data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String? = null,
    val extraNotes: String? = null
)
