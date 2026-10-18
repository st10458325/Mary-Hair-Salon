package za.co.maryhairsalon.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import za.co.maryhairsalon.ui.theme.Brand
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Month calendar like the website's booking modal: Monday-first, past days greyed out,
 * selected day filled orange.
 */
@Composable
fun MonthCalendar(
    month: YearMonth,
    selected: LocalDate?,
    onSelect: (LocalDate) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrevious, enabled = month.isAfter(YearMonth.from(today))) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
            }
            Text(
                "${month.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${month.year}",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            IconButton(onClick = onNext) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month")
            }
        }
        Row {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                Text(
                    it,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = Brand.TextFaint,
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        // Empty cells before the 1st (Monday = 0), then the days of the month.
        val leading = month.atDay(1).dayOfWeek.value - 1
        val cells: List<LocalDate?> = List(leading) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
        cells.chunked(7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(vertical = 2.dp)) {
                week.forEach { day -> DayCell(day, day != null && day == selected, day != null && !day.isBefore(today), onSelect, Modifier.weight(1f)) }
                repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Legend(Brand.Success, "Available")
            Legend(Brand.TextFaint, "Past")
            Legend(Brand.Orange, "Selected")
        }
    }
}

@Composable
private fun DayCell(day: LocalDate?, selected: Boolean, available: Boolean, onSelect: (LocalDate) -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier
            .aspectRatio(1f)
            .clip(shape)
            .then(
                when {
                    day == null -> Modifier
                    selected -> Modifier.background(Brand.Orange)
                    available -> Modifier.background(Brand.Success.copy(alpha = 0.12f)).border(1.dp, Brand.Success.copy(alpha = 0.35f), shape)
                    else -> Modifier
                }
            )
            .clickable(enabled = day != null && available) { day?.let(onSelect) },
        contentAlignment = Alignment.Center,
    ) {
        if (day != null) {
            Text(
                day.dayOfMonth.toString(),
                color = when {
                    selected -> Color.White
                    available -> Brand.Text
                    else -> Brand.TextFaint
                },
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun Legend(colour: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.padding(end = 4.dp).height(8.dp).aspectRatio(1f).clip(RoundedCornerShape(50)).background(colour))
        Text(label, style = MaterialTheme.typography.bodySmall, color = Brand.TextDim)
    }
}
