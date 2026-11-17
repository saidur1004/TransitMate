package com.transitmate.bd.domain.model

data class MetroStation(
    val order: Int,
    val code: String,
    val nameEn: String,
    val nameBn: String,
    val lineName: String,
    val isTerminal: Boolean,
    val distanceKmFromStart: Double,
    val landmarksEn: String,
    val landmarksBn: String
)

data class FareCalculationResult(
    val origin: MetroStation,
    val destination: MetroStation,
    val stationCount: Int,
    val singleJourneyFareBdt: Int,
    val cardDiscountFareBdt: Int
)
