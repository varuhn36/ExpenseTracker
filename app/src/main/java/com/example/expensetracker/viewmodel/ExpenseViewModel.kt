package com.example.expensetracker.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.api.FrankfurterApi
import com.example.expensetracker.data.settings.UserPrefs
import com.example.expensetracker.entities.ExpenseEntity
import com.example.expensetracker.repo.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val api: FrankfurterApi,
    private val userPrefs: UserPrefs
) : ViewModel() {

    private val _selectedExpense = MutableStateFlow<ExpenseEntity?>(null)
    val selectedEntry: StateFlow<ExpenseEntity?> = _selectedExpense

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    private val _showInHomeCurrency = MutableStateFlow(false)
    val showInHomeCurrency: StateFlow<Boolean> = _showInHomeCurrency

    private val rateCache = mutableMapOf<Pair<String, String>, BigDecimal>()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    fun clearError() { _error.value = null }

    val homeCurrency: StateFlow<String> =
        userPrefs.homeCurrency.stateIn(viewModelScope, SharingStarted.Eagerly, "USD")

    val expensesByCategory: StateFlow<List<ExpenseEntity>> =
        _selectedCategory
            .flatMapLatest { cat ->
                if (cat.isNullOrBlank()) {
                    expenseRepository.observeAll()
                } else {
                    expenseRepository.getExpensesByCategory(cat)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleCurrencyDisplay() {
        _showInHomeCurrency.value = !_showInHomeCurrency.value
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setSelectedExpense(expenseEntity: ExpenseEntity) {
        _selectedExpense.value = expenseEntity
    }

    fun getCategory(): String = selectedCategory.value.orEmpty()

    fun getAllCategories(): Flow<List<String>> {
        return expenseRepository.getAllCategories()
    }

    fun setHomeCurrency(code: String) {
        viewModelScope.launch {
            userPrefs.setHomeCurrency(code)
        }
    }

    suspend fun convertCurrency(
        amountCents: Long,
        from: String,
        to: String
    ): BigDecimal? {
        if (from.equals(to, ignoreCase = true)) {
            return BigDecimal.valueOf(amountCents, 2)
        }

        val key = from.uppercase() to to.uppercase()

        val rate: BigDecimal = rateCache[key] ?: try {
            val resp = api.getLatest(from.uppercase(), to.uppercase())
            val rateDbl = resp.rates[to.uppercase()]
            if (rateDbl == null) {
                _error.value = "Unable to fetch the latest rates. Please check your internet or try again later."
                _showInHomeCurrency.value = false
                return null
            }
            BigDecimal.valueOf(rateDbl).also { rateCache[key] = it }
        } catch (e: Exception) {
            Log.e("CurrencyDebug", "Conversion failed", e)
            _error.value = "Unable to fetch the latest rates. Please check your internet or try again later."
            _showInHomeCurrency.value = false
            return null
        }

        val amount = BigDecimal.valueOf(amountCents, 2)
        return amount.multiply(rate)
    }

    fun convertCurrencyAsync(entity: ExpenseEntity, onResult: (BigDecimal?) -> Unit) {
        println("RUNNING CURRENCY CONVERSION")
        viewModelScope.launch {
            val result = convertCurrency(
                amountCents = entity.cost,
                from = entity.currency,
                to = homeCurrency.value
            )
            onResult(result)
        }
    }

    fun createOrUpdate(
        title: String,
        category: String,
        cost: Long,
        store: String,
        date: String,
        currency: String
    ) {
        viewModelScope.launch {
            val current = _selectedExpense.value
            if (current == null) {
                expenseRepository.create(
                    title = title,
                    category = category,
                    cost = cost,
                    store = store,
                    date = date,
                    currency = currency
                )
            } else {
                val updated = current.copy(
                    title = title,
                    category = category,
                    cost = cost,
                    store = store,
                    date = date,
                    currency = currency
                )
                expenseRepository.update(updated)
                _selectedExpense.value = updated
            }
        }
    }

    fun delete(expenseEntity: ExpenseEntity) {
        viewModelScope.launch {
            expenseRepository.delete(expenseEntity)
        }
    }
}
