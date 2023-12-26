package moe.peanutmelonseedbigalmond.push.ui.component.widget

import android.text.Spanned
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import moe.peanutmelonseedbigalmond.push.BaseApp
import moe.peanutmelonseedbigalmond.push.ui.data.MessageData
import moe.peanutmelonseedbigalmond.push.utils.DatetimeUtils
import moe.peanutmelonseedbigalmond.push.utils.SpanUtils

@Composable
fun MessageItem(
    messageData: MessageData,
    onDeleteAction: (MessageData) -> Unit,
    onItemClick: (MessageData) -> Unit
) {
    val context = LocalContext.current
    val content: CharSequence = if (messageData.type == "markdown") {
        BaseApp.markwon.toMarkdown(messageData.content)
    } else {
        messageData.content
    }
    val secondaryTextColor = MaterialTheme.colorScheme.outline

    Row(modifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .fillMaxWidth()
        .wrapContentHeight()
        .clickable { onItemClick(messageData) }
        .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (content is Spanned) {
            ImagePreviewWidget(imageList = SpanUtils.findImageUrlFromSpan(content))
            Spacer(modifier = Modifier.width(8.dp))
        } else if (messageData.type == "image") {
            ImagePreviewWidget(imageList = listOf(messageData.content))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = messageData.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = DatetimeUtils.getDateString(context, messageData.sendTime),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            when (messageData.type) {
                "markdown" -> {
                    Text(
                        text = SpanUtils.uniformString(content),
                        maxLines = 2,
                        color = secondaryTextColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                "image" -> {
                    Text(
                        text = "图片",
                        maxLines = 2,
                        color = secondaryTextColor,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                else -> {
                    Text(
                        text = messageData.content,
                        maxLines = 2,
                        color = secondaryTextColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
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
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
        )
    }
}
