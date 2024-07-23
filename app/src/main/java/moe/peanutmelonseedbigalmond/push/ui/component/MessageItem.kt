package moe.peanutmelonseedbigalmond.push.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import moe.peanutmelonseedbigalmond.push.bean.MessageExcerptInfo
import moe.peanutmelonseedbigalmond.push.emuration.MessageType
import moe.peanutmelonseedbigalmond.push.utils.DateUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageItem(
    excerpt: String,
    pushedAt: Long,
    modifier: Modifier = Modifier,
    title: String? = null,
    coverImgUrl: String? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onLongClick = onLongClick,
                    onClick = onClick
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (coverImgUrl != null) {
                SubcomposeAsyncImage(
                    model = coverImgUrl,
                    contentDescription = null,
                    modifier = modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title ?: "未命名消息",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )

                Text(
                    text = DateUtils.timestampToString(pushedAt),
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )

                HorizontalDivider()

                Text(text = excerpt, maxLines = 2)
            }
        }
    }
}

@Composable
private fun TextAndMarkdownMessageItem(
    messageExcerptInfo: MessageExcerptInfo,
    modifier: Modifier = Modifier,
    onClick: (MessageExcerptInfo) -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    MessageItem(
        excerpt = messageExcerptInfo.excerpt,
        pushedAt = messageExcerptInfo.pushedAt,
        coverImgUrl = messageExcerptInfo.coverUrl,
        title = messageExcerptInfo.title,
        modifier = modifier,
        onClick = {
            onClick(messageExcerptInfo)
        },
        onLongClick = onLongClick
    )
}

@Composable
private fun PictureMessageItem(
    messageExcerptInfo: MessageExcerptInfo,
    modifier: Modifier = Modifier,
    onClick: (MessageExcerptInfo) -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    MessageItem(
        excerpt = "图片",
        pushedAt = messageExcerptInfo.pushedAt,
        coverImgUrl = messageExcerptInfo.excerpt,
        title = messageExcerptInfo.title,
        modifier = modifier,
        onClick = {
            onClick(messageExcerptInfo)
        },
        onLongClick = onLongClick
    )
}

@Composable
fun MessageItem(
    messageExcerptInfo: MessageExcerptInfo,
    modifier: Modifier = Modifier,
    onClick: (MessageExcerptInfo) -> Unit = {},
    onDelete: (MessageExcerptInfo) -> Unit = {}
) {
    var menuShow by remember { mutableStateOf(false) }
    Box {
        when (messageExcerptInfo.type) {
            MessageType.Text, MessageType.Markdown -> TextAndMarkdownMessageItem(
                messageExcerptInfo,
                onClick = onClick,
                modifier = modifier,
                onLongClick = {
                    menuShow = true
                }
            )

            MessageType.Picture -> PictureMessageItem(
                messageExcerptInfo,
                onClick = onClick,
                modifier = modifier,
                onLongClick = {
                    menuShow = true
                }
            )
        }

        DropdownMenu(expanded = menuShow, onDismissRequest = { menuShow = false }) {
            DropdownMenuItem(text = { Text(text = "删除") }, onClick = {
                menuShow = false
                onDelete(messageExcerptInfo)
            }, leadingIcon = {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
            })
        }
    }
}