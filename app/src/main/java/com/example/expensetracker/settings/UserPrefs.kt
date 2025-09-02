package com.example.expensetracker.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPrefs @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val KEY_HOME = stringPreferencesKey("home_currency")

    //Default to USD if we don't have anything set
    val homeCurrency: Flow<String> = dataStore.data.map { it[KEY_HOME] ?: "USD" }

    suspend fun setHomeCurrency(code: String) {
        val normalized = code.trim().uppercase(Locale.ROOT)
        dataStore.edit { it[KEY_HOME] = normalized }
    }
}
