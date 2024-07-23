package moe.peanutmelonseedbigalmond.push.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import moe.peanutmelonseedbigalmond.push.bean.KeyInfo
import moe.peanutmelonseedbigalmond.push.utils.DateUtils

@Composable
fun KeyItem(
    keyInfo: KeyInfo,
    modifier: Modifier = Modifier,
    onReset: (KeyInfo) -> Unit = {},
    onDelete: (KeyInfo) -> Unit = {},
    onCopy: (KeyInfo) -> Unit = {},
    onRename: (KeyInfo) -> Unit = {}
) {
    OutlinedCard(onClick = { onRename(keyInfo) }, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = keyInfo.name, maxLines = 1)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Filled.CalendarMonth, contentDescription = null)
                    Text(text = DateUtils.timestampToString(keyInfo.createdAt))
                }
            }

            Card(onClick = { onCopy(keyInfo) }, modifier = Modifier.fillMaxWidth()) {
                Text(text = keyInfo.key, modifier = Modifier.padding(16.dp))
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(onClick = { onCopy(keyInfo) }) {
                    Text(text = "复制")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = { onDelete(keyInfo) }) {
                        Text("删除")
                    }

                    Button(onClick = { onReset(keyInfo) }) {
                        Text(text = "重置")
                    }
                }
            }
        }
    }
}
