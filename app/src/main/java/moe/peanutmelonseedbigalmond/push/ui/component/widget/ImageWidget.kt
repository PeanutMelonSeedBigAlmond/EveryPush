package moe.peanutmelonseedbigalmond.push.ui.component.widget

import android.util.Log
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import moe.peanutmelonseedbigalmond.push.R


@Composable
fun ImageWidget(imageUrl: String, modifier: Modifier = Modifier) {
    val model = ImageRequest.Builder(LocalContext.current)
        .data(imageUrl)
        .crossfade(true)
        .build()
    SubcomposeAsyncImage(
        model = model,
        contentDescription = "Image",
        modifier = modifier,
        contentScale = ContentScale.Crop,
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