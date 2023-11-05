package moe.peanutmelonseedbigalmond.push.service

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.os.persistableBundleOf
import coil.imageLoader
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
import moe.peanutmelonseedbigalmond.push.App
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.repository.AppConfigurationRepository
import moe.peanutmelonseedbigalmond.push.repository.UserTokenRepository
import moe.peanutmelonseedbigalmond.push.utils.notification.NotificationUtil
import kotlin.random.Random

class FCMMessagingService : FirebaseMessagingService() {
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

        val messageId = message.messageId!!
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
                    "text" -> sendTextNotification(title, messageText, topic, messageId)
                    "image" -> sendImageNotification(title, messageText, topic, messageId)
                    "markdown" -> sendMarkdownMessage(title, messageText, topic, messageId)
                    else -> sendTextNotification(title, messageText, topic, messageId)
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

    private fun sendTextNotification(
        title: String,
        content: String,
        channelId: String?,
        messageId: String
    ): Int? {
        return NotificationUtil.sendTextNotification(title, content, channelId, messageId)
    }

    private fun sendMarkdownMessage(
        title: String,
        content: String,
        channelId: String?,
        messageId: String
    ) {
        val jobService = App.context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName =
            ComponentName(App.context, DownloadNotificationImageJobService::class.java)
        val extras = persistableBundleOf(
            "notificationChannelId" to channelId,
            "notificationTitle" to title,
            "notificationContent" to content,
            "notificationType" to "markdown",
            "messageId" to messageId,
        )
        val job = JobInfo.Builder(Random.nextInt(1, Int.MAX_VALUE), componentName)
            .setExtras(extras)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .build()
        jobService.schedule(job)
    }

    private fun sendImageNotification(
        title: String,
        imageUrl: String,
        channelId: String?,
        messageId: String
    ) {
        val jobService = App.context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName =
            ComponentName(App.context, DownloadNotificationImageJobService::class.java)
        val extras = persistableBundleOf(
            "notificationChannelId" to channelId,
            "notificationTitle" to title,
            "notificationContent" to imageUrl,
            "notificationType" to "image",
            "messageId" to messageId,
        )
        val job = JobInfo.Builder(Random.nextInt(1, Int.MAX_VALUE), componentName)
            .setExtras(extras)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .build()
        jobService.schedule(job)
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