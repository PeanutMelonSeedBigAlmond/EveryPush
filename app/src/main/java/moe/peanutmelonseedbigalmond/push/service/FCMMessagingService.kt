package moe.peanutmelonseedbigalmond.push.service

import android.graphics.drawable.BitmapDrawable
import android.text.Spanned
import android.util.Log
import coil.imageLoader
import coil.request.ImageRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.simple.ext.SimpleExtPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.repository.AppConfigurationRepository
import moe.peanutmelonseedbigalmond.push.repository.UserTokenRepository
import moe.peanutmelonseedbigalmond.push.utils.NotificationUtil

class FCMMessagingService : FirebaseMessagingService(),
    CoroutineScope by CoroutineScope(Dispatchers.Main) {
    private val markwon by lazy {
        Markwon.builder(this)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(this))
            .usePlugin(TaskListPlugin.create(this))
            .usePlugin(HtmlPlugin.create())
            .usePlugin(ImagesPlugin.create())
            .usePlugin(CoilImagesPlugin.create(this, this.imageLoader))
            .usePlugin(LinkifyPlugin.create())
            .usePlugin(SimpleExtPlugin.create())
            .build()
    }

    override fun onNewToken(token: String) {
        AppConfigurationRepository.fcmPushToken = token
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val messageData = message.data
        val title = messageData["title"] ?: getString(R.string.default_notification_title)
        val messageText = messageData["text"]
        val messageType = messageData["type"]

        if (messageText != null) {
            if (UserTokenRepository.token.isNotBlank()) { // 登录态
                when (messageType) {
                    "text" -> sendTextNotification(title, messageText)
                    "image" -> sendImageNotification(title, messageText)
                    else -> sendMarkdownMessage(title, markwon.toMarkdown(messageText))
                }
            }
        } else {
            Log.w("FCMMessagingService", "onMessageReceived: message body is null")
        }
    }

    private fun sendTextNotification(title: String, content: String): Int? {
        return NotificationUtil.sendTextNotification(title, content)
    }

    private fun sendMarkdownMessage(title: String, content: Spanned): Int? {
        return NotificationUtil.sendMarkdownTextNotification(title, content)
    }

    private fun sendImageNotification(title: String, imageUrl: String) {
        val imageRequest = ImageRequest.Builder(this)
            .data(imageUrl)
            .build()
        launch {
            val notificationId =
                sendTextNotification(title, getString(R.string.image_notification_brief))
            try {
                val bitmap =
                    withContext(Dispatchers.IO) { imageLoader.execute(imageRequest).drawable as BitmapDrawable? }
                if (bitmap != null) {
                    NotificationUtil.sendNotificationWithImage(
                        getString(R.string.default_notification_title),
                        bitmap.bitmap,
                        notificationId
                    )
                }
            } catch (e: Exception) {
                Log.w("TAG", "sendImageNotification: 下载通知图片失败")
                e.printStackTrace()
            }
        }
    }
}