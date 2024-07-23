package moe.peanutmelonseedbigalmond.push.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import moe.peanutmelonseedbigalmond.push.bean.MessageDetail
import moe.peanutmelonseedbigalmond.push.emuration.MessageType
import moe.peanutmelonseedbigalmond.push.utils.DateUtils

private fun createGradientBrush(
    startColor: Color,
    endColor: Color
): Brush {
    val endOffset = Offset(0f, Float.POSITIVE_INFINITY)
    return Brush.linearGradient(
        listOf(startColor, endColor),
        start = Offset.Zero,
        end = endOffset,
        tileMode = TileMode.Clamp
    )
}

@Composable
fun MessageDetailWithCoverImage(messageDetail: MessageDetail, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(320.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SubcomposeAsyncImage(
                model = messageDetail.coverImgUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(
                            createGradientBrush(
                                Color(255, 255, 255, 0),
                                Color(29, 41, 49, 128)
                            )
                        )
                        .padding(8.dp)
                        .padding(top = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = messageDetail.title ?: "未命名消息",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )

                    Text(
                        text = DateUtils.timestampToString(messageDetail.pushedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun MessageDetailWithoutCoverImage(messageDetail: MessageDetail, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = messageDetail.title ?: "未命名消息",
            style = MaterialTheme.typography.titleSmall
        )

        Text(
            text = DateUtils.timestampToString(messageDetail.pushedAt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )

        HorizontalDivider()
    }
}

@Composable
fun MessageDetailHeader(messageDetail: MessageDetail, modifier: Modifier = Modifier) {
    if (messageDetail.type == MessageType.Picture) {
        MessageDetailWithoutCoverImage(messageDetail = messageDetail, modifier)
    } else {
        if (messageDetail.coverImgUrl != null) {
            MessageDetailWithCoverImage(messageDetail = messageDetail, modifier)
        } else {
            MessageDetailWithoutCoverImage(messageDetail = messageDetail, modifier)
        }
    }
}