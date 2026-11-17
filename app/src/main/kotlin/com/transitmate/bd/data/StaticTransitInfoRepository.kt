package com.transitmate.bd.data

import com.transitmate.bd.domain.model.FareCalculationResult
import com.transitmate.bd.domain.model.MetroStation
import com.transitmate.bd.domain.repository.TransitInfoRepository
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Offline directory of operational Dhaka Metro Rail (MRT Line 6) stations
 * and verified DMTCL single-journey / 10%-discounted pass fare matrix.
 */
class StaticTransitInfoRepository : TransitInfoRepository {

    private val stations: List<MetroStation> = listOf(
        MetroStation(1, "UTN", "Uttara North", "উত্তরা উত্তর", "MRT Line 6", true, 0.0, "Diabari Depot, Sector 15–18", "দিয়াবাড়ী ডিপো, সেক্টর ১৫–১৮"),
        MetroStation(2, "UTC", "Uttara Center", "উত্তরা সেন্টার", "MRT Line 6", false, 1.5, "Sector 16 & 18 Connector", "সেক্টর ১৬ ও ১৮ সংযোগ সড়ক"),
        MetroStation(3, "UTS", "Uttara South", "উত্তরা দক্ষিণ", "MRT Line 6", false, 3.0, "Uttara Sector 12, Khalpar", "উত্তরা সেক্টর ১২, খালপাড়"),
        MetroStation(4, "PLB", "Pallabi", "পল্লবী", "MRT Line 6", false, 4.9, "Pallabi, Mirpur Section 12", "পল্লবী, মিরপুর সেকশন ১২"),
        MetroStation(5, "M11", "Mirpur 11", "মিরপুর ১১", "MRT Line 6", false, 6.2, "Mirpur Section 11, Kalshi Road", "মিরপুর সেকশন ১১, কালশী রোড"),
        MetroStation(6, "M10", "Mirpur 10", "মিরপুর ১০", "MRT Line 6", false, 7.6, "Mirpur 10 Circle, Benarasi Palli", "মিরপুর ১০ গোলচত্বর, বেনারসি পল্লী"),
        MetroStation(7, "KZP", "Kazipara", "কাজীপাড়া", "MRT Line 6", false, 8.7, "Kazipara Bus Stand, Begum Rokeya Sarani", "কাজীপাড়া বাসস্ট্যান্ড, বেগম রোকেয়া সরণি"),
        MetroStation(8, "SWP", "Shewrapara", "শেওড়াপাড়া", "MRT Line 6", false, 9.8, "West & East Shewrapara", "পশ্চিম ও পূর্ব শেওড়াপাড়া"),
        MetroStation(9, "AGR", "Agargaon", "আগারগাঁও", "MRT Line 6", false, 11.5, "IDB Bhaban, National Archive, Shishu Mela", "আইডিবি ভবন, জাতীয় আর্কাইভ, শিশু মেলা"),
        MetroStation(10, "BJS", "Bijoy Sarani", "বিজয় সরণি", "MRT Line 6", false, 12.7, "Bangabandhu Military Museum, Novo Theatre", "বিজয় সরণি মোড়, নভো থিয়েটার"),
        MetroStation(11, "FMG", "Farmgate", "ফার্মগেট", "MRT Line 6", false, 14.0, "Ananda Cinema Hall, Khamarbari, Tejgaon College", "খামারবাড়ি, আনন্দ হল, তেজগাঁও কলেজ"),
        MetroStation(12, "KWB", "Karwan Bazar", "কারওয়ান বাজার", "MRT Line 6", false, 15.3, "SAARC Fountain, Bashundhara City", "সার্ক ফোয়ারা, বসুন্ধরা সিটি"),
        MetroStation(13, "SHB", "Shahbagh", "শাহবাগ", "MRT Line 6", false, 16.4, "National Museum, BSMMU, Shahbagh Intersection", "জাতীয় জাদুঘর, পিজি হাসপাতাল, শাহবাগ মোড়"),
        MetroStation(14, "DUV", "Dhaka University", "ঢাকা বিশ্ববিদ্যালয়", "MRT Line 6", false, 17.5, "TSC, Curzon Hall, Central Library", "টিএসসি, কার্জন হল, কেন্দ্রীয় গ্রন্থাগার"),
        MetroStation(15, "SEC", "Bangladesh Secretariat", "বাংলাদেশ সচিবালয়", "MRT Line 6", false, 18.6, "National Press Club, Secretariat Gate", "জাতীয় প্রেস ক্লাব, সচিবালয় গেট"),
        MetroStation(16, "MTJ", "Motijheel", "মতিঝিল", "MRT Line 6", true, 20.1, "Shapla Chattar, Bangladesh Bank", "শাপলা চত্বর, বাংলাদেশ ব্যাংক")
    )

    // Published DMTCL MRT Line 6 symmetric single-journey fare chart (BDT) across stations 1..16
    private val fareMatrix: Array<IntArray> = arrayOf(
        intArrayOf(  0,  20,  20,  30,  30,  40,  40,  50,  60,  60,  70,  80,  80,  90,  90, 100), // 01 UTN
        intArrayOf( 20,   0,  20,  20,  30,  30,  40,  40,  50,  60,  70,  70,  80,  80,  90,  90), // 02 UTC
        intArrayOf( 20,  20,   0,  20,  20,  30,  30,  40,  50,  50,  60,  70,  70,  80,  80,  90), // 03 UTS
        intArrayOf( 30,  20,  20,   0,  20,  20,  30,  30,  40,  50,  60,  60,  70,  70,  80,  90), // 04 PLB
        intArrayOf( 30,  30,  20,  20,   0,  20,  20,  30,  40,  40,  50,  60,  60,  70,  70,  80), // 05 M11
        intArrayOf( 40,  30,  30,  20,  20,   0,  20,  20,  30,  40,  40,  50,  60,  60,  70,  70), // 06 M10
        intArrayOf( 40,  40,  30,  30,  20,  20,   0,  20,  20,  30,  40,  40,  50,  50,  60,  70), // 07 KZP
        intArrayOf( 50,  40,  40,  30,  30,  20,  20,   0,  20,  20,  30,  40,  40,  50,  50,  60), // 08 SWP
        intArrayOf( 60,  50,  50,  40,  40,  30,  20,  20,   0,  20,  30,  30,  40,  40,  50,  50), // 09 AGR
        intArrayOf( 60,  60,  50,  50,  40,  40,  30,  20,  20,   0,  20,  30,  30,  40,  40,  50), // 10 BJS
        intArrayOf( 70,  70,  60,  60,  50,  40,  40,  30,  30,  20,   0,  20,  20,  30,  30,  40), // 11 FMG
        intArrayOf( 80,  70,  70,  60,  60,  50,  40,  40,  30,  30,  20,   0,  20,  20,  30,  30), // 12 KWB
        intArrayOf( 80,  80,  70,  70,  60,  60,  50,  40,  40,  30,  20,  20,   0,  20,  20,  30), // 13 SHB
        intArrayOf( 90,  80,  80,  70,  70,  60,  50,  50,  40,  40,  30,  20,  20,   0,  20,  30), // 14 DUV
        intArrayOf( 90,  90,  80,  80,  70,  70,  60,  50,  50,  40,  30,  30,  20,  20,   0,  20), // 15 SEC
        intArrayOf(100,  90,  90,  90,  80,  70,  70,  60,  50,  50,  40,  30,  30,  30,  20,   0)  // 16 MTJ
    )

    override fun getStations(): List<MetroStation> = stations

    override fun searchStations(query: String): List<MetroStation> {
        val clean = query.trim()
        if (clean.isEmpty()) return stations
        return stations.filter { station ->
            station.nameEn.contains(clean, ignoreCase = true) ||
                station.nameBn.contains(clean, ignoreCase = true) ||
                station.landmarksEn.contains(clean, ignoreCase = true) ||
                station.landmarksBn.contains(clean, ignoreCase = true)
        }
    }

    override fun calculateFare(originCode: String, destinationCode: String): FareCalculationResult? {
        val originIdx = stations.indexOfFirst { it.code == originCode }
        val destIdx = stations.indexOfFirst { it.code == destinationCode }
        if (originIdx == -1 || destIdx == -1 || originIdx == destIdx) return null

        val origin = stations[originIdx]
        val destination = stations[destIdx]
        val singleFare = fareMatrix[originIdx][destIdx]
        val discountedFare = (singleFare * 0.9).roundToInt()

        return FareCalculationResult(
            origin = origin,
            destination = destination,
            stationCount = abs(destIdx - originIdx) + 1,
            singleJourneyFareBdt = singleFare,
            cardDiscountFareBdt = discountedFare
        )
    }
}
