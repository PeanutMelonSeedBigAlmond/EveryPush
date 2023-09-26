package moe.peanutmelonseedbigalmond.push.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun MyAppTheme(
    content: @Composable () -> Unit
) {
    val systemInDarkMode = isSystemInDarkTheme()
    val useDarkIcons = !systemInDarkMode
    val systemUiController = rememberSystemUiController()
    val context = LocalContext.current
    systemUiController.setStatusBarColor(
        Color.Transparent,
        useDarkIcons
    )
    MaterialTheme(
        colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (systemInDarkMode) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        } else {
            if (systemInDarkMode) {
                darkColorScheme()
            } else {
                lightColorScheme()
            }
        },
        content = content
    )
}