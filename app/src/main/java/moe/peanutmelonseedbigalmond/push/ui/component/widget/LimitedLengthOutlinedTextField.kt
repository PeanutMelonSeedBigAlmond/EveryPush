package moe.peanutmelonseedbigalmond.push.ui.component.widget

import androidx.annotation.IntRange
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun LimitedLengthOutlinedTextField(
    modifier: Modifier = Modifier,
    @IntRange(from = 0L) limit: Int,
    placeholder: @Composable () -> Unit,
    isError: Boolean,
    supportText: @Composable () -> Unit,
    value: String,
    onValueChange: (String) -> Unit,
    contentLimitation: Regex? = null,
) {
    var text by remember { mutableStateOf(value) }
    OutlinedTextField(
        modifier = modifier,
        value = text,
        placeholder = placeholder,
        isError = isError,
        supportingText = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box {
                    supportText()
                }
                Text(text = "${text.length} / $limit")
            }
        },
        onValueChange = {
            if (contentLimitation != null && !it.matches(contentLimitation)) return@OutlinedTextField
            if (it.length >= limit) {
                val truncated = it.substring(0, limit)
                text = truncated
                onValueChange(truncated)
            } else {
                text = it
                onValueChange(it)
            }
        },
    )
}