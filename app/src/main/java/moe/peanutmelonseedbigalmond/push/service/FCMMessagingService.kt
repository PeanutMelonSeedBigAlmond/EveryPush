package moe.peanutmelonseedbigalmond.push.service

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.text.Spanned
import android.util.Log
import androidx.core.text.getSpans
import coil.imageLoader
import coil.request.ImageRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.AsyncDrawableSpan
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
        if (UserTokenRepository.token.isBlank()) return // 非登录态

        val messageData = message.data
        val command = messageData["command"]?.toIntOrNull()
        val title = messageData["title"] ?: getString(R.string.title_default_notification)
        val messageText = messageData["text"]
        val messageType = messageData["type"]
        val topic = messageData["topic"]
        val topicName = messageData["topicName"]

        if (command == 0) { // 收到消息
            if (messageText != null) {
                if (topic != null && topicName != null) {
                    NotificationUtil.setupNotificationChannel(topic, topicName)
                }
                when (messageType) {
                    "text" -> sendTextNotification(title, messageText, topic)
                    "image" -> sendImageNotification(title, messageText, topic)
                    "markdown" -> sendMarkdownMessage(title, markwon.toMarkdown(messageText), topic)
                    else -> sendTextNotification(title, messageText, topic)
                }
            } else {
                Log.w("FCMMessagingService", "onMessageReceived: message body is null")
            }
        } else if (command == 1) { // 添加 topic
            if (topic != null && topicName != null) {
                NotificationUtil.setupNotificationChannel(topic, topicName)
            } else {
                Log.w(
                    "FCMMessagingService",
                    "onMessageReceived: 设置通知渠道失败：id=$topic, name=$topicName"
                )
            }
        } else if (command == 2) { // 删除 topic
            if (topic != null) {
                NotificationUtil.deleteNotificationChannel(topic)
            } else {
                Log.w("FCMMessagingService", "onMessageReceived: 删除通知渠道失败")
            }
        }
    }

    private fun sendTextNotification(title: String, content: String, channelId: String?): Int? {
        return NotificationUtil.sendTextNotification(title, content, channelId)
    }

    private fun sendMarkdownMessage(title: String, content: Spanned, channelId: String?): Int? {
        val contentStr = content.toString()
        val firstImage = content.getSpans<AsyncDrawableSpan>(0, content.length).firstOrNull()
        val notificationId =
            NotificationUtil.sendMarkdownTextNotification(title, contentStr, channelId)
        if (firstImage != null) {
            val imageUrl = firstImage.drawable.destination
            val imageRequest = ImageRequest.Builder(this)
                .data(imageUrl)
                .build()
            launch {
                try {
                    val bitmap =
                        withContext(Dispatchers.IO) { imageLoader.execute(imageRequest).drawable as BitmapDrawable? }
                    if (bitmap != null) {
                        NotificationUtil.sendMarkdownTextNotification(
                            title,
                            contentStr,
                            channelId,
                            bitmap.bitmap,
                            notificationId,
                        )
                    }
                } catch (e: Exception) {
                    Log.w("TAG", "sendImageNotification: 下载通知图片失败")
                    e.printStackTrace()
                }
            }
        }
        return notificationId
    }

    private fun sendImageNotification(title: String, imageUrl: String, channelId: String?) {
        val imageRequest = ImageRequest.Builder(this)
            .data(imageUrl)
            .build()
        launch {
            val notificationId =
                sendTextNotification(title, getString(R.string.image_notification_brief), channelId)
            try {
                val bitmap =
                    withContext(Dispatchers.IO) { imageLoader.execute(imageRequest).drawable as BitmapDrawable? }
                if (bitmap != null) {
                    NotificationUtil.sendNotificationWithImage(
                        title,
                        bitmap.bitmap,
                        channelId,
                        notificationId
                    )
                }
            } catch (e: Exception) {
                Log.w("TAG", "sendImageNotification: 下载通知图片失败")
                e.printStackTrace()
            }
        }
    }

    override fun onCreate() {
        Log.i("FCMMessagingService", "onCreate")
        super.onCreate()
    }

    override fun onDestroy() {
        Log.i("FCMMessagingService", "onDestroy")
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i("FCMMessagingService", "onUnbind")
        return super.onUnbind(intent)
    }
}