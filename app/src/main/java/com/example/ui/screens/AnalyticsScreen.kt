package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.formatRupiah
import com.example.ui.components.CashFlowComparisonChart
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.DailyExpenseBarChart
import com.example.ui.components.MonthlyExpenseBarChart
import com.example.ui.theme.BudgetWarning
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.FinanceUiState
import com.example.ui.viewmodel.FinanceViewModel
import java.util.Locale

@Composable
fun AnalyticsScreen(
    state: FinanceUiState,
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    var selectedChartTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tren Bulanan", "Sebaran Harian", "Kategori", "Arus Kas")
    var monthMenuOpen by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Month Picker Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Grafik & Analisis Keuangan",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Laporan komprehensif pengeluaran Anda",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { monthMenuOpen = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("analytics_month_picker"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = state.selectedMonthDisplay,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    DropdownMenu(
                        expanded = monthMenuOpen,
                        onDismissRequest = { monthMenuOpen = false }
                    ) {
                        state.availableMonths.forEach { ym ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = ym,
                                        fontWeight = if (ym == state.selectedYearMonth) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    viewModel.selectMonth(ym)
                                    monthMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // 3 Key Monthly Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Total Expense
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Total Keluar",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatRupiah(state.totalExpenseSelectedMonth),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${state.selectedMonthTransactions.filter { it.type == "EXPENSE" }.size} transaksi",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Card 2: Daily Average
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Rata² / Hari",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatRupiah(state.dailyAverageExpense),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Laju belanja",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Card 3: Peak Day
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Hari Terboros",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val peak = state.peakExpenseDay
                        Text(
                            text = if (peak != null) "Tgl ${peak.first}" else "-",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = BudgetWarning
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (peak != null) formatRupiah(peak.second) else "Belum ada",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Sub-chart Navigation Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedChartTab,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {},
                indicator = { tabPositions ->
                    if (selectedChartTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedChartTab]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedChartTab == index,
                        onClick = { selectedChartTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedChartTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedChartTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        }

        // Main Selected Chart Content
        item {
            when (selectedChartTab) {
                0 -> {
                    MonthlyExpenseBarChart(
                        monthlySummaries = state.monthlyTrendList,
                        selectedYearMonth = state.selectedYearMonth,
                        onSelectMonth = { viewModel.selectMonth(it) }
                    )
                }
                1 -> {
                    DailyExpenseBarChart(
                        dailyExpenses = state.dailyExpenseList,
                        monthDisplay = state.selectedMonthDisplay,
                        dailyAverage = state.dailyAverageExpense,
                        selectedDay = state.selectedDailyDay,
                        onSelectDay = { viewModel.selectDailyDay(it) }
                    )
                }
                2 -> {
                    CategoryDonutChart(
                        categories = state.categorySummaryList,
                        totalExpense = state.totalExpenseSelectedMonth,
                        selectedCategory = state.selectedCategoryDetail,
                        onSelectCategory = { viewModel.selectCategoryDetail(it) }
                    )
                }
                3 -> {
                    CashFlowComparisonChart(
                        monthlySummaries = state.monthlyTrendList
                    )
                }
            }
        }

        // Informative Financial Insights & Highlights Section
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("financial_insights_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Wawasan Keuangan Cerdas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Insight 1: Top Category
                    state.topSpendingCategory?.let { topCat ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = topCat.color,
                                modifier = Modifier.size(18.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pengeluaran terbanyak Anda berada di pos ${topCat.category} (${String.format(Locale("id", "ID"), "%.1f%%", topCat.percentage)} atau ${formatRupiah(topCat.totalAmount)}). Pertimbangkan untuk mengevaluasi pengeluaran ini agar target tabungan tercapai.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Insight 2: Savings Rate
                    if (state.totalIncomeSelectedMonth > 0) {
                        val savingsRate = ((state.netSavingsSelectedMonth / state.totalIncomeSelectedMonth) * 100).toFloat()
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (savingsRate >= 20f) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (savingsRate >= 20f) IncomeGreen else BudgetWarning,
                                modifier = Modifier.size(18.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (savingsRate >= 20f) {
                                    "Tingkat Tabungan Bulan Ini: ${String.format(Locale("id", "ID"), "%.1f%%", savingsRate)} (Sangat Sehat). Anda berhasil menyisihkan lebih dari rekomendasi ideal 20% dari total pemasukan."
                                } else if (savingsRate > 0f) {
                                    "Tingkat Tabungan Bulan Ini: ${String.format(Locale("id", "ID"), "%.1f%%", savingsRate)}. Sisa surplus Anda masih positif, namun usahakan mendekati target minimal 20%."
                                } else {
                                    "Perhatian: Pengeluaran bulan ini melebihi pemasukan (Defisit ${formatRupiah(kotlin.math.abs(state.netSavingsSelectedMonth))}). Kurangi pos belanja sekunder untuk memulihkan arus kas."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
