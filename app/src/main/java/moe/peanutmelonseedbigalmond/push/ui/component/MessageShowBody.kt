package moe.peanutmelonseedbigalmond.push.ui.component

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import moe.peanutmelonseedbigalmond.push.BaseApp
import moe.peanutmelonseedbigalmond.push.bean.MessageDetail
import moe.peanutmelonseedbigalmond.push.emuration.MessageType

@Composable
private fun TextMessageBody(messageDetail: MessageDetail, modifier: Modifier = Modifier) {
    SelectionContainer {
        Text(modifier = modifier.fillMaxSize(), text = messageDetail.content)
    }
}

// https://github.com/coil-kt/coil/issues/884
@Composable
private fun PictureMessageBody(messageDetail: MessageDetail, modifier: Modifier = Modifier) {
    var retryMarker by remember { mutableIntStateOf(0) }
    key(retryMarker) {
        SubcomposeAsyncImage(
            model = messageDetail.content,
            contentDescription = null,
            modifier = modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth
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
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = error.localizedMessage ?: error.toString(),
                            color = MaterialTheme.colorScheme.error
                        )

                        TextButton(onClick = {
                            retryMarker++
                        }) {
                            Text(text = "重试")
                        }
                    }
                }

                is AsyncImagePainter.State.Empty -> {
                    Text(text = "图片为空")
                }

                is AsyncImagePainter.State.Success -> {
                    SubcomposeAsyncImageContent()
                }
            }
        }
    }
}

@Composable
private fun MarkdownMessageBody(messageDetail: MessageDetail, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val textStyle = LocalTextStyle.current
    AndroidView(factory = {
        SelectableAndClickableTextView(context).also {
            val color = textStyle.color
            val colorInt =
                (((1 - color.alpha) * 0xff).toInt() shl 24) or ((color.red * 0xff).toInt() shl 16) or ((color.green * 0xff).toInt() shl 8) or (color.blue * 0xff).toInt()
            it.setTextColor(colorInt)
            it.textSize = textStyle.fontSize.value
            it.setTextIsSelectable(true)
        }
    }, modifier = modifier) {
        BaseApp.markwon.setMarkdown(it, messageDetail.content)
    }
}

@Composable
fun MessageBody(messageDetail: MessageDetail, modifier: Modifier = Modifier) {
    when (messageDetail.type) {
        MessageType.Text -> {
            TextMessageBody(messageDetail, modifier)
        }

        MessageType.Picture -> {
            PictureMessageBody(messageDetail, modifier.clip(RoundedCornerShape(8.dp)))
        }

        MessageType.Markdown -> {
            MarkdownMessageBody(messageDetail, modifier)
        }
    }
}