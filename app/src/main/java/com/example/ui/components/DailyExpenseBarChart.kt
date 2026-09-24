package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Warning
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
import com.example.data.model.DailyExpenseSummary
import com.example.data.model.formatRupiah
import com.example.ui.theme.BudgetWarning
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed

@Composable
fun DailyExpenseBarChart(
    dailyExpenses: List<DailyExpenseSummary>,
    monthDisplay: String,
    dailyAverage: Double,
    selectedDay: Int?,
    onSelectDay: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (dailyExpenses.isEmpty()) return

    val maxVal = remember(dailyExpenses) {
        dailyExpenses.maxOfOrNull { it.totalExpense }?.coerceAtLeast(50000.0) ?: 50000.0
    }

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(dailyExpenses) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(650))
    }

    val activeDaySummary = remember(selectedDay, dailyExpenses) {
        dailyExpenses.find { it.dayOfMonth == selectedDay }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_expense_bar_chart_card"),
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
                        text = "Pengeluaran Harian",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Sebaran $monthDisplay (Tgl 1 - ${dailyExpenses.size})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Average badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmeraldPrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Rata²: ${formatRupiah(dailyAverage)}/hari",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active Day Selected Information Banner
            if (activeDaySummary != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (activeDaySummary.isPeakDay) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = BudgetWarning,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = "Tanggal ${activeDaySummary.dayOfMonth} $monthDisplay" + if (activeDaySummary.isPeakDay) " (Paling Boros)" else "",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (activeDaySummary.isPeakDay) BudgetWarning else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = formatRupiah(activeDaySummary.totalExpense),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (activeDaySummary.totalExpense > dailyAverage) ExpenseRed else EmeraldPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
            val onSurfaceColor = MaterialTheme.colorScheme.onSurface
            val primaryColor = MaterialTheme.colorScheme.primary

            // Daily Chart Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .pointerInput(dailyExpenses) {
                        detectTapGestures { offset ->
                            val totalDays = dailyExpenses.size
                            if (totalDays == 0) return@detectTapGestures
                            val colWidth = size.width / totalDays
                            val index = (offset.x / colWidth).toInt().coerceIn(0, totalDays - 1)
                            val day = dailyExpenses[index].dayOfMonth
                            onSelectDay(if (selectedDay == day) null else day)
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val bottomPadding = 24.dp.toPx()
                val chartHeight = h - bottomPadding
                val count = dailyExpenses.size
                val colWidth = w / count
                val barWidth = (colWidth * 0.7f).coerceIn(4.dp.toPx(), 12.dp.toPx())

                // Draw Average Line
                if (maxVal > 0 && dailyAverage > 0) {
                    val avgFraction = (dailyAverage / maxVal).toFloat().coerceIn(0f, 1f)
                    val avgY = chartHeight * (1f - avgFraction)
                    drawLine(
                        color = EmeraldPrimary.copy(alpha = 0.6f),
                        start = Offset(0f, avgY),
                        end = Offset(w, avgY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )
                }

                // Render Day Bars
                dailyExpenses.forEachIndexed { i, item ->
                    val isSelected = (item.dayOfMonth == selectedDay)
                    val isPeak = item.isPeakDay && item.totalExpense > 0
                    val fraction = ((item.totalExpense / maxVal) * animProgress.value).toFloat().coerceIn(0f, 1f)

                    val barHeight = if (fraction > 0f) chartHeight * fraction else 2.dp.toPx()
                    val barTop = chartHeight - barHeight
                    val barLeft = i * colWidth + (colWidth - barWidth) / 2f

                    // Selection Glow / background
                    if (isSelected) {
                        drawRoundRect(
                            color = primaryColor.copy(alpha = 0.15f),
                            topLeft = Offset(i * colWidth, 0f),
                            size = Size(colWidth, chartHeight + 20.dp.toPx()),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }

                    val barColor = when {
                        isPeak -> BudgetWarning
                        item.totalExpense > dailyAverage -> ExpenseRed
                        item.totalExpense > 0 -> ExpenseRed.copy(alpha = 0.55f)
                        else -> surfaceVariantColor.copy(alpha = 0.4f)
                    }

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(barLeft, barTop),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                    )

                    // Draw Day Number for key intervals (1, 5, 10, 15, 20, 25, end) or selected
                    val shouldLabel = (item.dayOfMonth == 1 || item.dayOfMonth % 5 == 0 || item.dayOfMonth == count || isSelected)
                    if (shouldLabel) {
                        drawContext.canvas.nativeCanvas.apply {
                            val paint = android.graphics.Paint().apply {
                                color = if (isSelected) primaryColor.hashCode() else onSurfaceColor.copy(alpha = 0.6f).hashCode()
                                textSize = 9.sp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = isSelected
                                isAntiAlias = true
                            }
                            drawText(
                                item.dayOfMonth.toString(),
                                i * colWidth + colWidth / 2f,
                                h - 4.dp.toPx(),
                                paint
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chart Footer Legends
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(BudgetWarning))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Puncak Pengeluaran", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldPrimary))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Garis Rata-rata", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ExpenseRed))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("> Rata-rata", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
