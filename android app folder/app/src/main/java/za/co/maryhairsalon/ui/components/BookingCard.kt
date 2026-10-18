package za.co.maryhairsalon.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import za.co.maryhairsalon.data.model.Booking
import za.co.maryhairsalon.data.model.BookingType
import za.co.maryhairsalon.ui.theme.Brand

/**
 * A booking (website: booking-card). [showCustomer] shows who booked (stylist/manager views),
 * otherwise it shows the stylist (customer view). [actions] adds buttons at the bottom.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BookingCard(
    booking: Booking,
    showCustomer: Boolean = false,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    GlassCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(booking.serviceTitle.ifBlank { "Service" }, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            StatusBadge(booking.status)
        }
        if (booking.categoryLabel.isNotBlank()) {
            Text(booking.categoryLabel, style = MaterialTheme.typography.bodySmall, color = Brand.TextFaint)
        }
        Spacer(Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Meta(Icons.Default.DateRange, "${booking.dateFormatted} · ${booking.time}")
            Meta(
                Icons.Default.Person,
                if (showCustomer) "${booking.name} ${booking.surname}".trim() + " · ${booking.contact}"
                else booking.stylistName.ifBlank { "Stylist" },
            )
            Meta(
                Icons.Default.LocationOn,
                if (booking.type == BookingType.OUTCALL && booking.location.isNotBlank()) "Outcall — ${booking.location}" else booking.type.dbValue,
            )
        }
        if (booking.notes.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text("“${booking.notes}”", style = MaterialTheme.typography.bodySmall, color = Brand.TextDim)
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(formatZar(booking.total), color = Brand.OrangeLight, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (actions != null) Row(Modifier.wrapContentWidth(), content = actions)
        }
    }
}

@Composable
private fun Meta(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Brand.TextFaint, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = Brand.TextDim)
    }
}

/** Small text action used on booking cards. */
@Composable
fun CardAction(text: String, onClick: () -> Unit, danger: Boolean = false, color: Color? = null) {
    TextButton(onClick = onClick) {
        Text(text, color = color ?: if (danger) Brand.Danger else Brand.OrangeLight, fontWeight = FontWeight.SemiBold)
    }
}
