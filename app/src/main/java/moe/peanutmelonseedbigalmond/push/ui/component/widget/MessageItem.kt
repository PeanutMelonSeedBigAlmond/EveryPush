package moe.peanutmelonseedbigalmond.push.ui.component.widget

import android.text.Spanned
import android.util.Log
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import moe.peanutmelonseedbigalmond.push.utils.SpanUtils

@Composable
fun MessageItem(messageData: MessageData, onDeleteAction: (MessageData) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        Column {
            Box {
                when (messageData.type) {
                    MessageData.Type.TEXT -> {
                        TextContent(title = messageData.title, content = messageData.content)
                    }

                    MessageData.Type.IMAGE -> {
                        ImageWithTextContent(
                            title = null,
                            content = messageData.title,
                            imageUrls = listOf(messageData.content)
                        )
                    }

                    MessageData.Type.MARKDOWN -> {
                        val spanned = App.markwon.toMarkdown(messageData.content)
                        ImageWithTextContent(
                            title = messageData.title,
                            content = spanned,
                            imageUrls = SpanUtils.findImageUrlFromSpan(spanned)
                        )
                    }

                    else -> {
                        TextContent(title = messageData.title, content = messageData.content)
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
fun TextContent(title: String?, content: CharSequence) {
    var showDetailDialog by remember { mutableStateOf(false) }
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable {
                showDetailDialog = true
            }
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        SelectionContainer(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = content.toString(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
    if (showDetailDialog) {
        MyAlertDialog(
            title = { if (title != null) Text(text = title) },
            content = {
                SelectionContainer {
                    Text(text = content.toString())
                }
            },
            dismissButton = {
                TextButton(onClick = { showDetailDialog = false }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
            confirmButton = { }) {
            showDetailDialog = false
        }
    }
}

@Composable
private fun ImageWithTextContent(title: String?, content: CharSequence, imageUrls: List<String>) {
    var showDetailDialog by remember { mutableStateOf(false) }
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable {
            showDetailDialog = true
        }
    ) {
        ImagePreviewWidget(imageList = imageUrls)
        TextContent(title = title, content = content)
    }
    if (showDetailDialog) {
        MyAlertDialog(
            title = { if (title != null) Text(text = title) },
            content = {
                if (content is Spanned) {
                    AndroidView(
                        factory = {
                            SelectableAndClickableTextView(it).also {
                                it.setTextIsSelectable(
                                    true
                                )
                            }
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .sizeIn(maxHeight = 640.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        App.markwon.setParsedMarkdown(it, content)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(
                            items = imageUrls, key = { index, _ -> index }
                        ) { _, item ->
                            ImageWidget(imageUrl = item)
                        }
                        item {
                            SelectionContainer {
                                Text(text = content.toString())
                            }
                        }
                    }
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
private fun ImageWidget(imageUrl: String) {
    val model = ImageRequest.Builder(LocalContext.current)
        .data(imageUrl)
        .crossfade(true)
        .build()
    SubcomposeAsyncImage(
        model = model,
        contentDescription = "Image",
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
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

@Composable
fun ImagePreviewWidget(imageList: List<String>) {
    if (imageList.isEmpty()) return
    Box {
        ImageWidget(imageUrl = imageList[0])
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
