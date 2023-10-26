package moe.peanutmelonseedbigalmond.push.utils

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BigPictureStyle
import androidx.core.app.NotificationCompat.BigTextStyle
import androidx.core.app.NotificationManagerCompat
import moe.peanutmelonseedbigalmond.push.App
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.ui.MainActivity

object NotificationUtil {
    private const val NOTIFICATION_REQUEST_CODE = 0

    private const val INTERNAL_NOTIFICATION_CHANNEL_PREFIX = "EveryPush.Internal"
    private const val USER_NOTIFICATION_CHANNEL_PREFIX = "EveryPush.User"
    private const val DEFAULT_NOTIFICATION_CHANNEL =
        "${INTERNAL_NOTIFICATION_CHANNEL_PREFIX}.Default"

    fun setupDefaultNotificationChannel() {
        val notificationManager = NotificationManagerCompat.from(App.context)
        if (SystemUtils.isNewerThanO()) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    DEFAULT_NOTIFICATION_CHANNEL,
                    App.context.getString(R.string.title_default_notification),
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
    }

    fun setupNotificationChannels(channelIdAndName: Map<String?, String?>) {
        if (!SystemUtils.isNewerThanO()) return
        val nm = NotificationManagerCompat.from(App.context)
        val inputChannelAndName = channelIdAndName.map { (k, v) ->
            if (k != null) {
                return@map "$USER_NOTIFICATION_CHANNEL_PREFIX.$k" to v!!
            } else {
                // 默认通知渠道
                return@map DEFAULT_NOTIFICATION_CHANNEL to App.context.getString(R.string.notification_channel_group_received_messages)
            }
        }.toMap()

        val registeredNotificationChannel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.notificationChannels.associate { it.id to it.name }
        } else {
            emptyMap()
        }

        val newNotificationChannel = inputChannelAndName - registeredNotificationChannel.keys
        val deletedNotificationChannel = registeredNotificationChannel - inputChannelAndName.keys

        newNotificationChannel.forEach { (id, name) ->
            nm.createNotificationChannel(
                NotificationChannel(
                    id.toString(),
                    name,
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }

        deletedNotificationChannel.forEach { (id, _) ->
            nm.deleteNotificationChannel(id.toString())
        }
    }

    fun setupNotificationChannel(
        topicId: String,
        name: String
    ) {
        val nm = NotificationManagerCompat.from(App.context)
        if (SystemUtils.isNewerThanO()) {
            nm.createNotificationChannel(
                NotificationChannel(
                    "$USER_NOTIFICATION_CHANNEL_PREFIX.$topicId",
                    name,
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
    }

    fun deleteNotificationChannel(id: String) {
        val nm = NotificationManagerCompat.from(App.context)
        if (SystemUtils.isNewerThanO()) {
            nm.deleteNotificationChannel(id)
        }
    }

    fun clearNotificationChannel() {
        val nm = NotificationManagerCompat.from(App.context)
        if (SystemUtils.isNewerThanO()) {
            nm.notificationChannels.forEach {
                nm.deleteNotificationChannel(it.id)
            }
        }
    }

    fun sendTextNotification(
        title: String,
        content: CharSequence,
        topicId: String?,
        id: Int? = null,
        time: Long = System.currentTimeMillis()
    ): Int? {
        val pendingIntent = if (topicId == null) {
            PendingIntent.getActivity(
                App.context, NOTIFICATION_REQUEST_CODE,
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("app://moe.peanutmelonseedbigalmond.push/pages/topicDetail"),
                    App.context,
                    MainActivity::class.java
                ),
                PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                App.context, NOTIFICATION_REQUEST_CODE,
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("app://moe.peanutmelonseedbigalmond.push/pages/topicDetail?topicId=$topicId"),
                    App.context,
                    MainActivity::class.java
                ),
                PendingIntent.FLAG_IMMUTABLE
            )
        }
        val mChannelId = getNotificationChannelId(topicId)
        val notification = NotificationCompat.Builder(App.context, mChannelId)
            .setContentTitle(title)
            .setContentText(content)
            .setWhen(time)
            .setSmallIcon(R.drawable.ic_notifications)
            .setLargeIcon(
                BitmapFactory.decodeResource(App.context.resources, R.drawable.ic_notifications)
            )
            .setContentIntent(pendingIntent)
            .apply {
                if (!SystemUtils.isNewerThanO()) {
                    priority = NotificationCompat.PRIORITY_HIGH
                }
            }
            .setAutoCancel(true)
            .build()
        return sendNotification(notification, id ?: time.toInt())
    }

    fun sendMarkdownTextNotification(
        title: String,
        content: String,
        topicId: String?,
        previewImage: Bitmap? = null,
        id: Int? = null,
        time: Long = System.currentTimeMillis()
    ): Int? {
        val pendingIntent = if (topicId == null) {
            PendingIntent.getActivity(
                App.context, NOTIFICATION_REQUEST_CODE,
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("app://moe.peanutmelonseedbigalmond.push/pages/topicDetail"),
                    App.context,
                    MainActivity::class.java
                ),
                PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                App.context, NOTIFICATION_REQUEST_CODE,
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("app://moe.peanutmelonseedbigalmond.push/pages/topicDetail?topicId=$topicId"),
                    App.context,
                    MainActivity::class.java
                ),
                PendingIntent.FLAG_IMMUTABLE
            )
        }
        val mChannelId = getNotificationChannelId(topicId)
        val notification = NotificationCompat.Builder(App.context, mChannelId)
            .setContentText(content)
            .setContentTitle(title)
            .setWhen(time)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentIntent(pendingIntent)
            .apply {
                if (!SystemUtils.isNewerThanO()) {
                    priority = NotificationCompat.PRIORITY_HIGH
                }
            }
            .apply {
                if (previewImage != null) {
                    setStyle(
                        BigPictureStyle()
                            .bigPicture(previewImage)
                            .bigLargeIcon(null as Bitmap?)
                    )
                        .setLargeIcon(previewImage)
                } else {
                    setStyle(
                        BigTextStyle()
                            .bigText(content)
                    )
                        .setLargeIcon(
                            BitmapFactory.decodeResource(
                                App.context.resources,
                                R.drawable.ic_notifications
                            )
                        )
                }
            }
            .setAutoCancel(true)
            .build()
        return sendNotification(notification, id ?: time.toInt())
    }

    fun sendNotificationWithImage(
        title: String,
        content: Bitmap,
        topicId: String?,
        id: Int? = null,
        time: Long = System.currentTimeMillis()
    ): Int? {
        val pendingIntent = if (topicId == null) {
            PendingIntent.getActivity(
                App.context, NOTIFICATION_REQUEST_CODE,
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("app://moe.peanutmelonseedbigalmond.push/pages/topicDetail"),
                    App.context,
                    MainActivity::class.java
                ),
                PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                App.context, NOTIFICATION_REQUEST_CODE,
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("app://moe.peanutmelonseedbigalmond.push/pages/topicDetail?topicId=$topicId"),
                    App.context,
                    MainActivity::class.java
                ),
                PendingIntent.FLAG_IMMUTABLE
            )
        }
        val mChannelId = getNotificationChannelId(topicId)
        val notification = NotificationCompat.Builder(App.context, mChannelId)
            .setContentTitle(title)
            .setLargeIcon(content)
            .setStyle(
                BigPictureStyle()
                    .bigPicture(content)
                    .bigLargeIcon(null as Bitmap?)
            )
            .setWhen(time)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentText(App.context.getString(R.string.image_notification_brief))
            .setContentIntent(pendingIntent)
            .apply {
                if (!SystemUtils.isNewerThanO()) {
                    priority = NotificationCompat.PRIORITY_HIGH
                }
            }
            .setAutoCancel(true)
            .build()
        return sendNotification(notification, id ?: time.toInt())
    }

    /**
     * 发送/修改现有通知
     * @param notification Notification
     * @param id Int?
     * @return Int?
     */
    private fun sendNotification(notification: Notification, id: Int): Int? {
        return if (ActivityCompat.checkSelfPermission(
                App.context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("NotificationUtil", "sendTextNotification: 通知权限被拒绝")
            null
        } else {
            val nm = NotificationManagerCompat.from(App.context)
            nm.notify(id, notification)
            id
        }
    }

    private fun getNotificationChannelId(topicId: String?): String {
        return if (topicId == null) {
            DEFAULT_NOTIFICATION_CHANNEL
        } else {
            "$USER_NOTIFICATION_CHANNEL_PREFIX.$topicId"
        }
    }
}