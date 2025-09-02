package com.example.expensetracker.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expensetracker.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
fun ExpenseDetails(
    navController: NavController,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val expense by viewModel.selectedEntry.collectAsState()
    var displayAmount by remember { mutableStateOf<String?>(null) }

    val showInHomeCurrency by viewModel.showInHomeCurrency.collectAsState()
    val homeCurrency by viewModel.homeCurrency.collectAsState()

    LaunchedEffect(showInHomeCurrency, expense!!.id, homeCurrency) {
        println("Launched effect running")
        if (showInHomeCurrency) {
            viewModel.convertCurrencyAsync(expense!!) { result ->
                displayAmount = result?.let {
                    formatDecimal(it, homeCurrency)
                } ?: formatCents(expense!!.cost, expense!!.currency)
            }
        } else {
            displayAmount = formatCents(expense!!.cost, expense!!.currency)
        }
    }

    val context = LocalContext.current
    val error by viewModel.error.collectAsState()

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Expense Details",
            style = MaterialTheme.typography.headlineSmall
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Show in Home Currency", style = MaterialTheme.typography.bodyMedium)
            val showInHomeCurrency by viewModel.showInHomeCurrency.collectAsState()
            Switch(
                checked = showInHomeCurrency,
                onCheckedChange = { viewModel.toggleCurrencyDisplay() }
            )
        }

        val e = expense!!

        DetailCard(label = "Title", value = e.title)
        DetailCard(label = "Category", value = e.category)
        DetailCard(
            label = "Cost",
            value = displayAmount.toString()
        )
        DetailCard(label = "Store", value = e.store)
        DetailCard(label = "Date", value = e.date)

        Button(
            onClick = {
                viewModel.setSelectedExpense(expense!!)
                navController.navigate("edit_expense")
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Edit Expense")
        }
    }
}

@Composable
private fun DetailCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


private fun formatCents(
    cents: Long,
    currencyCode: String,
    locale: Locale = Locale.getDefault()
): String {
    val nf = NumberFormat.getCurrencyInstance(locale)
    val safeCurrency = try {
        Currency.getInstance(currencyCode.uppercase(Locale.ROOT))
    } catch (e: Exception) {
        Currency.getInstance("USD")
    }
    nf.currency = safeCurrency
    return nf.format(cents.toDouble() / 100.0)
}

private fun formatDecimal(
    amount: java.math.BigDecimal,
    currencyCode: String,
    locale: Locale = Locale.getDefault()
): String {
    val nf = NumberFormat.getCurrencyInstance(locale)
    val safeCurrency = try {
        Currency.getInstance(currencyCode.uppercase(Locale.ROOT))
    } catch (e: Exception) {
        Currency.getInstance("USD")
    }
    nf.currency = safeCurrency
    return nf.format(amount)
}

