package moe.peanutmelonseedbigalmond.push.ui.component.widget.preference

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PreferenceGroup(
    modifier: Modifier,
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier,
    ) {
        Text(text = title, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}