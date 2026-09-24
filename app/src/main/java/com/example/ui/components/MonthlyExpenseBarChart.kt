package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MonthlyFinanceSummary
import com.example.data.model.formatCompactRupiah
import com.example.data.model.formatRupiah
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

@Composable
fun MonthlyExpenseBarChart(
    monthlySummaries: List<MonthlyFinanceSummary>,
    selectedYearMonth: String,
    onSelectMonth: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (monthlySummaries.isEmpty()) return

    val maxVal = remember(monthlySummaries) {
        monthlySummaries.maxOfOrNull { maxOf(it.totalExpense, it.totalIncome) }?.coerceAtLeast(100000.0) ?: 1000000.0
    }

    var activeTooltipMonth by remember(selectedYearMonth) {
        mutableStateOf(monthlySummaries.find { it.yearMonth == selectedYearMonth } ?: monthlySummaries.lastOrNull())
    }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(monthlySummaries) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(700))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_expense_bar_chart_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                        text = "Grafik Tren Pengeluaran",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Perbandingan 6 Bulan Terakhir",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Legend
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ExpenseRed)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Pengeluaran",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(IncomeGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Pemasukan",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active Tooltip Banner
            activeTooltipMonth?.let { summary ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = summary.displayLabel + if (summary.yearMonth == selectedYearMonth) " (Bulan Terpilih)" else "",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Pengeluaran: ${formatRupiah(summary.totalExpense)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ExpenseRed
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Pemasukan: ${formatRupiah(summary.totalIncome)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = IncomeGreen
                            )
                            val net = summary.netSavings
                            Text(
                                text = (if (net >= 0) "Surplus: " else "Defisit: ") + formatRupiah(kotlin.math.abs(net)),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (net >= 0) IncomeGreen else ExpenseRed
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
            val onSurfaceColor = MaterialTheme.colorScheme.onSurface
            val primaryColor = MaterialTheme.colorScheme.primary

            // Interactive Canvas Bar Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .pointerInput(monthlySummaries) {
                            detectTapGestures { offset ->
                                val count = monthlySummaries.size
                                if (count == 0) return@detectTapGestures
                                val itemWidth = size.width / count
                                val index = (offset.x / itemWidth).toInt().coerceIn(0, count - 1)
                                val tapped = monthlySummaries[index]
                                activeTooltipMonth = tapped
                                onSelectMonth(tapped.yearMonth)
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val bottomPadding = 32.dp.toPx()
                    val chartHeight = h - bottomPadding
                    val count = monthlySummaries.size
                    val slotWidth = w / count

                    // Draw 3 horizontal gridlines
                    val gridLevels = listOf(1f, 0.66f, 0.33f)
                    val strokePathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                    for (level in gridLevels) {
                        val yPos = chartHeight * (1f - level)
                        drawLine(
                            color = surfaceVariantColor.copy(alpha = 0.5f),
                            start = Offset(0f, yPos),
                            end = Offset(w, yPos),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = strokePathEffect
                        )
                    }

                    // Draw Bars for each month
                    monthlySummaries.forEachIndexed { i, summary ->
                        val slotCenter = (i + 0.5f) * slotWidth
                        val barWidth = (slotWidth * 0.28f).coerceIn(12.dp.toPx(), 28.dp.toPx())
                        val isSelected = summary.yearMonth == selectedYearMonth

                        // Selection highlight pillar
                        if (isSelected) {
                            drawRoundRect(
                                color = primaryColor.copy(alpha = 0.08f),
                                topLeft = Offset(i * slotWidth + 4.dp.toPx(), 0f),
                                size = Size(slotWidth - 8.dp.toPx(), chartHeight + 24.dp.toPx()),
                                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                            )
                        }

                        // Expense Bar (Primary)
                        val expFraction = ((summary.totalExpense / maxVal) * animProgress.value).toFloat().coerceIn(0.02f, 1f)
                        val expBarHeight = chartHeight * expFraction
                        val expTop = chartHeight - expBarHeight
                        val expLeft = slotCenter - barWidth - 2.dp.toPx()

                        val expBrush = Brush.verticalGradient(
                            colors = if (isSelected) {
                                listOf(ExpenseRed, ExpenseRed.copy(alpha = 0.85f))
                            } else {
                                listOf(ExpenseRed.copy(alpha = 0.7f), ExpenseRed.copy(alpha = 0.4f))
                            }
                        )

                        drawRoundRect(
                            brush = expBrush,
                            topLeft = Offset(expLeft, expTop),
                            size = Size(barWidth, expBarHeight),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        // Income Bar (Secondary)
                        val incFraction = ((summary.totalIncome / maxVal) * animProgress.value).toFloat().coerceIn(0.02f, 1f)
                        val incBarHeight = chartHeight * incFraction
                        val incTop = chartHeight - incBarHeight
                        val incLeft = slotCenter + 2.dp.toPx()

                        val incBrush = Brush.verticalGradient(
                            colors = if (isSelected) {
                                listOf(IncomeGreen, IncomeGreen.copy(alpha = 0.85f))
                            } else {
                                listOf(IncomeGreen.copy(alpha = 0.7f), IncomeGreen.copy(alpha = 0.4f))
                            }
                        )

                        drawRoundRect(
                            brush = incBrush,
                            topLeft = Offset(incLeft, incTop),
                            size = Size(barWidth, incBarHeight),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        // X-axis label (Month Name)
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = if (isSelected) primaryColor.hashCode() else onSurfaceColor.hashCode()
                                textSize = 11.sp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = isSelected
                                isAntiAlias = true
                            }
                            drawText(
                                summary.displayLabel,
                                slotCenter,
                                h - 8.dp.toPx(),
                                paint
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Ketuk salah satu batang bulan untuk menganalisis bulan tersebut",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
