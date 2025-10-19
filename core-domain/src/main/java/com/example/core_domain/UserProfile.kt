package com.example.core_domain
data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String? = null,
    val extraNotes: String? = null,
    val avatarUrl: String? = null,
    )
