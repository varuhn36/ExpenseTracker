package com.example.expensetracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expensetracker.resources.hardcodedCurrencies
import com.example.expensetracker.viewmodel.ExpenseViewModel
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Currency
import java.util.Locale
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpense(
    navController: NavController,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var store by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }

    val homeCurrency by viewModel.homeCurrency.collectAsState()
    var currency by remember(homeCurrency) { mutableStateOf(homeCurrency) }

    val allCategories by viewModel
        .getAllCategories()
        .map { it.distinct().sorted() }
        .collectAsState(initial = emptyList())

    var expanded by remember { mutableStateOf(false) }
    val filteredCategories = remember(category, allCategories) {
        if (category.isBlank()) allCategories
        else allCategories.filter { it.contains(category, ignoreCase = true) }
    }

    val titleError = title.isBlank()
    val storeError = store.isBlank()

    val costCents: Long? = remember(cost) {
        if (cost.isBlank()) null else runCatching {
            val normalized = cost.replace(',', '.')
            val bd = normalized.toBigDecimal()
            val scaled = bd.setScale(2, RoundingMode.HALF_UP)
            scaled.movePointRight(2).longValueExact()
        }.getOrNull()
    }
    val costHasError = cost.isNotEmpty() && (costCents == null || costCents <= 0L)

    val dateError by remember(date) { mutableStateOf(date.isNotBlank() && !isValidIsoDate(date)) }

    val currencyOptions = remember { hardcodedCurrencies }
    val currencyError = remember(currency) {
        currency.isBlank() || currencyOptions.none { it.code.equals(currency, ignoreCase = true) }
    }

    val isFormValid =
        !titleError && !storeError && !dateError && !currencyError && (costCents != null && costCents > 0L)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Add Expense", style = MaterialTheme.typography.headlineSmall)

        // Title
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title *") },
            isError = titleError,
            supportingText = { if (titleError) Text("Title is required") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        // Category
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {
                    category = it
                    expanded = true
                },
                label = { Text("Category") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            ExposedDropdownMenu(
                expanded = expanded && filteredCategories.isNotEmpty(),
                onDismissRequest = { expanded = false }
            ) {
                filteredCategories.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            category = option
                            expanded = false
                        }
                    )
                }
            }
        }

        //Cost
        OutlinedTextField(
            value = cost,
            onValueChange = { input ->
                val candidate = input.replace(',', '.').let { if (it.startsWith(".")) "0$it" else it }
                val pattern = Regex("""^\d*(?:\.\d{0,2})?$""")
                if (candidate.isEmpty() || pattern.matches(candidate)) cost = candidate
            },
            label = { Text("Cost *") },
            isError = costHasError,
            supportingText = {
                if (costHasError) Text("Enter a positive amount, e.g. 12.50")
                else if (cost.isBlank()) Text("Enter amount with up to 2 decimals")
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )

        //Store
        OutlinedTextField(
            value = store,
            onValueChange = { store = it },
            label = { Text("Store *") },
            isError = storeError,
            supportingText = { if (storeError) Text("Store is required") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        //Date
        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date (YYYY-MM-DD) *") },
            isError = date.isNotBlank() && dateError,
            supportingText = { if (dateError) Text("Use ISO format, e.g. ${todayIso()}") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        //Currency
        var currencyMenuOpen by remember { mutableStateOf(false) }
        var currencyFilter by remember(currency) { mutableStateOf("") }

        val filteredCurrencies = remember(currencyFilter, currencyOptions) {
            val q = currencyFilter.trim()
            if (q.isEmpty()) currencyOptions
            else currencyOptions.filter {
                it.code.contains(q, true) || it.name.contains(q, true)
            }
        }

        ExposedDropdownMenuBox(
            expanded = currencyMenuOpen,
            onExpandedChange = { currencyMenuOpen = it }
        ) {
            OutlinedTextField(
                value = currency,
                onValueChange = {
                    currency = it.uppercase(Locale.ROOT)
                    currencyFilter = it
                    currencyMenuOpen = true
                },
                label = { Text("Currency *") },
                isError = currencyError,
                supportingText = { if (currencyError) Text("Choose a supported currency") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(currencyMenuOpen) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
            ExposedDropdownMenu(
                expanded = currencyMenuOpen,
                onDismissRequest = { currencyMenuOpen = false }
            ) {
                filteredCurrencies.forEach { currencyOption ->
                    DropdownMenuItem(
                        text = { Text("${currencyOption.code} — ${currencyOption.name}") },
                        onClick = {
                            currency = currencyOption.code
                            currencyFilter = ""
                            currencyMenuOpen = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                val safeCategory = category.ifBlank { "Uncategorized" }.trim()
                val safeCurrency = currency.ifBlank { homeCurrency }.trim()
                val safeDate = date.ifBlank { todayIso() }

                viewModel.createOrUpdate(
                    title = title.trim(),
                    category = safeCategory,
                    cost = costCents!!,
                    store = store.trim(),
                    date = safeDate,
                    currency = safeCurrency
                )
                viewModel.setSelectedCategory(safeCategory)
                navController.navigate("category_expenses")
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid
        ) {
            Text("Add Expense")
        }
    }
}

private val isoFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

private fun isValidIsoDate(value: String): Boolean {
    return try {
        LocalDate.parse(value, isoFormatter)
        true
    } catch (_: DateTimeParseException) {
        false
    }
}

private fun todayIso(): String = LocalDate.now().format(isoFormatter)
