package moe.peanutmelonseedbigalmond.push.ui.component.widget

import android.text.TextUtils
import android.util.Log
import android.widget.TextView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import moe.peanutmelonseedbigalmond.push.App
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.ui.component.widget.view.SelectableAndClickableTextView
import moe.peanutmelonseedbigalmond.push.ui.data.MessageData
import moe.peanutmelonseedbigalmond.push.utils.DatetimeUtils
import moe.peanutmelonseedbigalmond.push.utils.SpanUtils

@Composable
fun MessageItem(messageData: MessageData, onDeleteAction: (MessageData) -> Unit) {
    val context = LocalContext.current
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
                    "${messageData.title} · ${
                        DatetimeUtils.getDateString(
                            context,
                            messageData.sendTime
                        )
                    }"
                } else {
                    DatetimeUtils.getDateString(context, messageData.sendTime)
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
    var showDetailDialog by remember { mutableStateOf(false) }
    val model = ImageRequest.Builder(LocalContext.current)
        .data(content)
        .crossfade(true)
        .build()
    SubcomposeAsyncImage(
        model = model,
        contentDescription = "Image",
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clickable {
                showDetailDialog = true
            }
            .clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop,
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                CircularProgressIndicator(modifier = Modifier.wrapContentSize())
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

                if (showDetailDialog) {
                    MyAlertDialog(
                        title = { },
                        content = {
                            SubcomposeAsyncImageContent(
                                modifier = Modifier.sizeIn(maxHeight = 640.dp),
                                contentScale = ContentScale.Fit
                            )
                        },
                        dismissButton = { },
                        confirmButton = {
                            TextButton(onClick = { showDetailDialog = false }) {
                                Text(text = stringResource(id = R.string.confirm))
                            }
                        }) {
                        showDetailDialog = false
                    }
                }
            }
        }
    }
}

@Composable
fun MarkdownContent(content: String) {
    val spanned = remember { App.markwon.toMarkdown(content) }
    var showDetailDialog by remember {
        mutableStateOf(false)
    }
    OutlinedCard(modifier = Modifier
        .fillMaxWidth()
        .clickable { showDetailDialog = true }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AndroidView(factory = {
                TextView(it).also {
                    it.maxLines = 5
                    it.ellipsize = TextUtils.TruncateAt.END
                }
            }, modifier = Modifier.fillMaxWidth()) {
                it.text = spanned
            }
            BuildMarkdownImageGrid(imageList = SpanUtils.findImageUrlFromSpan(spanned))
        }
    }

    if (showDetailDialog) {
        MyAlertDialog(
            title = { },
            content = {
                AndroidView(
                    factory = {
                        SelectableAndClickableTextView(it).also {
                            it.setTextIsSelectable(
                                true
                            )
                        }
                    }, modifier = Modifier
                        .fillMaxWidth()
                        .sizeIn(maxHeight = 800.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    App.markwon.setMarkdown(it, content)
                }
            },
            dismissButton = { },
            confirmButton = {
                TextButton(onClick = { showDetailDialog = false }) {
                    Text(text = stringResource(id = R.string.confirm))
                }
            }) {
            showDetailDialog = false
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

@Composable
fun BuildMarkdownImageGrid(imageList: List<String>) {
    val list = imageList.take(9).chunked(3)
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in list.indices) {
            key(i) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(240.dp)
                ) {
                    for (j in list[i].indices) {
                        key(j) {
                            val model = ImageRequest.Builder(LocalContext.current)
                                .data(list[i][j])
                                .crossfade(true)
                                .build()
                            SubcomposeAsyncImage(
                                model = model,
                                contentDescription = "Image",
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.FillWidth,
                            ) {
                                when (painter.state) {
                                    is AsyncImagePainter.State.Loading -> {
                                        CircularProgressIndicator(modifier = Modifier.wrapContentSize())
                                    }

                                    is AsyncImagePainter.State.Error -> {
                                        val error =
                                            (painter.state as AsyncImagePainter.State.Error).result.throwable
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
                    }
                }
            }
        }
    }
}
