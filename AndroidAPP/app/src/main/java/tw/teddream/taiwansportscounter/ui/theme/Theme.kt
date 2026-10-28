package tw.teddream.taiwansportscounter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** 對應 webapp／iOS 的配色 */
object Brand {
    val Blue = Color(0xFF007BBF)
    val Orange = Color(0xFFFF9500)
    val Red = Color(0xFFFF3B30)
    val Gray = Color(0xFF8E8E93)

    /** 依人數比例回傳顏色：<50% 藍、50~80% 橘、>80% 紅，無容留上限則灰 */
    fun progressColor(current: Int, max: Int): Color {
        if (max <= 0) return Gray
        val ratio = current.toFloat() / max * 100
        return when {
            ratio < 50 -> Blue
            ratio <= 80 -> Orange
            else -> Red
        }
    }
}

@Immutable
data class AppColors(
    val background: Color,
    val card: Color,
    val chip: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
)

private val LightColors = AppColors(
    background = Color(0xFFF2F2F7),
    card = Color.White,
    chip = Color(0xFFE5E5EA),
    divider = Color(0xFFE5E5EA),
    textPrimary = Color(0xFF1C1C1E),
    textSecondary = Color(0xFF6C6C70),
)

private val DarkColors = AppColors(
    background = Color.Black,
    card = Color(0xFF1C1C1E),
    chip = Color(0xFF2C2C2E),
    divider = Color(0xFF38383A),
    textPrimary = Color.White,
    textSecondary = Color(0xFFAEAEB2),
)

private val LocalAppColors = staticCompositionLocalOf { LightColors }

object DongchaoTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current
}

@Composable
fun DongchaoTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val colors = if (dark) DarkColors else LightColors
    // 不使用 Dynamic Color，維持品牌藍色
    val scheme = if (dark) {
        darkColorScheme(
            primary = Brand.Blue,
            background = colors.background,
            surface = colors.background,
            surfaceContainer = colors.card,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary,
            onSurfaceVariant = colors.textSecondary,
            secondaryContainer = colors.chip,
        )
    } else {
        lightColorScheme(
            primary = Brand.Blue,
            background = colors.background,
            surface = colors.background,
            surfaceContainer = colors.card,
            onBackground = colors.textPrimary,
            onSurface = colors.textPrimary,
            onSurfaceVariant = colors.textSecondary,
            secondaryContainer = colors.chip,
        )
    }
    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
