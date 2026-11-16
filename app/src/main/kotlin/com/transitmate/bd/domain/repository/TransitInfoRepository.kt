package com.transitmate.bd.domain.repository

import com.transitmate.bd.domain.model.FareCalculationResult
import com.transitmate.bd.domain.model.MetroStation

interface TransitInfoRepository {
    fun getStations(): List<MetroStation>
    fun searchStations(query: String): List<MetroStation>
    fun calculateFare(originCode: String, destinationCode: String): FareCalculationResult?
}
