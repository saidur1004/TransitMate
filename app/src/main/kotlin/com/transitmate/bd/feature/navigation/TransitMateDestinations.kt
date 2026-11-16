package com.transitmate.bd.feature.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Contactless
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import com.transitmate.bd.R
import com.transitmate.bd.core.designsystem.component.BottomNavDestination

object TransitRoutes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val MY_CARDS = "my_cards"
    const val SCAN = "scan"
    const val SCAN_RESULT = "scan_result"
    const val CARD_DETAILS = "card_details/{cardId}"
    const val INSIGHTS = "insights"
    const val HISTORY = "history?cardId={cardId}"
    const val FARE_CALCULATOR = "fare_calculator?originCode={originCode}"
    const val STATION_EXPLORER = "station_explorer"
    const val SETTINGS = "settings"
    const val DATA_MANAGEMENT = "data_management"
    const val NFC_DIAGNOSTICS = "nfc_diagnostics"
    const val HELP_FAQ = "help_faq"
    const val ABOUT = "about"

    fun cardDetails(cardId: Long): String = "card_details/$cardId"
    fun history(cardId: Long? = null): String =
        if (cardId != null) "history?cardId=$cardId" else "history"
    fun fareCalculator(originCode: String? = null): String =
        if (originCode != null) "fare_calculator?originCode=$originCode" else "fare_calculator"
}

val BottomNavigationDestinations = listOf(
    BottomNavDestination(TransitRoutes.HOME, R.string.nav_home, Icons.Rounded.Home),
    BottomNavDestination(TransitRoutes.MY_CARDS, R.string.nav_my_cards, Icons.Rounded.CreditCard),
    BottomNavDestination(TransitRoutes.SCAN, R.string.nav_scan, Icons.Rounded.Contactless),
    BottomNavDestination(TransitRoutes.INSIGHTS, R.string.nav_insights, Icons.Rounded.BarChart),
    BottomNavDestination(TransitRoutes.SETTINGS, R.string.nav_settings, Icons.Rounded.Settings)
)
