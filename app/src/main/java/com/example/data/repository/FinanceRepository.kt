package com.example.data.repository

import com.example.data.db.InitialDataLoader
import com.example.data.db.TransactionDao
import com.example.data.model.BudgetEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FinanceRepository(private val dao: TransactionDao) {
    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()

    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        val count = dao.getTransactionCount()
        if (count == 0) {
            val (txs, budgets) = InitialDataLoader.generateInitialData()
            dao.insertTransactions(txs)
            budgets.forEach { dao.setBudget(it) }
        }
    }

    suspend fun insertTransaction(transaction: TransactionEntity): Long = withContext(Dispatchers.IO) {
        dao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        dao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        dao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteTransactionById(id)
    }

    fun getBudgetForMonth(yearMonth: String): Flow<BudgetEntity?> = dao.getBudgetForMonth(yearMonth)

    suspend fun setBudget(yearMonth: String, limit: Double) = withContext(Dispatchers.IO) {
        dao.setBudget(BudgetEntity(yearMonth = yearMonth, monthlyLimit = limit))
    }
}
