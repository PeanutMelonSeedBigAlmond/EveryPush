package moe.peanutmelonseedbigalmond.push.service

import android.graphics.drawable.BitmapDrawable
import android.util.Log
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
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
    override fun onNewToken(token: String) {
        AppConfigurationRepository.fcmPushToken = token
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val messageData = message.data
        val title = messageData["title"] ?: getString(R.string.default_notification_title)
        val messageText = messageData["text"]
        val messageType = messageData["type"] ?: "text"

        val messageBody = when (messageType) {
            "text", "image" -> {
                messageText
            }

            "markdown" -> {
                getString(R.string.markdown_norification_brief)
            }

            else -> {
                messageText
            }
        }
        if (messageBody != null) {
            if (UserTokenRepository.token.isNotBlank()) { // 登录态
                when (messageType) {
                    "text" -> sendTextMessage(title, messageBody)
                    "image" -> sendImageNotification(title, messageBody)
                    else -> sendTextMessage(title, messageBody)
                }
            }
        } else {
            Log.w("FCMMessagingService", "onMessageReceived: message body is null")
        }
    }

    private fun sendTextMessage(title: String, content: String): Int {
        return NotificationUtil.sendNotification(this, title, content)
    }

    private fun sendImageNotification(title: String, imageUrl: String) {
        val imageRequest = ImageRequest.Builder(this)
            .data(imageUrl)
            .build()
        launch {
            val notificationId =
                sendTextMessage(title, getString(R.string.image_notification_brief))
            try {
                val bitmap =
                    withContext(Dispatchers.IO) { imageLoader.execute(imageRequest).drawable as BitmapDrawable? }
                if (bitmap != null) {
                    NotificationUtil.sendNotificationWithImage(
                        this@FCMMessagingService,
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