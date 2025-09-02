package com.example.expensetracker

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.expensetracker.ui.*
import com.example.expensetracker.viewmodel.ExpenseViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val items = listOf(
        BottomNavItem("home", "Home", Icons.Filled.Home),
        BottomNavItem("add_expense", "Add", Icons.Filled.Add),
        BottomNavItem("settings", "Settings", Icons.Filled.Menu)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val destination = navBackStackEntry?.destination

                items.forEach { item ->
                    val selected =
                        if (item.route == "home") {
                            destination?.hierarchy?.any { it.route == "main" } == true
                        } else {
                            destination?.hierarchy?.any { it.route == item.route } == true
                        }

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (item.route == "home") {
                                navController.navigate("home") {
                                    popUpTo("home") {
                                        saveState = true
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            } else {
                                navController.navigate(item.route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "main",
            modifier = Modifier.padding(innerPadding)
        ) {
            navigation(startDestination = "home", route = "main") {

                composable("home") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("main")
                    }
                    val vm: ExpenseViewModel = hiltViewModel(parentEntry)
                    HomePage(navController = navController, viewModel = vm)
                }

                composable("category_expenses") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("main")
                    }
                    val vm: ExpenseViewModel = hiltViewModel(parentEntry)
                    CategoryExpenses(navController = navController, viewModel = vm)
                }

                composable("expense_details") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("main")
                    }
                    val vm: ExpenseViewModel = hiltViewModel(parentEntry)
                    ExpenseDetails(navController = navController, viewModel = vm)
                }

                composable("edit_expense") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("main")
                    }
                    val vm: ExpenseViewModel = hiltViewModel(parentEntry)
                    EditExpense(navController = navController, viewModel = vm)
                }

                composable("add_expense") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("main")
                    }
                    val vm: ExpenseViewModel = hiltViewModel(parentEntry)
                    AddExpense(navController = navController, viewModel = vm)
                }

                composable("settings") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("main")
                    }
                    val vm: ExpenseViewModel = hiltViewModel(parentEntry)
                    SettingsScreen(viewModel = vm)
                }
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
