package com.example.core_ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.core_ui.design.AppShapes

private val Light = lightColorScheme()
private val Dark = darkColorScheme()

@Composable
fun PharmacyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val ctx = LocalContext.current
    val scheme =
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        else if (darkTheme) Dark else Light

    MaterialTheme(colorScheme = scheme, shapes = AppShapes, typography = Typography, content = content)
}
