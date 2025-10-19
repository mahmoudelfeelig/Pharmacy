package com.example.pharmacy.core.domain.pharmacy

import kotlinx.coroutines.flow.Flow

interface PharmacyRepository {
    fun streamAll(): kotlinx.coroutines.flow.Flow<List<Pharmacy>>
}
