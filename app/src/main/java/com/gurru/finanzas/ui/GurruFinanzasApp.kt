package com.gurru.finanzas.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gurru.finanzas.ui.screens.DashboardScreen
import com.gurru.finanzas.ui.screens.PlanningScreen
import com.gurru.finanzas.ui.screens.ScheduleScreen
import com.gurru.finanzas.ui.screens.TransactionsScreen

private enum class AppDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    INICIO("inicio", "Inicio", Icons.Default.Home),
    GASTOS("gastos", "Gastos", Icons.Default.AccountBalanceWallet),
    AGENDA("agenda", "Agenda", Icons.Default.CalendarMonth),
    PLAN("plan", "Plan", Icons.Default.Savings)
}

@Composable
fun GurruFinanzasApp(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppDestination.entries.forEach { dest ->
                    NavigationBarItem(
                        selected = currentRoute == dest.route,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.INICIO.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppDestination.INICIO.route) {
                DashboardScreen(state = state)
            }
            composable(AppDestination.GASTOS.route) {
                TransactionsScreen(
                    state = state,
                    onAddExpense = viewModel::saveExpense,
                    onDeleteExpense = viewModel::deleteExpense
                )
            }
            composable(AppDestination.AGENDA.route) {
                ScheduleScreen(
                    state = state,
                    onSaveIncome = viewModel::saveIncome,
                    onDeleteIncome = viewModel::deleteIncome,
                    onSaveDebt = viewModel::saveDebt,
                    onPayDebtInstallment = viewModel::payDebtInstallment,
                    onDeleteDebt = viewModel::deleteDebt
                )
            }
            composable(AppDestination.PLAN.route) {
                PlanningScreen(
                    state = state,
                    onSaveGoal = viewModel::saveSavingsGoal,
                    onAddToGoal = viewModel::addToSavingsGoal,
                    onDeleteGoal = viewModel::deleteSavingsGoal
                )
            }
        }
    }
}
