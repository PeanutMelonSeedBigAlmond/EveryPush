package moe.peanutmelonseedbigalmond.push.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.persistableBundleOf
import moe.peanutmelonseedbigalmond.push.BaseApp
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.emuration.MessageType
import moe.peanutmelonseedbigalmond.push.notification.PushMessageNotificationBean
import moe.peanutmelonseedbigalmond.push.service.NotificationImageDownloaderJobService
import moe.peanutmelonseedbigalmond.push.ui.MainActivity

object NotificationUtil {
    private const val notificationChannelGroupIdSuffix = "NotificationChannelGroup"

    private const val minPriorityMessagesNotificiationChannelSuffix = "MinPriorityMessages"
    private const val lowPriorityMessagesNotificiationChannelSuffix = "LowPriorityMessages"
    private const val normalPriorityMessagesNotificiationChannelSuffix = "NormalPriorityMessages"
    private const val highPriorityMessagesNotificiationChannelSuffix = "HighPriorityMessages"

    private val notificationManagerCompat by lazy { NotificationManagerCompat.from(BaseApp.context) }

    init {
        ensureDefaultNotificationChannelCreated()
    }

    fun createNotificationChannelGroupByTopicId(
        messageGroupId: String?,
        messageGroupTitle: String
    ) {
        val minPriorityMessageNotificationChannel: String
        val lowPriorityMessageNotificationChannel: String
        val normalPriorityMessageNotificationChannel: String
        val highPriorityMessageNotificationChannel: String
        val notificationChannelGroupId: String
        if (messageGroupId == null) {
            minPriorityMessageNotificationChannel = minPriorityMessagesNotificiationChannelSuffix
            lowPriorityMessageNotificationChannel = lowPriorityMessagesNotificiationChannelSuffix
            normalPriorityMessageNotificationChannel =
                normalPriorityMessagesNotificiationChannelSuffix
            highPriorityMessageNotificationChannel = highPriorityMessagesNotificiationChannelSuffix
            notificationChannelGroupId = notificationChannelGroupIdSuffix
        } else {
            minPriorityMessageNotificationChannel =
                "$messageGroupId.$minPriorityMessagesNotificiationChannelSuffix"
            lowPriorityMessageNotificationChannel =
                "$messageGroupId.$lowPriorityMessagesNotificiationChannelSuffix"
            normalPriorityMessageNotificationChannel =
                "$messageGroupId.$normalPriorityMessagesNotificiationChannelSuffix"
            highPriorityMessageNotificationChannel =
                "$messageGroupId.$highPriorityMessagesNotificiationChannelSuffix"
            notificationChannelGroupId = "$messageGroupId.$notificationChannelGroupIdSuffix"
        }
        notificationManagerCompat.createNotificationChannelGroup(
            NotificationChannelGroupCompat.Builder(notificationChannelGroupId)
                .setName(messageGroupTitle)
                .build()
        )

        notificationManagerCompat.createNotificationChannel(
            NotificationChannelCompat.Builder(
                minPriorityMessageNotificationChannel,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationManager.IMPORTANCE_MIN else NotificationCompat.PRIORITY_MIN
            ).setName("Min priority messages (<1)").setGroup(notificationChannelGroupId).build()
        )
        notificationManagerCompat.createNotificationChannel(
            NotificationChannelCompat.Builder(
                lowPriorityMessageNotificationChannel,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationManager.IMPORTANCE_LOW else NotificationCompat.PRIORITY_LOW
            ).setName("Low priority messages (1-3)").setGroup(notificationChannelGroupId).build()
        )
        notificationManagerCompat.createNotificationChannel(
            NotificationChannelCompat.Builder(
                normalPriorityMessageNotificationChannel,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationManager.IMPORTANCE_DEFAULT else NotificationCompat.PRIORITY_DEFAULT
            ).setName("Normal priority messages (4-7)").setGroup(notificationChannelGroupId).build()
        )
        notificationManagerCompat.createNotificationChannel(
            NotificationChannelCompat.Builder(
                highPriorityMessageNotificationChannel,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationManager.IMPORTANCE_HIGH else NotificationCompat.PRIORITY_HIGH
            ).setName("High priority messages (>7)").setGroup(notificationChannelGroupId).build()
        )
    }

    private fun ensureDefaultNotificationChannelCreated() {
        createNotificationChannelGroupByTopicId(null, "Default")
    }

    private fun ensureNotificationChannelGroupByTopicIdCreated(
        messageGroupId: String?,
        messageGroupTitle: String
    ) {
        createNotificationChannelGroupByTopicId(messageGroupId, messageGroupTitle)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getNotificationChannelGroups(): Map<String, String> {
        return notificationManagerCompat.notificationChannelGroups.filter {
            it.id != notificationChannelGroupIdSuffix
        }.associate {
            it.id to it.name.toString()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMessageGroups(): Map<String, String> {
        return getNotificationChannelGroups().map {
            it.key.removeSuffix(".$notificationChannelGroupIdSuffix") to it.value
        }.toMap()
    }

    fun deleteNotificationChannelGroup(messageGroupId: String, messageGroupTitle: String) {
        notificationManagerCompat.deleteNotificationChannelGroup(
            "$messageGroupId.$notificationChannelGroupIdSuffix"
        )
    }

    fun renameNotificationChannelGroup(messageGroupId: String, newTitle: String) {
        createNotificationChannelGroupByTopicId(
            messageGroupId,
            newTitle
        )
    }

    @RequiresPermission("android.permission.POST_NOTIFICATIONS")
    fun pushNotificationByMessageGroup(
        notificationBean: PushMessageNotificationBean
    ) {
        if (notificationBean.messageGroupId == null || notificationBean.messageGroupTitle == null) {
            ensureDefaultNotificationChannelCreated()
        } else {
            ensureNotificationChannelGroupByTopicIdCreated(
                notificationBean.messageGroupId,
                notificationBean.messageGroupTitle
            )
        }

        val uri =
            Uri.parse("everypush://moe.peanutmelonseedbigalmond.push/pages/message?mesageId=${notificationBean.id}")
        val intent = Intent(Intent.ACTION_VIEW, uri, BaseApp.context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            BaseApp.context,
            notificationBean.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(
            BaseApp.context,
            getNotificationChannelId(notificationBean.messageGroupId, notificationBean.priority)
        )
            .setContentTitle(notificationBean.title ?: "未命名消息")
            .setContentText(if (notificationBean.type == MessageType.Picture) "图片" else notificationBean.content)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentIntent(pendingIntent)
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(true)
            .build()
        notificationManagerCompat.notify(notificationBean.id, notification)

        val imgUrl = if (notificationBean.type == MessageType.Picture) {
            notificationBean.coverImgUrl ?: notificationBean.content
        } else {
            notificationBean.coverImgUrl
        }
        if (imgUrl != null) {
            val jobScheduler =
                BaseApp.context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            val job = JobInfo.Builder(
                notificationBean.id,
                ComponentName(BaseApp.context, NotificationImageDownloaderJobService::class.java)
            )
                .setExtras(
                    persistableBundleOf(
                        "notificationId" to notificationBean.id,
                        "coverImgUrl" to imgUrl,
                        "messageGroupId" to notificationBean.messageGroupId,
                        "priority" to notificationBean.priority,
                        "messageContent" to notificationBean.content,
                        "messageTitle" to notificationBean.title
                    )
                )
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build()
            jobScheduler.schedule(job)
        }
    }

    private fun getNotificationChannelId(messageGroupId: String?, priority: Int): String {
        val suffix = when {
            priority < 1 -> minPriorityMessagesNotificiationChannelSuffix
            priority < 3 -> lowPriorityMessagesNotificiationChannelSuffix
            priority < 7 -> normalPriorityMessagesNotificiationChannelSuffix
            else -> highPriorityMessagesNotificiationChannelSuffix
        }
        return if (messageGroupId == null) {
            suffix
        } else {
            "$messageGroupId.$suffix"
        }
    }

    @RequiresPermission("android.permission.POST_NOTIFICATIONS")
    fun repostNotificationWithImage(
        messageGroupId: String?,
        priority: Int,
        notificationId: Int,
        messageTitle: String?,
        messageContent: String,
        image: Bitmap
    ) {
        val uri =
            Uri.parse("everypush://moe.peanutmelonseedbigalmond.push/pages/message?mesageId=${notificationId}")
        val intent = Intent(Intent.ACTION_VIEW, uri, BaseApp.context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            BaseApp.context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(
            BaseApp.context,
            getNotificationChannelId(messageGroupId, priority)
        )
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(image))
            .setContentText(messageContent)
            .setContentTitle(messageTitle)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentIntent(pendingIntent)
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(true)
            .build()
        notificationManagerCompat.notify(notificationId, notification)
    }
}