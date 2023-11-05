package moe.peanutmelonseedbigalmond.push.ui.component.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import moe.peanutmelonseedbigalmond.push.App
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.ui.component.widget.preference.getWidgetSurfaceColor
import moe.peanutmelonseedbigalmond.push.ui.data.MessageData
import moe.peanutmelonseedbigalmond.push.utils.SpanUtils

@Composable
fun MessageItem(
    messageData: MessageData,
    onDeleteAction: (MessageData) -> Unit,
    onItemClick: (MessageData) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onItemClick(messageData) },
        colors = CardDefaults.cardColors(containerColor = getWidgetSurfaceColor(elevation = 2.dp)),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 2.dp,
            focusedElevation = 1.dp,
        )
    ) {
        Column {
            Box {
                when (messageData.type) {
                    MessageData.Type.IMAGE -> {
                        ImageWithTextContent(
                            title = null,
                            content = messageData.title,
                            imageUrls = listOf(messageData.content),
                        )
                    }

                    MessageData.Type.MARKDOWN -> {
                        val spanned = App.markwon.toMarkdown(messageData.content)
                        ImageWithTextContent(
                            title = messageData.title,
                            content = spanned,
                            imageUrls = SpanUtils.findImageUrlFromSpan(spanned),
                        )
                    }

                    else -> { //text and else
                        TextContent(
                            title = messageData.title,
                            content = messageData.content,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                MoreOptionsMenu {
                    onDeleteAction(messageData)
                }
            }
        }
    }
}

@Composable
private fun TextContent(
    title: String?,
    content: CharSequence?,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        if (title?.isNotBlank() == true) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (content != null) {
            Text(
                text = content.toString(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ImageWithTextContent(
    title: String?,
    content: CharSequence?,
    imageUrls: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        ImagePreviewWidget(imageList = imageUrls)
        TextContent(title = title, content = content, modifier = Modifier.padding(8.dp))
    }
}

@Composable
private fun MoreOptionsMenu(
    onDeleteAction: () -> Unit,
) {
    var dropDownMenuVisible by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxHeight()) {
        IconButton(onClick = { dropDownMenuVisible = true }, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "More options"
            )
        }
        DropdownMenu(
            expanded = dropDownMenuVisible,
            onDismissRequest = { dropDownMenuVisible = false }
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.delete)) },
                onClick = {
                    dropDownMenuVisible = false
                    onDeleteAction()
                })
        }
    }
}

@Composable
private fun ImagePreviewWidget(imageList: List<String>) {
    if (imageList.isEmpty()) return
    Box {
        ImageWidget(
            imageUrl = imageList[0],
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )
        if (imageList.size > 1) {
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .75f),
                    shape = RoundedCornerShape(bottomStart = 8.dp)
                ) {
                    Text(
                        text = pluralStringResource(
                            id = R.plurals.tip_image_count,
                            count = imageList.size,
                            imageList.size
                        ),
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
