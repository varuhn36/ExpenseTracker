package com.example.expensetracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensetracker.resources.hardcodedCurrencies
import com.example.expensetracker.viewmodel.ExpenseViewModel
import java.util.Locale
import androidx.compose.ui.text.input.ImeAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val currentHomeCurrency by viewModel.homeCurrency.collectAsState()

    var menuOpen by remember { mutableStateOf(false) }
    var fieldValue by remember(currentHomeCurrency) { mutableStateOf(currentHomeCurrency) }
    var filter by remember { mutableStateOf("") }

    val filtered = remember(filter) {
        val q = filter.trim()
        if (q.isEmpty()) hardcodedCurrencies
        else hardcodedCurrencies.filter {
            it.code.contains(q, true) || it.name.contains(q, true)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Home currency", style = MaterialTheme.typography.titleMedium)

            ExposedDropdownMenuBox(
                expanded = menuOpen,
                onExpandedChange = { menuOpen = it }
            ) {
                OutlinedTextField(
                    value = fieldValue,
                    onValueChange = {
                        fieldValue = it.uppercase(Locale.ROOT)
                        filter = it
                        menuOpen = true
                    },
                    label = { Text("Select home currency") },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuOpen) },
                    supportingText = { Text("Current: $currentHomeCurrency") }
                )

                ExposedDropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false }
                ) {
                    filtered.forEach { currencyOption ->
                        DropdownMenuItem(
                            text = { Text("${currencyOption.code} — ${currencyOption.name}") },
                            onClick = {
                                fieldValue = currencyOption.code
                                filter = ""
                                menuOpen = false
                                viewModel.setHomeCurrency(currencyOption.code)
                            }
                        )
                    }
                }
            }

            Text(
                text = "Used when “Show in Home Currency” is enabled.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
