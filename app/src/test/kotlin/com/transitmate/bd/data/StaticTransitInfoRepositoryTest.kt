package com.transitmate.bd.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StaticTransitInfoRepositoryTest {

    private val repo = StaticTransitInfoRepository()

    @Test
    fun getStations_returnsAllSixteenMrtLine6StationsInOrder() {
        val stations = repo.getStations()
        assertEquals(16, stations.size)
        assertEquals("UTN", stations.first().code)
        assertEquals("MTJ", stations.last().code)
        assertTrue(stations.first().isTerminal)
        assertTrue(stations.last().isTerminal)
    }

    @Test
    fun searchStations_supportsEnglishAndBanglaQueries() {
        val enMatch = repo.searchStations("Agargaon")
        assertEquals(1, enMatch.size)
        assertEquals("AGR", enMatch.first().code)

        val bnMatch = repo.searchStations("মতিঝিল")
        assertEquals(1, bnMatch.size)
        assertEquals("MTJ", bnMatch.first().code)
    }

    @Test
    fun calculateFare_matchesPublishedDmtclMatrixAndPassDiscount() {
        val utnToMtj = repo.calculateFare("UTN", "MTJ")
        assertNotNull(utnToMtj)
        assertEquals(100, utnToMtj?.singleJourneyFareBdt)
        assertEquals(90, utnToMtj?.cardDiscountFareBdt)
        assertEquals(16, utnToMtj?.stationCount)

        val agrToMtj = repo.calculateFare("AGR", "MTJ")
        assertEquals(50, agrToMtj?.singleJourneyFareBdt)
        assertEquals(45, agrToMtj?.cardDiscountFareBdt)

        val fmgToMtj = repo.calculateFare("FMG", "MTJ")
        assertEquals(40, fmgToMtj?.singleJourneyFareBdt)
        assertEquals(36, fmgToMtj?.cardDiscountFareBdt)
    }

    @Test
    fun calculateFare_sameStationOrInvalid_returnsNull() {
        assertNull(repo.calculateFare("MTJ", "MTJ"))
        assertNull(repo.calculateFare("INVALID", "MTJ"))
    }
}
