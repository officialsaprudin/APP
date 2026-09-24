package com.example.data.db

import com.example.data.model.BudgetEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import java.util.Calendar

object InitialDataLoader {
    fun generateInitialData(): Pair<List<TransactionEntity>, List<BudgetEntity>> {
        val transactions = mutableListOf<TransactionEntity>()
        val budgets = mutableListOf<BudgetEntity>()

        val calendar = Calendar.getInstance()
        // Ensure calendar starts at current time
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) // 0-indexed

        // Create data for the last 6 months (5 months prior up to current month)
        for (monthOffset in 5 downTo 0) {
            val mCal = Calendar.getInstance()
            mCal.set(Calendar.YEAR, currentYear)
            mCal.set(Calendar.MONTH, currentMonth - monthOffset)
            val y = mCal.get(Calendar.YEAR)
            val m = mCal.get(Calendar.MONTH) + 1
            val ymKey = String.format("%04d-%02d", y, m)

            // Monthly budget of 5.500.000
            budgets.add(BudgetEntity(yearMonth = ymKey, monthlyLimit = 5500000.0))

            // Income: Salary on day 25 or day 1
            val salaryCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, m - 1)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
            }
            transactions.add(
                TransactionEntity(
                    title = "Gaji Bulanan",
                    amount = 8500000.0,
                    type = TransactionType.INCOME.name,
                    category = "Gaji Pokok",
                    timestamp = salaryCal.timeInMillis,
                    wallet = "Bank",
                    notes = "Transfer gaji bulanan"
                )
            )

            // Freelance / Bonus occasionally
            if (monthOffset % 2 == 1) {
                val bonusCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, y)
                    set(Calendar.MONTH, m - 1)
                    set(Calendar.DAY_OF_MONTH, 14)
                    set(Calendar.HOUR_OF_DAY, 14)
                }
                transactions.add(
                    TransactionEntity(
                        title = "Proyek Sampingan Desain",
                        amount = 1750000.0,
                        type = TransactionType.INCOME.name,
                        category = "Bonus & Tunjangan",
                        timestamp = bonusCal.timeInMillis,
                        wallet = "E-Wallet",
                        notes = "Freelance UI design"
                    )
                )
            }

            // Fixed monthly expenses
            val billCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, m - 1)
                set(Calendar.DAY_OF_MONTH, 5)
                set(Calendar.HOUR_OF_DAY, 10)
            }
            transactions.add(
                TransactionEntity(
                    title = "Tagihan Listrik PLN",
                    amount = 385000.0,
                    type = TransactionType.EXPENSE.name,
                    category = "Tagihan & Utilitas",
                    timestamp = billCal.timeInMillis,
                    wallet = "Bank",
                    notes = "Token listrik rumah"
                )
            )
            transactions.add(
                TransactionEntity(
                    title = "Internet Wi-Fi Fiber",
                    amount = 350000.0,
                    type = TransactionType.EXPENSE.name,
                    category = "Tagihan & Utilitas",
                    timestamp = billCal.timeInMillis + 3600000L,
                    wallet = "Bank",
                    notes = "IndiHome bulanan"
                )
            )

            // Dynamic days for expenses
            val daysInMonth = mCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val maxDayToGenerate = if (monthOffset == 0) {
                // Current month: up to today's day
                calendar.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
            } else {
                daysInMonth
            }

            // Food & Drink expenses on various days
            val foodDays = listOf(2, 4, 7, 9, 12, 15, 18, 20, 22, 26, 28)
            for (fd in foodDays) {
                if (fd <= maxDayToGenerate) {
                    val c = Calendar.getInstance().apply {
                        set(Calendar.YEAR, y)
                        set(Calendar.MONTH, m - 1)
                        set(Calendar.DAY_OF_MONTH, fd)
                        set(Calendar.HOUR_OF_DAY, 12)
                    }
                    val nominal = when (fd % 3) {
                        0 -> 125000.0 // Supermarket groceries
                        1 -> 45000.0  // Lunch
                        else -> 78000.0 // Dinner
                    }
                    val title = when (fd % 3) {
                        0 -> "Belanja Sayur & Buah"
                        1 -> "Makan Siang Kantor"
                        else -> "Makan Malam & Kopi"
                    }
                    transactions.add(
                        TransactionEntity(
                            title = title,
                            amount = nominal,
                            type = TransactionType.EXPENSE.name,
                            category = "Makanan & Minuman",
                            timestamp = c.timeInMillis,
                            wallet = if (fd % 2 == 0) "E-Wallet" else "Tunai"
                        )
                    )
                }
            }

            // Transport expenses
            val transportDays = listOf(3, 8, 14, 19, 24)
            for (td in transportDays) {
                if (td <= maxDayToGenerate) {
                    val c = Calendar.getInstance().apply {
                        set(Calendar.YEAR, y)
                        set(Calendar.MONTH, m - 1)
                        set(Calendar.DAY_OF_MONTH, td)
                        set(Calendar.HOUR_OF_DAY, 8)
                    }
                    transactions.add(
                        TransactionEntity(
                            title = if (td % 2 == 0) "Bensin Pertamax" else "Ojek Online",
                            amount = if (td % 2 == 0) 100000.0 else 32000.0,
                            type = TransactionType.EXPENSE.name,
                            category = "Transportasi",
                            timestamp = c.timeInMillis,
                            wallet = "E-Wallet"
                        )
                    )
                }
            }

            // Shopping expenses
            val shoppingDays = listOf(6, 17)
            for (sd in shoppingDays) {
                if (sd <= maxDayToGenerate) {
                    val c = Calendar.getInstance().apply {
                        set(Calendar.YEAR, y)
                        set(Calendar.MONTH, m - 1)
                        set(Calendar.DAY_OF_MONTH, sd)
                        set(Calendar.HOUR_OF_DAY, 16)
                    }
                    transactions.add(
                        TransactionEntity(
                            title = if (sd == 6) "Kebutuhan Rumah Tangga" else "Baju & Sepatu",
                            amount = if (sd == 6) 420000.0 else 275000.0,
                            type = TransactionType.EXPENSE.name,
                            category = "Belanja",
                            timestamp = c.timeInMillis,
                            wallet = "Bank"
                        )
                    )
                }
            }

            // Entertainment
            if (11 <= maxDayToGenerate) {
                val c = Calendar.getInstance().apply {
                    set(Calendar.YEAR, y)
                    set(Calendar.MONTH, m - 1)
                    set(Calendar.DAY_OF_MONTH, 11)
                    set(Calendar.HOUR_OF_DAY, 19)
                }
                transactions.add(
                    TransactionEntity(
                        title = "Nonton Bioskop & Snack",
                        amount = 135000.0,
                        type = TransactionType.EXPENSE.name,
                        category = "Hiburan",
                        timestamp = c.timeInMillis,
                        wallet = "E-Wallet"
                    )
                )
            }

            // Health
            if (16 <= maxDayToGenerate) {
                val c = Calendar.getInstance().apply {
                    set(Calendar.YEAR, y)
                    set(Calendar.MONTH, m - 1)
                    set(Calendar.DAY_OF_MONTH, 16)
                    set(Calendar.HOUR_OF_DAY, 11)
                }
                transactions.add(
                    TransactionEntity(
                        title = "Vitamin C & Suplemen",
                        amount = 160000.0,
                        type = TransactionType.EXPENSE.name,
                        category = "Kesehatan",
                        timestamp = c.timeInMillis,
                        wallet = "Tunai"
                    )
                )
            }
        }

        return Pair(transactions, budgets)
    }
}
