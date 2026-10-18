package za.co.maryhairsalon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import za.co.maryhairsalon.data.model.BookingStatus
import za.co.maryhairsalon.ui.theme.Brand
import java.util.Locale
import kotlin.math.abs

/* Reusable building blocks that mirror the website's look (glass cards, orange gradient buttons). */

/** "R495.00" — same format as the website. */
fun formatZar(amount: Double): String = "R" + String.format(Locale.US, "%.2f", amount)

/** The website's `.glass` panel: translucent fill, thin light border, rounded corners. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    highlighted: Boolean = false,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier
            .clip(shape)
            .background(if (highlighted) Brand.OrangeSoft else Brand.Glass)
            .border(1.dp, if (highlighted) Brand.Orange else Brand.GlassBorder, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        content = content,
    )
}

/** Primary call-to-action with the orange gradient (website: .auth-form__submit / .bm-submit). */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier
            .height(50.dp)
            .clip(shape)
            .background(if (enabled && !loading) Brand.ButtonGradient else androidx.compose.ui.graphics.SolidColor(Brand.GlassBorder))
            .clickable(enabled = enabled && !loading, onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

/** Secondary outlined button (website: ghost buttons / Google button). */
@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier
            .height(50.dp)
            .clip(shape)
            .background(Brand.Glass)
            .border(1.dp, Brand.GlassBorder, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(10.dp))
        }
        Text(text, fontWeight = FontWeight.SemiBold, color = if (enabled) Brand.Text else Brand.TextFaint)
    }
}

/** Crown mark + name, like the website's navigation logo. */
@Composable
fun BrandMark(modifier: Modifier = Modifier, large: Boolean = false) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text("♕", color = Brand.OrangeLight, fontSize = if (large) 34.sp else 22.sp)
        Spacer(Modifier.width(8.dp))
        Column {
            Text("Mary", style = if (large) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge)
            Text("HAIR SALON", style = MaterialTheme.typography.labelSmall, color = Brand.TextFaint)
        }
    }
}

/** Orange eyebrow + serif title (website: section heads). */
@Composable
fun SectionHeader(eyebrow: String, title: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelSmall, color = Brand.OrangeLight)
        Spacer(Modifier.height(4.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    error: String? = null,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    placeholder: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it, color = Brand.TextFaint) } },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        enabled = enabled,
        readOnly = readOnly,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(10.dp),
        colors = fieldColors(),
    )
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Password",
    modifier: Modifier = Modifier,
    error: String? = null,
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            TextButton(onClick = { visible = !visible }) {
                Text(if (visible) "Hide" else "Show", color = Brand.OrangeLight)
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        shape = RoundedCornerShape(10.dp),
        colors = fieldColors(),
    )
}

@Composable
fun SearchField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("Search styles, e.g. braids", color = Brand.TextFaint) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = if (value.isNotEmpty()) {
            { TextButton(onClick = { onValueChange("") }) { Text("Clear", color = Brand.OrangeLight) } }
        } else null,
        shape = RoundedCornerShape(24.dp),
        colors = fieldColors(),
    )
}

@Composable
fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Brand.Orange,
    unfocusedBorderColor = Brand.GlassBorder,
    focusedContainerColor = Brand.Glass,
    unfocusedContainerColor = Brand.Glass,
    disabledContainerColor = Brand.Glass,
    focusedLabelColor = Brand.OrangeLight,
    cursorColor = Brand.Orange,
    focusedLeadingIconColor = Brand.OrangeLight,
)

/** Coloured pill for a booking status (matches the website's booking-card status colours). */
@Composable
fun StatusBadge(status: BookingStatus) {
    val (bg, fg) = when (status) {
        BookingStatus.PENDING -> Color(0x33F2B33D) to Color(0xFFF2B33D)
        BookingStatus.UPCOMING -> Color(0x334A90D9) to Color(0xFF7DB3EA)
        BookingStatus.COMPLETED -> Color(0x332E9E6B) to Color(0xFF5CC593)
        BookingStatus.CANCELLED -> Color(0x33E0475A) to Color(0xFFEF7D8B)
    }
    Text(
        status.dbValue.replaceFirstChar { it.uppercase() },
        modifier = Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp),
        color = fg,
        style = MaterialTheme.typography.labelMedium,
    )
}

private val AVATAR_COLOURS = listOf(
    Color(0xFFD96B2B), Color(0xFF7C4DFF), Color(0xFF1E9D7A), Color(0xFFC2185B), Color(0xFF1976D2), Color(0xFF8D6E63),
)

/** Initials in a coloured circle (same idea as the website's stylist avatars). */
@Composable
fun InitialsAvatar(name: String, size: Dp = 48.dp, modifier: Modifier = Modifier) {
    val initials = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" }
    val colour = AVATAR_COLOURS[abs(name.hashCode()) % AVATAR_COLOURS.size]
    Box(modifier.size(size).clip(CircleShape).background(colour), contentAlignment = Alignment.Center) {
        Text(initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.38f).sp)
    }
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier, text: String? = null) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = Brand.Orange)
        if (text != null) {
            Spacer(Modifier.height(12.dp))
            Text(text, color = Brand.TextDim)
        }
    }
}

@Composable
fun EmptyState(title: String, message: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Column(
        modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("♕", color = Brand.OrangeGlow, fontSize = 40.sp)
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(message, color = Brand.TextDim, textAlign = TextAlign.Center)
        if (action != null) {
            Spacer(Modifier.height(16.dp))
            action()
        }
    }
}

/** Small round icon button with a glass background. */
@Composable
fun GlassIconButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.clip(CircleShape).background(Brand.Glass).border(1.dp, Brand.GlassBorder, CircleShape),
    ) {
        Icon(icon, contentDescription = contentDescription, tint = Brand.Text)
    }
}

/** A selectable chip-like box used for time slots, dates and booking type. */
@Composable
fun SelectBox(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    subtitle: String? = null,
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier
            .clip(shape)
            .background(
                when {
                    selected -> Brand.Orange
                    !enabled -> Color.Transparent
                    else -> Brand.Glass
                }
            )
            .border(1.dp, if (selected) Brand.Orange else Brand.GlassBorder, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text,
            color = when {
                selected -> Color.White
                !enabled -> Brand.TextFaint
                else -> Brand.Text
            },
            fontWeight = FontWeight.SemiBold,
            textDecoration = if (!enabled) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
        )
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = if (selected) Color.White else Brand.TextDim)
        }
    }
}
