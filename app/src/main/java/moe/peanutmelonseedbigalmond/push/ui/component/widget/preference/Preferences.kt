package moe.peanutmelonseedbigalmond.push.ui.component.widget.preference

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ln

private class ContentAlpha(val alpha: Float) {
    companion object {
        val AlphaHigh = ContentAlpha(1f)
        val AlphaMedium = ContentAlpha(.75f)
        val AlphaDisabled = ContentAlpha(.33f)
    }
}

@Composable
fun TextPreferences(
    modifier: Modifier,
    title: @Composable () -> Unit,
    summary: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    val typography = MaterialTheme.typography
    val titleStyle = applyTextStyle(
        textStyle = typography.titleMedium,
        textColor = MaterialTheme.colorScheme.onSurface,
        contentAlpha = if (enabled) {
            ContentAlpha.AlphaHigh
        } else {
            ContentAlpha.AlphaDisabled
        },
        content = title
    )!!
    val summaryStyledText = if (summary == null) {
        null
    } else {
        applyTextStyle(
            textStyle = typography.bodyMedium,
            textColor = MaterialTheme.colorScheme.onBackground,
            contentAlpha = if (enabled) {
                ContentAlpha.AlphaHigh
            } else {
                ContentAlpha.AlphaDisabled
            },
            content = summary
        )
    }

    Card(
        modifier = modifier
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = getWidgetSurfaceColor(elevation = 2.dp))
    ) {
        Column(modifier = modifier.padding(16.dp)) {
            titleStyle()
            Spacer(modifier = Modifier.height(8.dp))
            summaryStyledText?.invoke()
        }
    }

}

@Composable
fun <T> MenuPreferences(
    modifier: Modifier,
    optionsText: Array<String>,
    optionsId: Array<T>,
    title: @Composable () -> Unit,
    summary: @Composable (() -> Unit)? = null,
    menuTitle: String? = null,
    enabled: Boolean = true,
    onMenuSelected: (T) -> Unit = {},
) {
    var menuVisible by rememberSaveable { mutableStateOf(false) }

    Box {
        TextPreferences(
            modifier = Modifier.fillMaxWidth(),
            title = title,
            summary = summary,
            enabled = enabled,
        ) {
            menuVisible = true
        }
        DropdownMenu(
            expanded = menuVisible,
            onDismissRequest = { menuVisible = false },
        ) {
            if (menuTitle != null) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = menuTitle,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            for (i in optionsText.indices) {
                val id = optionsId[i]
                DropdownMenuItem(
                    text = {
                        Text(
                            text = optionsText[i],
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        menuVisible = false
                        onMenuSelected(id)
                    },
                )
            }
        }
    }
}

private fun applyTextStyle(
    textStyle: TextStyle,
    textColor: Color,
    contentAlpha: ContentAlpha,
    content: @Composable (() -> Unit)?,
): @Composable (() -> Unit)? {
    if (content == null) return null
    return {
        val newTextStyle = textStyle.copy(
            color = textColor.copy(alpha = contentAlpha.alpha),
        )
        CompositionLocalProvider(
            LocalTextStyle provides newTextStyle,
        ) {
            content()
        }
    }
}

// from https://github.com/WangDaYeeeeee/GeometricWeather
@Composable
fun getWidgetSurfaceColor(elevation: Dp): Color {
    val surface = MaterialTheme.colorScheme.surface

    if (elevation == 0.dp) {
        return surface
    }

    return MaterialTheme
        .colorScheme
        .surfaceTint
        .copy(alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f)
        .compositeOver(surface)
}