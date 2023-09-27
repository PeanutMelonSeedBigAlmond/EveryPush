package moe.peanutmelonseedbigalmond.push.ui.component.widget

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoadingDialog(
    message: String = "Loading...",
    onDismissRequest: () -> Unit = {}
) {
    AlertDialog(onDismissRequest = onDismissRequest, confirmButton = {}, text = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = message)
        }
    })
}