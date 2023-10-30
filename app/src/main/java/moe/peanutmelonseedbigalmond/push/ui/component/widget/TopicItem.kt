package moe.peanutmelonseedbigalmond.push.ui.component.widget

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import moe.peanutmelonseedbigalmond.push.App
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.ui.component.widget.preference.getWidgetSurfaceColor
import moe.peanutmelonseedbigalmond.push.ui.data.TopicData
import moe.peanutmelonseedbigalmond.push.utils.DatetimeUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TopicItem(
    data: TopicData,
    onClick: (TopicData) -> Unit,
    onRenameAction: (TopicData) -> Unit,
    onDeleteAction: (TopicData) -> Unit
) {
    val context = LocalContext.current
    var dropDownMenuShow by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .combinedClickable(
                onClick = { onClick(data) },
                onLongClick = { dropDownMenuShow = true }
            )
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = getWidgetSurfaceColor(elevation = 1.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = data.name ?: stringResource(id = R.string.title_default_notification),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                if (data.latestMessage != null) {
                    Text(
                        text = DatetimeUtils.getDateString(context, data.latestMessage.sendTime),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (data.latestMessage?.title?.isNotBlank() == true) {
                    data.latestMessage.title
                } else if (data.latestMessage?.content?.isNotBlank() == true) {
                    App.markwon.toMarkdown(data.latestMessage.content).toString()
                } else {
                    ""
                }.replace(Regex("[\\s\\n]+"), " "),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (data.name != null && data.id != null) {
            DropdownMenu(
                expanded = dropDownMenuShow,
                onDismissRequest = { dropDownMenuShow = false }) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(id = R.string.rename)) },
                    onClick = {
                        onRenameAction(data)
                        dropDownMenuShow = false
                    })

                DropdownMenuItem(
                    text = {
                        Text(text = stringResource(id = R.string.delete))
                    },
                    onClick = {
                        onDeleteAction(data)
                        dropDownMenuShow = false
                    })
            }
        }
    }
}
