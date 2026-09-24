package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionType
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.SetBudgetDialog
import com.example.ui.viewmodel.FinanceViewModel

@Composable
fun MainAppScaffold(
    viewModel: FinanceViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var currentTab by remember { mutableIntStateOf(0) }

    var showAddDialog by remember { mutableStateOf(false) }
    var addDialogInitialType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var showBudgetDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 0) Icons.Default.Home else Icons.Outlined.Home,
                            contentDescription = "Ringkasan"
                        )
                    },
                    label = { Text("Ringkasan") },
                    modifier = Modifier.testTag("nav_home_tab"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                )

                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 1) Icons.Default.Assessment else Icons.Outlined.Assessment,
                            contentDescription = "Grafik"
                        )
                    },
                    label = { Text("Grafik") },
                    modifier = Modifier.testTag("nav_analytics_tab"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                )

                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ListAlt,
                            contentDescription = "Riwayat"
                        )
                    },
                    label = { Text("Riwayat") },
                    modifier = Modifier.testTag("nav_transactions_tab"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    addDialogInitialType = TransactionType.EXPENSE
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("main_add_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Catat Transaksi"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> HomeScreen(
                    state = state,
                    viewModel = viewModel,
                    onNavigateToAnalytics = { currentTab = 1 },
                    onNavigateToTransactions = { currentTab = 2 },
                    onOpenAddTransaction = { type ->
                        addDialogInitialType = type
                        showAddDialog = true
                    },
                    onOpenEditBudget = { showBudgetDialog = true }
                )
                1 -> AnalyticsScreen(
                    state = state,
                    viewModel = viewModel
                )
                2 -> TransactionsScreen(
                    state = state,
                    viewModel = viewModel
                )
            }
        }
    }

    // Add Transaction Dialog
    if (showAddDialog) {
        AddTransactionDialog(
            initialType = addDialogInitialType,
            onDismiss = { showAddDialog = false },
            onSave = { title, amount, type, category, wallet, notes ->
                viewModel.addTransaction(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    wallet = wallet,
                    notes = notes
                )
                showAddDialog = false
            }
        )
    }

    // Set Budget Dialog
    if (showBudgetDialog) {
        SetBudgetDialog(
            monthDisplay = state.selectedMonthDisplay,
            currentBudget = state.monthlyBudget,
            onDismiss = { showBudgetDialog = false },
            onSave = { newBudget ->
                viewModel.setMonthlyBudget(newBudget)
                showBudgetDialog = false
            }
        )
    }
}
