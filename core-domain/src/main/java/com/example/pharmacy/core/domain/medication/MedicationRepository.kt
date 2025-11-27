package com.example.pharmacy.core.domain.medication

import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    fun streamAll(): Flow<List<Medication>>
    suspend fun add(med: Medication)
    suspend fun update(med: Medication)
    suspend fun delete(id: String)
}
