package za.co.maryhairsalon.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/*
 * Same design tokens as the website (style.css): near-black background, frosted "glass"
 * surfaces and a burnt-orange accent. Headings use a serif face (the website uses
 * Playfair Display), body text the default sans-serif (the website uses Inter).
 */
object Brand {
    val Bg = Color(0xFF0B0908)
    val BgElevated = Color(0xFF151110)
    val Glass = Color(0x0EFFFFFF)          // rgba(255,255,255,0.055)
    val GlassHover = Color(0x17FFFFFF)     // rgba(255,255,255,0.09)
    val GlassBorder = Color(0x1FFFFFFF)    // rgba(255,255,255,0.12)
    val Orange = Color(0xFFE8551C)
    val OrangeLight = Color(0xFFFF8A4C)
    val OrangeSoft = Color(0x29E8551C)     // rgba(232,85,28,0.16)
    val OrangeGlow = Color(0x59E8551C)
    val Text = Color(0xFFF7F1EA)
    val TextDim = Color(0xFFCFC4BA)
    val TextFaint = Color(0xFF8F8378)
    val Success = Color(0xFF2E9E6B)
    val Info = Color(0xFF4A90D9)
    val Danger = Color(0xFFE0475A)

    val ButtonGradient = Brush.linearGradient(listOf(OrangeLight, Orange))
    val GlowGradient = Brush.radialGradient(listOf(OrangeGlow, Color.Transparent))
}

private val Display = FontFamily.Serif

private val AppTypography = Typography(
    displaySmall = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 40.sp),
    headlineLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.5.sp),
)

private val AppColors = darkColorScheme(
    primary = Brand.Orange,
    onPrimary = Color.White,
    primaryContainer = Brand.OrangeSoft,
    onPrimaryContainer = Brand.OrangeLight,
    secondary = Brand.OrangeLight,
    onSecondary = Color.White,
    background = Brand.Bg,
    onBackground = Brand.Text,
    surface = Brand.BgElevated,
    onSurface = Brand.Text,
    surfaceVariant = Brand.Glass,
    onSurfaceVariant = Brand.TextDim,
    surfaceContainer = Brand.BgElevated,
    surfaceContainerHigh = Color(0xFF1C1715),
    surfaceContainerHighest = Color(0xFF231D1A),
    outline = Brand.GlassBorder,
    outlineVariant = Brand.GlassBorder,
    error = Brand.Danger,
    onError = Color.White,
)

@Composable
fun MaryHairSalonTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColors, typography = AppTypography, content = content)
}
