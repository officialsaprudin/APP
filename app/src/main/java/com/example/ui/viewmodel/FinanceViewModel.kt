package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryExpenseSummary
import com.example.data.model.CategoryRegistry
import com.example.data.model.DailyExpenseSummary
import com.example.data.model.MonthlyFinanceSummary
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class FinanceUiState(
    val allTransactions: List<TransactionEntity> = emptyList(),
    val availableMonths: List<String> = emptyList(), // "YYYY-MM"
    val selectedYearMonth: String = "", // "YYYY-MM"
    val selectedMonthDisplay: String = "", // "September 2026"
    val selectedMonthTransactions: List<TransactionEntity> = emptyList(),
    // Summary for selected month
    val totalIncomeSelectedMonth: Double = 0.0,
    val totalExpenseSelectedMonth: Double = 0.0,
    val netSavingsSelectedMonth: Double = 0.0,
    val dailyAverageExpense: Double = 0.0,
    val peakExpenseDay: Pair<Int, Double>? = null, // Day to amount
    val monthlyBudget: Double = 5000000.0,
    val budgetUsagePercent: Float = 0f,
    // Overall balance
    val totalBalance: Double = 0.0,
    val totalAllTimeIncome: Double = 0.0,
    val totalAllTimeExpense: Double = 0.0,
    // Chart Models
    val monthlyTrendList: List<MonthlyFinanceSummary> = emptyList(),
    val dailyExpenseList: List<DailyExpenseSummary> = emptyList(),
    val categorySummaryList: List<CategoryExpenseSummary> = emptyList(),
    val topSpendingCategory: CategoryExpenseSummary? = null,
    val expenseDifferenceVsPrevMonth: Double? = null, // negative is savings
    val expensePercentageDiffVsPrevMonth: Float? = null,
    // UI Filters
    val searchQuery: String = "",
    val filterType: TransactionType? = null,
    val filterCategory: String? = null,
    val selectedDailyDay: Int? = null,
    val selectedCategoryDetail: String? = null,
    val isInitialized: Boolean = false
)

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {

    private val _selectedYearMonth = MutableStateFlow(getCurrentYearMonth())
    private val _searchQuery = MutableStateFlow("")
    private val _filterType = MutableStateFlow<TransactionType?>(null)
    private val _filterCategory = MutableStateFlow<String?>(null)
    private val _selectedDailyDay = MutableStateFlow<Int?>(null)
    private val _selectedCategoryDetail = MutableStateFlow<String?>(null)
    private val _budgetMap = MutableStateFlow<Map<String, Double>>(emptyMap())

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    val uiState: StateFlow<FinanceUiState> = combine(
        repository.allTransactions,
        _selectedYearMonth,
        _searchQuery,
        _filterType,
        _filterCategory
    ) { txs, selectedYM, query, fType, fCat ->
        computeUiState(txs, selectedYM, query, fType, fCat)
    }.combine(_budgetMap) { state, budgetMap ->
        val currentBudget = budgetMap[state.selectedYearMonth] ?: 5500000.0
        val usage = if (currentBudget > 0) ((state.totalExpenseSelectedMonth / currentBudget) * 100).toFloat() else 0f
        state.copy(
            monthlyBudget = currentBudget,
            budgetUsagePercent = usage.coerceAtLeast(0f)
        )
    }.combine(_selectedDailyDay) { state, day ->
        state.copy(selectedDailyDay = day)
    }.combine(_selectedCategoryDetail) { state, cat ->
        state.copy(selectedCategoryDetail = cat)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinanceUiState()
    )

    private fun computeUiState(
        txs: List<TransactionEntity>,
        selectedYM: String,
        query: String,
        fType: TransactionType?,
        fCat: String?
    ): FinanceUiState {
        val ymFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val displayMonthFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        val cal = Calendar.getInstance()

        // Discover all unique months available in transactions, ensure selectedYM is present
        val monthSet = sortedSetOf(Comparator.reverseOrder<String>())
        monthSet.add(getCurrentYearMonth())
        txs.forEach { tx ->
            monthSet.add(ymFormat.format(tx.timestamp))
        }
        val availableMonths = monthSet.toList()

        val effectiveYM = if (selectedYM in availableMonths) selectedYM else availableMonths.firstOrNull() ?: getCurrentYearMonth()

        // Filter transactions for the selected month
        val selectedMonthTxs = txs.filter { tx ->
            ymFormat.format(tx.timestamp) == effectiveYM
        }

        var totalIncMonth = 0.0
        var totalExpMonth = 0.0
        selectedMonthTxs.forEach { tx ->
            if (tx.type == TransactionType.INCOME.name) totalIncMonth += tx.amount
            else totalExpMonth += tx.amount
        }
        val netMonth = totalIncMonth - totalExpMonth

        // Calculate all-time totals
        var allTimeInc = 0.0
        var allTimeExp = 0.0
        txs.forEach { tx ->
            if (tx.type == TransactionType.INCOME.name) allTimeInc += tx.amount
            else allTimeExp += tx.amount
        }
        val totalBal = allTimeInc - allTimeExp

        // Daily breakdown for selected month
        val parsedCal = parseYearMonth(effectiveYM)
        val daysInMonth = parsedCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dayExpenses = DoubleArray(daysInMonth + 1)
        selectedMonthTxs.filter { it.type == TransactionType.EXPENSE.name }.forEach { tx ->
            cal.timeInMillis = tx.timestamp
            val day = cal.get(Calendar.DAY_OF_MONTH)
            if (day in 1..daysInMonth) {
                dayExpenses[day] += tx.amount
            }
        }

        var maxExpense = 0.0
        var peakDay = 1
        for (d in 1..daysInMonth) {
            if (dayExpenses[d] > maxExpense) {
                maxExpense = dayExpenses[d]
                peakDay = d
            }
        }
        val peakPair = if (maxExpense > 0) Pair(peakDay, maxExpense) else null

        val currentDay = if (effectiveYM == getCurrentYearMonth()) Calendar.getInstance().get(Calendar.DAY_OF_MONTH) else daysInMonth
        val dailyAvg = if (currentDay > 0) totalExpMonth / currentDay else 0.0

        val dailyExpenseList = (1..daysInMonth).map { d ->
            DailyExpenseSummary(
                dayOfMonth = d,
                displayDay = d.toString(),
                totalExpense = dayExpenses[d],
                isPeakDay = (d == peakDay && maxExpense > 0)
            )
        }

        // Category breakdown for selected month
        val categoryTotals = mutableMapOf<String, Pair<Double, Int>>()
        selectedMonthTxs.filter { it.type == TransactionType.EXPENSE.name }.forEach { tx ->
            val prev = categoryTotals[tx.category] ?: Pair(0.0, 0)
            categoryTotals[tx.category] = Pair(prev.first + tx.amount, prev.second + 1)
        }
        val categorySummaries = categoryTotals.map { (catName, pair) ->
            val (amount, count) = pair
            val meta = CategoryRegistry.getCategoryMeta(catName, TransactionType.EXPENSE)
            val pct = if (totalExpMonth > 0) ((amount / totalExpMonth) * 100).toFloat() else 0f
            CategoryExpenseSummary(
                category = catName,
                totalAmount = amount,
                percentage = pct,
                count = count,
                color = meta.color,
                icon = meta.icon
            )
        }.sortedByDescending { it.totalAmount }

        // Monthly trends across up to 6 months
        val recentMonths = availableMonths.take(6).reversed()
        val monthlyTrendList = recentMonths.map { ym ->
            val mCal = parseYearMonth(ym)
            val label = SimpleDateFormat("MMM yy", Locale("id", "ID")).format(mCal.time)
            var inc = 0.0
            var exp = 0.0
            txs.filter { ymFormat.format(it.timestamp) == ym }.forEach { tx ->
                if (tx.type == TransactionType.INCOME.name) inc += tx.amount
                else exp += tx.amount
            }
            MonthlyFinanceSummary(
                yearMonth = ym,
                displayLabel = label,
                totalIncome = inc,
                totalExpense = exp,
                netSavings = inc - exp,
                isCurrentMonth = (ym == effectiveYM)
            )
        }

        // Comparison vs previous month
        val prevYM = getPreviousYearMonth(effectiveYM)
        val prevMonthExp = txs.filter { ymFormat.format(it.timestamp) == prevYM && it.type == TransactionType.EXPENSE.name }
            .sumOf { it.amount }
        val diffVsPrev = if (prevMonthExp > 0) totalExpMonth - prevMonthExp else null
        val diffPct = if (prevMonthExp > 0) (((totalExpMonth - prevMonthExp) / prevMonthExp) * 100).toFloat() else null

        // Format selected month display
        val selectedDisplay = try {
            displayMonthFormat.format(parseYearMonth(effectiveYM).time).replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            effectiveYM
        }

        // Apply filters to transactions list
        var filteredList = selectedMonthTxs
        if (!query.isBlank()) {
            filteredList = filteredList.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                it.notes.contains(query, ignoreCase = true) ||
                it.wallet.contains(query, ignoreCase = true)
            }
        }
        if (fType != null) {
            filteredList = filteredList.filter { it.type == fType.name }
        }
        if (fCat != null) {
            filteredList = filteredList.filter { it.category.equals(fCat, ignoreCase = true) }
        }

        return FinanceUiState(
            allTransactions = txs,
            availableMonths = availableMonths,
            selectedYearMonth = effectiveYM,
            selectedMonthDisplay = selectedDisplay,
            selectedMonthTransactions = filteredList,
            totalIncomeSelectedMonth = totalIncMonth,
            totalExpenseSelectedMonth = totalExpMonth,
            netSavingsSelectedMonth = netMonth,
            dailyAverageExpense = dailyAvg,
            peakExpenseDay = peakPair,
            totalBalance = totalBal,
            totalAllTimeIncome = allTimeInc,
            totalAllTimeExpense = allTimeExp,
            monthlyTrendList = monthlyTrendList,
            dailyExpenseList = dailyExpenseList,
            categorySummaryList = categorySummaries,
            topSpendingCategory = categorySummaries.firstOrNull(),
            expenseDifferenceVsPrevMonth = diffVsPrev,
            expensePercentageDiffVsPrevMonth = diffPct,
            searchQuery = query,
            filterType = fType,
            filterCategory = fCat,
            isInitialized = true
        )
    }

    fun selectMonth(yearMonth: String) {
        _selectedYearMonth.value = yearMonth
        _selectedDailyDay.value = null
        _selectedCategoryDetail.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterType(type: TransactionType?) {
        _filterType.value = type
    }

    fun setFilterCategory(category: String?) {
        _filterCategory.value = category
    }

    fun selectDailyDay(day: Int?) {
        _selectedDailyDay.value = day
    }

    fun selectCategoryDetail(category: String?) {
        _selectedCategoryDetail.value = category
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        timestamp: Long = System.currentTimeMillis(),
        wallet: String = "Tunai",
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    title = title,
                    amount = amount,
                    type = type.name,
                    category = category,
                    timestamp = timestamp,
                    wallet = wallet,
                    notes = notes
                )
            )
        }
    }

    fun updateTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(tx)
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
        }
    }

    fun deleteTransactionById(id: Long) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
        }
    }

    fun setMonthlyBudget(limit: Double) {
        val ym = _selectedYearMonth.value
        val map = _budgetMap.value.toMutableMap()
        map[ym] = limit
        _budgetMap.value = map
        viewModelScope.launch {
            repository.setBudget(ym, limit)
        }
    }

    private fun getCurrentYearMonth(): String {
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(System.currentTimeMillis())
    }

    private fun parseYearMonth(ym: String): Calendar {
        val parts = ym.split("-")
        val cal = Calendar.getInstance()
        if (parts.size >= 2) {
            val year = parts[0].toIntOrNull() ?: cal.get(Calendar.YEAR)
            val month = (parts[1].toIntOrNull() ?: 1) - 1
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, 1)
        }
        return cal
    }

    private fun getPreviousYearMonth(ym: String): String {
        val cal = parseYearMonth(ym)
        cal.add(Calendar.MONTH, -1)
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(cal.time)
    }

    class Factory(private val repository: FinanceRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FinanceViewModel(repository) as T
        }
    }
}
