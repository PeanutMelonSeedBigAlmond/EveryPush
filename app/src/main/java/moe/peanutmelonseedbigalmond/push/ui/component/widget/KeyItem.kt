package moe.peanutmelonseedbigalmond.push.ui.component.widget

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.ui.data.TokenData
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date

@Composable
fun KeyItem(
    key: TokenData,
    onResetAction: (TokenData) -> Unit,
    onDeleteAction: (TokenData) -> Unit,
    onCopyAction: (TokenData) -> Unit,
    onItemClick: (TokenData) -> Unit
) {
    val dateFormatter=DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.SHORT)
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onItemClick(key)
            },
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Key,
                        contentDescription = "Keys icon",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.surfaceTint
                    )
                    Text(text = key.name, modifier = Modifier.padding(4.dp))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(12.dp),
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = "Date icon"
                    )
                    Text(
                        text =  dateFormatter.format(Date(key.createTime)),
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Text(text = key.key, maxLines = 1, modifier = Modifier.padding(8.dp))
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = { onDeleteAction(key) }) {
                    Text(text = stringResource(id = R.string.delete))
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(onClick = { onResetAction(key) }) {
                        Text(text = stringResource(id = R.string.reset))
                    }
                    Button(onClick = { onCopyAction(key) }) {
                        Text(text = stringResource(id = R.string.copy))
                    }
                }
            }
        }
    }
}