package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MonthlyFinanceSummary
import com.example.data.model.formatRupiah
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

@Composable
fun CashFlowComparisonChart(
    monthlySummaries: List<MonthlyFinanceSummary>,
    modifier: Modifier = Modifier
) {
    if (monthlySummaries.isEmpty()) return

    val maxVal = remember(monthlySummaries) {
        monthlySummaries.maxOfOrNull { maxOf(it.totalExpense, it.totalIncome) }?.coerceAtLeast(100000.0) ?: 1000000.0
    }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(monthlySummaries) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(650))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cash_flow_comparison_chart_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Arus Kas Bulanan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Pemasukan vs Pengeluaran per Bulan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(IncomeGreen))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Masuk", style = MaterialTheme.typography.labelSmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ExpenseRed))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Keluar", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            val onSurfaceColor = MaterialTheme.colorScheme.onSurface
            val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val w = size.width
                val h = size.height
                val bottomPadding = 24.dp.toPx()
                val chartHeight = h - bottomPadding
                val count = monthlySummaries.size
                val slotWidth = w / count
                val barWidth = (slotWidth * 0.32f).coerceIn(8.dp.toPx(), 20.dp.toPx())

                monthlySummaries.forEachIndexed { i, item ->
                    val center = (i + 0.5f) * slotWidth

                    // Income bar (left)
                    val incFrac = ((item.totalIncome / maxVal) * animProgress.value).toFloat().coerceIn(0f, 1f)
                    val incHeight = chartHeight * incFrac
                    val incTop = chartHeight - incHeight
                    val incLeft = center - barWidth - 2.dp.toPx()

                    drawRoundRect(
                        color = IncomeGreen,
                        topLeft = Offset(incLeft, incTop),
                        size = Size(barWidth, incHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // Expense bar (right)
                    val expFrac = ((item.totalExpense / maxVal) * animProgress.value).toFloat().coerceIn(0f, 1f)
                    val expHeight = chartHeight * expFrac
                    val expTop = chartHeight - expHeight
                    val expLeft = center + 2.dp.toPx()

                    drawRoundRect(
                        color = ExpenseRed,
                        topLeft = Offset(expLeft, expTop),
                        size = Size(barWidth, expHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // X-axis label
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = onSurfaceColor.hashCode()
                            textSize = 10.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        drawText(item.displayLabel, center, h - 4.dp.toPx(), paint)
                    }
                }
            }
        }
    }
}
