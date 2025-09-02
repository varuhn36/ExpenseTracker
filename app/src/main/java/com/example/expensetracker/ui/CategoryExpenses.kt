package com.example.expensetracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.expensetracker.viewmodel.ExpenseViewModel

@Composable
fun CategoryExpenses(
    navController: NavController,
    viewModel: ExpenseViewModel = hiltViewModel(),
) {
    val expenses by viewModel.expensesByCategory.collectAsState(initial = emptyList())
    var searchFilter by rememberSaveable { mutableStateOf("") }
    val filteredExpenses = remember(expenses, searchFilter) {
        if (searchFilter.isBlank()) expenses
        else expenses.filter { it.title.contains(searchFilter, ignoreCase = true) }
    }
    Scaffold(
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { navController.navigate("add_expense") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("New")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            Text(
                text = "${viewModel.getCategory()} Expenses",
                style = MaterialTheme.typography.titleLarge
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

            OutlinedTextField(
                value = searchFilter,
                onValueChange = {filter ->
                    searchFilter = filter
                },
                label = { Text("Search for a Category") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Spacer(Modifier.height(32.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredExpenses, key = { it.id }) { expense ->
                    ExpenseRow(
                        expense = expense,
                        onEdit = { viewModel.setSelectedExpense(it); navController.navigate("edit_expense") },
                        onDetails = { viewModel.setSelectedExpense(it); navController.navigate("expense_details") },
                        onDelete = { viewModel.delete(it) },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

