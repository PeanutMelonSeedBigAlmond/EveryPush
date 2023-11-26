package moe.peanutmelonseedbigalmond.push.utils.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.text.Spanned
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BigPictureStyle
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import moe.peanutmelonseedbigalmond.push.BaseApp
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.receiver.NotificationDismissBroadcastReceiver
import moe.peanutmelonseedbigalmond.push.ui.MainActivity
import moe.peanutmelonseedbigalmond.push.utils.SystemUtils
import kotlin.random.Random

object NotificationUtil {
    private const val NOTIFICATION_VIEW_REQUEST_CODE = 1
    private const val NOTIFICATION_DELETE_REQUEST_CODE = 2

    private const val INTERNAL_NOTIFICATION_CHANNEL_PREFIX = "EveryPush.Internal"
    private const val USER_NOTIFICATION_CHANNEL_PREFIX = "EveryPush.User"
    private const val DEFAULT_NOTIFICATION_CHANNEL =
        "$INTERNAL_NOTIFICATION_CHANNEL_PREFIX.Default"

    private const val NOTIFICATION_GROUP_ID_PREFIX = "EveryPush.NotificationGroup"
    private const val NOTIFICATION_GROUP_ID_DEFAULT = "$NOTIFICATION_GROUP_ID_PREFIX.Default"
    private const val NOTIFICATION_GROUP_ID_USER_PREFIX = "$NOTIFICATION_GROUP_ID_PREFIX.User"

    fun setupDefaultNotificationChannel() {
        val notificationManager = NotificationManagerCompat.from(BaseApp.context)
        if (SystemUtils.isNewerThanO()) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    DEFAULT_NOTIFICATION_CHANNEL,
                    BaseApp.context.getString(R.string.title_default_notification),
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }
    }

    fun setupNotificationChannels(channelIdAndName: Map<String?, String?>) {
        if (!SystemUtils.isNewerThanO()) return
        val nm = NotificationManagerCompat.from(BaseApp.context)
        val inputChannelAndName = channelIdAndName.map { (k, v) ->
            if (k != null) {
                return@map "$USER_NOTIFICATION_CHANNEL_PREFIX.$k" to v!!
            } else {
                // 默认通知渠道
                return@map DEFAULT_NOTIFICATION_CHANNEL to BaseApp.context.getString(R.string.notification_channel_group_received_messages)
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
        val nm = NotificationManagerCompat.from(BaseApp.context)
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
        val nm = NotificationManagerCompat.from(BaseApp.context)
        if (SystemUtils.isNewerThanO()) {
            nm.deleteNotificationChannel(id)
        }
    }

    fun clearNotificationChannel() {
        val nm = NotificationManagerCompat.from(BaseApp.context)
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
        messageId: String,
        id: Int? = null,
    ): Int? {
        val currentTime = System.currentTimeMillis()
        val notificationId = id ?: currentTime.toInt()
        val mChannelId = getNotificationChannelId(topicId)
        val notificationHolder =
            NotificationHolder(title, content.toString(), notificationId, topicId, messageId)
        val notification = NotificationCompat.Builder(BaseApp.context, mChannelId)
            .setContentTitle(title)
            .setContentText(content)
            .setWhen(currentTime)
            .setLargeIcon(
                BitmapFactory.decodeResource(BaseApp.context.resources, R.drawable.ic_notifications)
            )
        return sendNotification(SendNotificationData(notification, notificationHolder))
    }

    fun sendMarkdownTextNotification(
        title: String,
        content: Spanned,
        topicId: String?,
        previewImage: Bitmap,
        id: Int? = null,
        messageId: String,
    ): Int? {
        val currentTime = System.currentTimeMillis()
        val notificationId = id ?: currentTime.toInt()
        val mChannelId = getNotificationChannelId(topicId)
        val notificationHolder =
            NotificationHolder(title, content, notificationId, topicId, messageId)
        val notification = NotificationCompat.Builder(BaseApp.context, mChannelId)
            .setContentText(content)
            .setContentTitle(title)
            .setWhen(currentTime)
            .setStyle(
                BigPictureStyle()
                    .bigPicture(previewImage)
                    .bigLargeIcon(null as Bitmap?)
            )
            .setLargeIcon(previewImage)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    BaseApp.context.resources,
                    R.drawable.ic_notifications
                )
            )
        return sendNotification(SendNotificationData(notification, notificationHolder))
    }

    fun sendNotificationWithImage(
        title: String,
        content: Bitmap,
        topicId: String?,
        id: Int? = null,
        messageId: String,
    ): Int? {
        val currentTime = System.currentTimeMillis()
        val notificationId = id ?: currentTime
        val mChannelId = getNotificationChannelId(topicId)
        val notificationHolder = NotificationHolder(
            title,
            BaseApp.context.getString(R.string.image_notification_brief),
            notificationId.toInt(),
            topicId,
            messageId
        )
        val notificationBuilder = NotificationCompat.Builder(BaseApp.context, mChannelId)
            .setContentTitle(title)
            .setLargeIcon(content)
            .setStyle(
                BigPictureStyle()
                    .bigPicture(content)
                    .bigLargeIcon(null as Bitmap?)
            )
            .setWhen(currentTime)
            .setContentText(BaseApp.context.getString(R.string.image_notification_brief))
        return sendNotification(SendNotificationData(notificationBuilder, notificationHolder))
    }

    fun cancelNotification(notificationId: Int, notificationGroupId: String) {
        val nm = NotificationManagerCompat.from(BaseApp.context)
        BaseApp.notifications[notificationGroupId]?.remove { it.id == notificationId }
        nm.cancel(notificationId)
    }

    fun cancelNotificationSummary(notificationGroupId: String) {
        val nm = NotificationManagerCompat.from(BaseApp.context)
        BaseApp.notifications.remove(notificationGroupId)?.forEach {
            nm.cancel(it.id)
        }
        BaseApp.summaryNotifications.remove(notificationGroupId)?.let {
            nm.cancel(it)
        }
    }

    /**
     * 发送/修改现有通知
     */
    private fun sendNotification(sendNotificationData: SendNotificationData): Int? {
        if (ActivityCompat.checkSelfPermission(
                BaseApp.context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("NotificationUtil", "sendTextNotification: 通知权限被拒绝")
            return null
        }

        //region 通知
        val notificationGroupId = getNotificationGroupId(sendNotificationData.holder.topicId)
        val notification = sendNotificationData.notificationBuilder
            .applyCommonOptions()
            .setContentIntent(getPendingIntent(sendNotificationData.holder))
            .setGroup(notificationGroupId)
            .apply {
                try {
                    val dismissIntent =
                        Intent(BaseApp.context, NotificationDismissBroadcastReceiver::class.java)
                    dismissIntent.action =
                        NotificationDismissBroadcastReceiver.NOTIFICATION_DELETED_ACTION
                    dismissIntent.putExtra("notificationId", sendNotificationData.holder.id)
                    dismissIntent.putExtra("notificationGroupId", notificationGroupId)
                    dismissIntent.putExtra(
                        "notificationType",
                        NotificationDismissBroadcastReceiver.NotificationType.NOTIFICATION
                    )
                    this.setDeleteIntent(
                        PendingIntent.getBroadcast(
                            BaseApp.context,
                            sendNotificationData.holder.hashCode() + 31 * NOTIFICATION_DELETE_REQUEST_CODE,
                            dismissIntent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            .build()
        val nm = NotificationManagerCompat.from(BaseApp.context)
        nm.notify(sendNotificationData.holder.id, notification)

        if (BaseApp.notifications[notificationGroupId] == null) {
            BaseApp.notifications[notificationGroupId] =
                mutableListOf(sendNotificationData.holder)
        } else {
            BaseApp.notifications[notificationGroupId]?.let { list ->
                list.remove { it.id == sendNotificationData.holder.id }
                list.add(sendNotificationData.holder)
            }
        }
        //endregion

        val notificationGroupContentList = BaseApp.notifications[notificationGroupId]?.map {
            it.content
        } ?: return sendNotificationData.holder.id

        //region 通知摘要
        val summaryId = obtainSummaryNotificationId(sendNotificationData.holder.topicId)
        val summaryNotification = NotificationCompat.Builder(
            BaseApp.context,
            getNotificationChannelId(sendNotificationData.holder.topicId)
        )
            .setContentTitle(
                BaseApp.context.resources.getQuantityString(
                    R.plurals.notification_summary_title,
                    notificationGroupContentList.size,
                    notificationGroupContentList.size
                )
            )
            .applyCommonOptions()
            .setStyle(NotificationCompat.InboxStyle()
                .setSummaryText(
                    BaseApp.context.resources.getQuantityString(
                        R.plurals.notification_summary_title,
                        notificationGroupContentList.size,
                        notificationGroupContentList.size
                    )
                )
                .also { style ->
                    notificationGroupContentList.forEach {
                        style.addLine(it)
                    }
                })
            .setGroup(notificationGroupId)
            .setGroupSummary(true)
            .apply {
                try {
                    val dismissIntent =
                        Intent(BaseApp.context, NotificationDismissBroadcastReceiver::class.java)
                    dismissIntent.action =
                        NotificationDismissBroadcastReceiver.NOTIFICATION_DELETED_ACTION
                    dismissIntent.putExtra("notificationId", summaryId)
                    dismissIntent.putExtra("notificationGroupId", notificationGroupId)
                    dismissIntent.putExtra(
                        "notificationType",
                        NotificationDismissBroadcastReceiver.NotificationType.NOTIFICATION_SUMMARY
                    )
                    this.setDeleteIntent(
                        PendingIntent.getBroadcast(
                            BaseApp.context,
                            notificationGroupId.hashCode(),
                            dismissIntent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            .build()
        nm.notify(summaryId, summaryNotification)
        BaseApp.summaryNotifications[notificationGroupId] = summaryId
        //endregion

        return sendNotificationData.holder.id
    }

    private fun NotificationCompat.Builder.applyCommonOptions(): NotificationCompat.Builder {
        return this.setSmallIcon(R.drawable.ic_notifications)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .apply {
                if (!SystemUtils.isNewerThanO()) {
                    priority = NotificationCompat.PRIORITY_HIGH
                }
            }
    }

    private fun getNotificationChannelId(topicId: String?): String {
        return if (topicId == null) {
            DEFAULT_NOTIFICATION_CHANNEL
        } else {
            "$USER_NOTIFICATION_CHANNEL_PREFIX.$topicId"
        }
    }

    private fun getNotificationGroupId(topicId: String?): String {
        return if (topicId == null) {
            NOTIFICATION_GROUP_ID_DEFAULT
        } else {
            "$NOTIFICATION_GROUP_ID_USER_PREFIX.$topicId"
        }
    }

    private fun getPendingIntent(notificationHolder: NotificationHolder): PendingIntent {
        val uriStr = if (notificationHolder.topicId == null) {
            "app://moe.peanutmelonseedbigalmond.push/pages/topicDetail"
        } else {
            "app://moe.peanutmelonseedbigalmond.push/pages/topicDetail?topicId=${notificationHolder.topicId}"
        }

        val intent =
            Intent(Intent.ACTION_VIEW, uriStr.toUri(), BaseApp.context, MainActivity::class.java)
        val pendingIntent = TaskStackBuilder.create(BaseApp.context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(
                notificationHolder.hashCode() + 31 * NOTIFICATION_VIEW_REQUEST_CODE,
                PendingIntent.FLAG_IMMUTABLE
            )
        }
        return pendingIntent!!
    }

    private data class SendNotificationData(
        val notificationBuilder: NotificationCompat.Builder,
        val holder: NotificationHolder
    )

    private fun <E> MutableList<E>.remove(predicate: (E) -> Boolean): Boolean {
        var removed = false
        val iterator = this.iterator()
        while (iterator.hasNext()) {
            if (predicate(iterator.next())) {
                iterator.remove()
                removed = true
            }
        }
        return removed
    }

    private fun obtainSummaryNotificationId(topicId: String?): Int {
        if (getNotificationGroupId(topicId) in BaseApp.summaryNotifications) {
            return BaseApp.summaryNotifications[getNotificationGroupId(topicId)]!!
        } else {
            return Random.nextInt(0, Int.MAX_VALUE)
        }
    }
}