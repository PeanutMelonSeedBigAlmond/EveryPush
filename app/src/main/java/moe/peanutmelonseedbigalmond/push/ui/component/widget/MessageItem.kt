package moe.peanutmelonseedbigalmond.push.ui.component.widget

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.ui.component.LocalMarkwon
import moe.peanutmelonseedbigalmond.push.ui.component.widget.view.SelectableAndClickableTextView
import moe.peanutmelonseedbigalmond.push.ui.data.MessageData
import java.text.DateFormat
import java.util.Date

@Composable
fun MessageItem(messageData: MessageData, onDeleteAction: (MessageData) -> Unit) {
    val dateFormatter =
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT) // yyyy年MM月dd日 HH:mm
    val datetimeString = dateFormatter.format(Date(messageData.sendTime))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (messageData.title.isNotBlank()) {
                    "${messageData.title} · $datetimeString"
                } else {
                    datetimeString
                }
            )
            MoreOptionsMenu {
                onDeleteAction(messageData)
            }
        }

        when (messageData.type) {
            MessageData.Type.TEXT -> {
                TextMessageContent(text = messageData.content)
            }

            MessageData.Type.IMAGE -> {
                ImageMessageContent(content = messageData.content)
            }

            MessageData.Type.MARKDOWN -> {
                MarkdownContent(content = messageData.content)
            }

            else -> {
                TextMessageContent(text = messageData.content)
            }
        }
    }
}

@Composable
private fun TextMessageContent(text: String) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        SelectionContainer {
            Text(text = text, modifier = Modifier.padding(12.dp))
        }
    }
}

@Composable
private fun ImageMessageContent(content: String) {
    val model = ImageRequest.Builder(LocalContext.current)
        .data(content)
        .crossfade(true)
        .build()
    SubcomposeAsyncImage(
        model = model,
        contentDescription = "Image",
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.FillWidth,
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                CircularProgressIndicator()
            }

            is AsyncImagePainter.State.Error -> {
                val error = (painter.state as AsyncImagePainter.State.Error).result.throwable
                Log.w("TAG", "ImageMessageContent: 加载图片失败")
                error.printStackTrace()
                Text(
                    text = stringResource(id = R.string.failed_to_load_image),
                    color = MaterialTheme.colorScheme.error
                )
            }

            is AsyncImagePainter.State.Empty -> {
                Text(text = stringResource(id = R.string.image_is_empty))
            }

            is AsyncImagePainter.State.Success -> {
                SubcomposeAsyncImageContent()
            }
        }
    }
}

@Composable
fun MarkdownContent(content: String) {
    val markwon = LocalMarkwon.current
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        AndroidView(
            factory = {
                SelectableAndClickableTextView(it).apply { setTextIsSelectable(true) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            markwon.setMarkdown(it, content)
        }
    }
}

@Composable
private fun MoreOptionsMenu(
    onDeleteAction: () -> Unit,
) {
    var dropDownMenuVisible by remember { mutableStateOf(false) }
    Box(modifier = Modifier.wrapContentSize()) {
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
