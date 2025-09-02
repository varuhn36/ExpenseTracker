package com.example.expensetracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expensetracker.resources.hardcodedCurrencies
import com.example.expensetracker.viewmodel.ExpenseViewModel
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpense(
    navController: NavController,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val expense by viewModel.selectedEntry.collectAsState()

    var title by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var cost by rememberSaveable { mutableStateOf("") }        // <-- keep as String for input
    var store by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf("") }
    var currency by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(expense?.id) {
        expense?.let { e ->
            title = e.title
            category = e.category
            cost = centsToEditableAmount(e.cost)
            store = e.store
            date = e.date
            currency = e.currency
        }
    }

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
        if (cost.isBlank()) return@remember null

        val normalized = cost.replace(',', '.')
        val amount = normalized.toBigDecimalOrNull() ?: return@remember null

        //Cap to 2 decimals for cents
        val scaled = amount.setScale(2, RoundingMode.HALF_UP)

        //Reject negative values
        if (scaled < BigDecimal.ZERO) return@remember null

        //Convert to minor units
        try {
            scaled.movePointRight(2).longValueExact()
        } catch (_: ArithmeticException) {
            null
        }
    }
    val costHasError = cost.isNotEmpty() && (costCents == null || costCents <= 0L)

    val dateError by remember(date) {
        mutableStateOf(date.isNotBlank() && !isValidIsoDate(date))
    }

    val currencyOptions = remember { hardcodedCurrencies }
    val currencyError by remember(currency) {
        mutableStateOf(currency.isNotBlank() && !isValidCurrencyCode(currency))
    }

    val isFormValid =
        !titleError &&
                !storeError &&
                !dateError &&
                !currencyError &&
                (costCents != null && costCents > 0L)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Edit Expense",
            style = MaterialTheme.typography.headlineSmall
        )

        //Title
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

        //Category
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
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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
                val candidate = input.replace(',', '.').let { s ->
                    if (s.startsWith(".")) "0$s" else s
                }
                val pattern = Regex("""^\d*(?:\.\d{0,2})?$""")
                if (candidate.isEmpty() || pattern.matches(candidate)) {
                    cost = candidate
                }
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
            isError = dateError,
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
                filteredCurrencies.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text("${opt.code} — ${opt.name}") },
                        onClick = {
                            currency = opt.code
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
                val safeCurrency = currency.ifBlank { "USD" }.trim()
                val safeDate = date.ifBlank { todayIso() }

                viewModel.createOrUpdate(
                    title = title.trim(),
                    category = safeCategory,
                    cost = costCents!!,
                    store = store.trim(),
                    date = safeDate,
                    currency = safeCurrency
                )

                navController.navigate("expense_details")
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid
        ) {
            Text("Save changes")
        }
    }
}

private val isoFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

private fun isValidIsoDate(value: String): Boolean =
    runCatching { LocalDate.parse(value, isoFormatter) }.isSuccess

private fun isValidCurrencyCode(code: String): Boolean {
    val c = code.trim()
    if (c.length != 3) return false
    return runCatching { Currency.getInstance(c.uppercase(Locale.ROOT)) }.isSuccess
}

private fun todayIso(): String = LocalDate.now().format(isoFormatter)

private fun centsToEditableAmount(cents: Long): String =
    (BigDecimal(cents).movePointLeft(2)).setScale(2, RoundingMode.UNNECESSARY).toPlainString()
