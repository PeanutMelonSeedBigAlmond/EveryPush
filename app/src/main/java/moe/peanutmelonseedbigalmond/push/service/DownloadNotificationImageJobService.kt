package moe.peanutmelonseedbigalmond.push.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.peanutmelonseedbigalmond.push.BaseApp
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.utils.SpanUtils
import moe.peanutmelonseedbigalmond.push.utils.notification.NotificationUtil
import moe.peanutmelonseedbigalmond.push.utils.notification.NotificationUtil.sendTextNotification

class DownloadNotificationImageJobService : JobService(),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {

    override fun onStartJob(params: JobParameters?): Boolean {
        if (params == null) return false

        val extra = params.extras
        val title = extra.getString("notificationTitle") ?: return false
        val content = extra.getString("notificationContent") ?: return false
        val type = extra.getString("notificationType") ?: return false
        val messageId = extra.getString("messageId") ?: return false

        val channelId = extra.getString("notificationChannelId")
        when (type) {
            "image" -> sendImageNotification(params, title, content, channelId, messageId)
            "markdown" -> sendMarkdownMessage(params, title, content, channelId, messageId)
            else -> return false
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        this.cancel()
    }

    private fun sendMarkdownMessage(
        jobParams: JobParameters,
        title: String,
        content: String,
        channelId: String?,
        messageId: String,
    ) {
        val spanned = BaseApp.markwon.toMarkdown(content)
        val firstImageUrl = SpanUtils.findImageUrlFromSpan(spanned).firstOrNull()
        val notificationId = sendTextNotification(title, spanned, channelId, messageId)
        if (firstImageUrl != null) {
            val imageRequest = ImageRequest.Builder(this)
                .data(firstImageUrl)
                .build()
            launch {
                try {
                    val bitmap =
                        withContext(Dispatchers.IO) {
                            imageLoader.execute(imageRequest).drawable as BitmapDrawable?
                        }
                    if (bitmap != null) {
                        NotificationUtil.sendMarkdownTextNotification(
                            title,
                            spanned,
                            channelId,
                            bitmap.bitmap,
                            notificationId,
                            messageId
                        )
                    }
                } catch (e: Exception) {
                    Log.w("TAG", "sendImageNotification: 下载通知图片失败")
                    e.printStackTrace()
                } finally {
                    jobFinished(jobParams, false)
                }
            }
        }
    }

    private fun sendImageNotification(
        jobParams: JobParameters,
        title: String,
        imageUrl: String,
        channelId: String?,
        messageId: String,
    ) {
        launch {
            val imageRequest = ImageRequest.Builder(BaseApp.context)
                .data(imageUrl)
                .build()
            val notificationId =
                sendTextNotification(
                    title,
                    getString(R.string.image_notification_brief),
                    channelId,
                    messageId
                )
            try {
                val bitmap =
                    withContext(Dispatchers.IO) { imageLoader.execute(imageRequest).drawable as BitmapDrawable? }
                if (bitmap != null) {
                    NotificationUtil.sendNotificationWithImage(
                        title,
                        bitmap.bitmap,
                        channelId,
                        notificationId,
                        messageId
                    )
                }
            } catch (e: Exception) {
                Log.w("TAG", "sendImageNotification: 下载通知图片失败")
                e.printStackTrace()
            } finally {
                jobFinished(jobParams, false)
            }
        }
    }
}