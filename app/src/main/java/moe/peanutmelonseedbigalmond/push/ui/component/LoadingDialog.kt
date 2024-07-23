package moe.peanutmelonseedbigalmond.push.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingDialog(show: Boolean, modifier: Modifier = Modifier, onDismissRequest: () -> Unit = {}) {
    if (!show) return
    AlertDialog(onDismissRequest = onDismissRequest, confirmButton = {}, text = {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator()

            Text(text = "Loading")
        }
    }, modifier = modifier)
}