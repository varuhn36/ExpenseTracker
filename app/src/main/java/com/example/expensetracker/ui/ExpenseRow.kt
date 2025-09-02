package com.example.expensetracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensetracker.entities.ExpenseEntity
import com.example.expensetracker.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
fun ExpenseRow(
    expense: ExpenseEntity,
    modifier: Modifier = Modifier,
    onEdit: (ExpenseEntity) -> Unit,
    onDetails: (ExpenseEntity) -> Unit,
    onDelete: (ExpenseEntity) -> Unit,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    var menuOpen by remember { mutableStateOf(false) }
    var displayAmount by remember { mutableStateOf<String?>(null) }

    val showInHomeCurrency by viewModel.showInHomeCurrency.collectAsState()
    val homeCurrency by viewModel.homeCurrency.collectAsState()

    LaunchedEffect(showInHomeCurrency, expense.currency, expense.cost, homeCurrency) {
        println("Launched effect running")
        if (showInHomeCurrency) {
            viewModel.convertCurrencyAsync(expense) { result ->
                displayAmount = result?.let {
                    formatDecimal(it, homeCurrency)
                } ?: formatCents(expense.cost, expense.currency)
            }
        } else {
            displayAmount = formatCents(expense.cost, expense.currency)
        }
    }

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = expense.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = displayAmount ?: "…",
                style = MaterialTheme.typography.titleMedium,
            )

            // 3-dot menu
            Box(modifier = Modifier.wrapContentSize(Alignment.Center)) {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More")
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { menuOpen = false; onEdit(expense) }
                    )
                    DropdownMenuItem(
                        text = { Text("Details") },
                        onClick = { menuOpen = false; onDetails(expense) }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Delete",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            menuOpen = false
                            onDelete(expense)
                        }
                    )
                }
            }
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
        Currency.getInstance("USD") // fallback
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
