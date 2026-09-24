package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.data.db.AppDatabase
import com.example.data.repository.FinanceRepository
import com.example.ui.screens.MainAppScaffold
import com.example.ui.theme.FinankuTheme
import com.example.ui.viewmodel.FinanceViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: FinanceViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = FinanceRepository(database.transactionDao())
        FinanceViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinankuTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}
