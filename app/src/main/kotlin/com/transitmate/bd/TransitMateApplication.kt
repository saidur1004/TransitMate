package com.transitmate.bd

import android.app.Application
import com.transitmate.bd.core.common.DefaultDispatcherProvider
import com.transitmate.bd.core.common.DispatcherProvider
import com.transitmate.bd.core.database.TransitMateDatabase
import com.transitmate.bd.core.nfc.AndroidNfcAvailabilityChecker
import com.transitmate.bd.core.nfc.NfcDiagnosticReader
import com.transitmate.bd.core.nfc.NfcTagScanMapper
import com.transitmate.bd.data.DataStorePreferencesRepository
import com.transitmate.bd.data.DefaultNfcAvailabilityRepository
import com.transitmate.bd.data.RoomCardRepository
import com.transitmate.bd.data.StaticTransitInfoRepository
import com.transitmate.bd.domain.repository.CardRepository
import com.transitmate.bd.domain.repository.NfcAvailabilityRepository
import com.transitmate.bd.domain.repository.PreferencesRepository
import com.transitmate.bd.domain.repository.TransitInfoRepository

class TransitMateContainer(application: Application) {
    val dispatchers: DispatcherProvider = DefaultDispatcherProvider()

    private val database: TransitMateDatabase by lazy {
        TransitMateDatabase.getInstance(application)
    }

    val nfcAvailabilityRepository: NfcAvailabilityRepository by lazy {
        DefaultNfcAvailabilityRepository(AndroidNfcAvailabilityChecker(application))
    }

    val cardRepository: CardRepository by lazy {
        RoomCardRepository(database.transitCardDao())
    }

    val preferencesRepository: PreferencesRepository by lazy {
        DataStorePreferencesRepository(application)
    }

    val transitInfoRepository: TransitInfoRepository by lazy {
        StaticTransitInfoRepository()
    }

    val nfcDiagnosticReader: NfcDiagnosticReader by lazy {
        NfcDiagnosticReader(NfcTagScanMapper())
    }
}

class TransitMateApplication : Application() {
    lateinit var container: TransitMateContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = TransitMateContainer(this)
    }
}
