package moe.peanutmelonseedbigalmond.push.service

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.app.job.JobService
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.peanutmelonseedbigalmond.push.utils.NotificationUtil
import moe.peanutmelonseedbigalmond.push.utils.SystemUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.coroutines.cancellation.CancellationException

class NotificationImageDownloaderJobService : JobService(),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    @SuppressLint("MissingPermission")
    override fun onStartJob(params: JobParameters?): Boolean {
        if (params == null) return false
        val extra = params.extras
        val notificationId = extra.getInt("notificationId")
        val messageGroupId = extra.getString("messageGroupId")
        val priority = extra.getInt("priority")
        val messageTitle=extra.getString("messageTitle")
        val messageContent=extra.getString("messageContent")?:return false
        val coverImgUrl = extra.getString("coverImgUrl") ?: return false

        launch {
            try {
                val bitmap = downloadAndCompressImage(coverImgUrl)
                if (SystemUtils.hasNotificationPermission()) {
                    NotificationUtil.repostNotificationWithImage(
                        messageGroupId,
                        priority,
                        notificationId,
                        messageTitle,
                        messageContent,
                        bitmap
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                jobFinished(params, false)
            }
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    override fun onStopJob(params: JobParameters?): Boolean = false

    suspend fun downloadAndCompressImage(url: String, maxSize: Long = 1024 * 1024 * 1): Bitmap {
        val imageRequest = ImageRequest.Builder(this@NotificationImageDownloaderJobService)
            .data(url)
            .build()
        val image = imageLoader.execute(imageRequest)
        val bitmap = compressBitmap((image.drawable as BitmapDrawable).bitmap, maxSize)
        return bitmap
    }

    private suspend fun compressBitmap(bitmap: Bitmap, maxSize: Long): Bitmap =
        withContext(Dispatchers.IO) {
            if (bitmap.byteCount > maxSize) {
                var quality = 100
                val baos = ByteArrayOutputStream()
                while (isActive) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
                    if (baos.size() <= maxSize) {
                        break
                    } else {
                        quality -= 10
                        if (quality <= 0) break
                        baos.reset()
                    }
                }

                if (quality <= 0) {
                    baos.reset()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 10, baos)
                }
                val bais = ByteArrayInputStream(baos.toByteArray())
                return@withContext BitmapFactory.decodeStream(bais)
            } else {
                return@withContext bitmap
            }
        }
}