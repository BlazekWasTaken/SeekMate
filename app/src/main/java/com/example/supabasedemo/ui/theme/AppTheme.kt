package com.example.supabasedemo.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Shapes
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

private val lightColorScheme = lightColorScheme(
    background = Background,
    onBackground = OnBackground,
    outline = Outline,
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    inversePrimary = InversePrimary,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    surfaceDim = GreyBlue,
    surfaceTint = LightBlue,
    surfaceBright = LightBlue,
    surfaceContainer = LightBlue,
    surfaceContainerLow = DarkBlue,
    surfaceContainerHigh = LightBlue,
    surfaceContainerLowest = DarkBlue,
    surfaceContainerHighest = LightBlue,
    onSurfaceVariant = OnSurfaceVariant,
    inverseSurface = DarkBlue,
    inverseOnSurface = LightBlue,
    scrim = Color.Blue,
    error = Error,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    onError = OnError,
    outlineVariant = OutlineVariant,
)

private val darkColorScheme = darkColorScheme(
    background = BackgroundD,
    onBackground = OnBackgroundD,
    outline = OutlineD,
    primary = PrimaryD,
    onPrimary = OnPrimaryD,
    primaryContainer = PrimaryContainerD,
    inversePrimary = InversePrimaryD,
    onPrimaryContainer = OnPrimaryContainerD,
    secondary = SecondaryD,
    onSecondary = OnSecondaryD,
    secondaryContainer = SecondaryContainerD,
    onSecondaryContainer = OnSecondaryContainerD,
    tertiary = TertiaryD,
    onTertiary = OnTertiaryD,
    tertiaryContainer = TertiaryContainerD,
    onTertiaryContainer = OnTertiaryContainerD,
    surface = SurfaceD,
    onSurface = OnSurfaceD,
    surfaceVariant = SurfaceVariantD,
    surfaceDim = GreyBlueDark,
    surfaceTint = LightBlueDark,
    surfaceBright = LightBlueDark,
    surfaceContainer = LightBlueDark,
    surfaceContainerLow = DarkBlueDark,
    surfaceContainerHigh = LightBlueDark,
    surfaceContainerLowest = DarkBlueDark,
    surfaceContainerHighest = LightBlueDark,
    onSurfaceVariant = OnSurfaceVariantD,
    inverseSurface = DarkBlueDark,
    inverseOnSurface = LightBlueDark,
    scrim = Color.Blue,
    error = ErrorD,
    errorContainer = ErrorContainerD,
    onErrorContainer = OnErrorContainerD,
    onError = OnErrorD,
    outlineVariant = OutlineVariantD,
)

private val typography = Typography(
    titleLarge = Typography.titleLarge,
    titleSmall = Typography.titleSmall,
    bodyLarge = Typography.bodyLarge,
    labelLarge = Typography.labelLarge,
    labelMedium = Typography.labelMedium,
    labelSmall = Typography.labelSmall
)

val replyShapes = Shapes(
    extraSmall = RoundedCornerShape(0),
    small = RoundedCornerShape(0),
    medium = RoundedCornerShape(0),
    large = RoundedCornerShape(0),
    extraLarge = RoundedCornerShape(0)
)

@Composable
fun AppTheme(
    getThemeChoice: () -> ThemeChoice,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val colorScheme: ColorScheme = when (getThemeChoice()) {
        is ThemeChoice.Dark -> {
            darkColorScheme
        }
        is ThemeChoice.Light -> {
            lightColorScheme
        }
        is ThemeChoice.System -> {
            if (isDarkTheme) darkColorScheme else lightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content,
        shapes = replyShapes
    )
}

sealed class ThemeChoice {
    data object Light: ThemeChoice()
    data object Dark: ThemeChoice()
    data object System: ThemeChoice()
}

@Composable
fun MyOutlinedButton(
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        content = content,
        shape = RectangleShape,
        border = (BorderStroke(1.dp, AppTheme.colorScheme.outline))
    )
}

//do the same for outlined text field
@Composable
fun MyOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: @Composable () -> Unit,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        shape = RectangleShape,
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = AppTheme.colorScheme.outlineVariant,
            focusedIndicatorColor = AppTheme.colorScheme.outline,
            unfocusedPlaceholderColor = AppTheme.colorScheme.onSurface
        ),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions
    )
}

object AppTheme {
    val colorScheme: ColorScheme @Composable get() = MaterialTheme.colorScheme
    val typography: Typography @Composable get() = MaterialTheme.typography
}