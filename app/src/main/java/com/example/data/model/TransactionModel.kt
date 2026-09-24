package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tv
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ui.theme.CatBills
import com.example.ui.theme.CatBonus
import com.example.ui.theme.CatEducation
import com.example.ui.theme.CatEntertainment
import com.example.ui.theme.CatFood
import com.example.ui.theme.CatHealth
import com.example.ui.theme.CatInvestment
import com.example.ui.theme.CatOther
import com.example.ui.theme.CatSalary
import com.example.ui.theme.CatShopping
import com.example.ui.theme.CatTransport
import java.text.NumberFormat
import java.util.Locale

enum class TransactionType(val label: String) {
    EXPENSE("Pengeluaran"),
    INCOME("Pemasukan")
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "EXPENSE" or "INCOME"
    val category: String,
    val timestamp: Long,
    val wallet: String = "Tunai", // "Tunai", "Bank", "E-Wallet"
    val notes: String = ""
)

@Entity(tableName = "monthly_budgets")
data class BudgetEntity(
    @PrimaryKey
    val yearMonth: String, // "YYYY-MM", e.g. "2026-09"
    val monthlyLimit: Double
)

data class CategoryMeta(
    val name: String,
    val type: TransactionType,
    val icon: ImageVector,
    val color: Color
)

object CategoryRegistry {
    val expenseCategories = listOf(
        CategoryMeta("Makanan & Minuman", TransactionType.EXPENSE, Icons.Default.Fastfood, CatFood),
        CategoryMeta("Transportasi", TransactionType.EXPENSE, Icons.Default.DirectionsCar, CatTransport),
        CategoryMeta("Belanja", TransactionType.EXPENSE, Icons.Default.ShoppingBag, CatShopping),
        CategoryMeta("Tagihan & Utilitas", TransactionType.EXPENSE, Icons.Default.Receipt, CatBills),
        CategoryMeta("Hiburan", TransactionType.EXPENSE, Icons.Default.Tv, CatEntertainment),
        CategoryMeta("Kesehatan", TransactionType.EXPENSE, Icons.Default.LocalHospital, CatHealth),
        CategoryMeta("Pendidikan", TransactionType.EXPENSE, Icons.Default.School, CatEducation),
        CategoryMeta("Lain-lain", TransactionType.EXPENSE, Icons.Default.MoreHoriz, CatOther)
    )

    val incomeCategories = listOf(
        CategoryMeta("Gaji Pokok", TransactionType.INCOME, Icons.Default.AttachMoney, CatSalary),
        CategoryMeta("Bonus & Tunjangan", TransactionType.INCOME, Icons.Default.CardGiftcard, CatBonus),
        CategoryMeta("Investasi / Bunga", TransactionType.INCOME, Icons.Default.TrendingUp, CatInvestment),
        CategoryMeta("Pemasukan Lain", TransactionType.INCOME, Icons.Default.AccountBalance, CatOther)
    )

    fun getCategoryMeta(name: String, type: TransactionType = TransactionType.EXPENSE): CategoryMeta {
        val list = if (type == TransactionType.EXPENSE) expenseCategories else incomeCategories
        return list.find { it.name.equals(name, ignoreCase = true) }
            ?: (expenseCategories + incomeCategories).find { it.name.equals(name, ignoreCase = true) }
            ?: CategoryMeta(name, type, Icons.Default.MoreHoriz, CatOther)
    }
}

data class CategoryExpenseSummary(
    val category: String,
    val totalAmount: Double,
    val percentage: Float,
    val count: Int,
    val color: Color,
    val icon: ImageVector
)

data class MonthlyFinanceSummary(
    val yearMonth: String, // "YYYY-MM"
    val displayLabel: String, // "Sep 2026"
    val totalIncome: Double,
    val totalExpense: Double,
    val netSavings: Double,
    val isCurrentMonth: Boolean = false
)

data class DailyExpenseSummary(
    val dayOfMonth: Int, // 1..31
    val displayDay: String, // "23"
    val totalExpense: Double,
    val isPeakDay: Boolean = false
)

fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    format.maximumFractionDigits = 0
    val formatted = format.format(amount)
    // Replace "Rp" with "Rp " for standard neat spacing
    return if (formatted.startsWith("Rp") && !formatted.startsWith("Rp ")) {
        formatted.replaceFirst("Rp", "Rp ")
    } else {
        formatted
    }
}

fun formatCompactRupiah(amount: Double): String {
    return when {
        amount >= 1_000_000_000 -> String.format(Locale("id", "ID"), "%.1f M", amount / 1_000_000_000)
        amount >= 1_000_000 -> String.format(Locale("id", "ID"), "%.1f Jt", amount / 1_000_000)
        amount >= 1_000 -> String.format(Locale("id", "ID"), "%.0f Rb", amount / 1_000)
        else -> String.format(Locale("id", "ID"), "%.0f", amount)
    }
}
