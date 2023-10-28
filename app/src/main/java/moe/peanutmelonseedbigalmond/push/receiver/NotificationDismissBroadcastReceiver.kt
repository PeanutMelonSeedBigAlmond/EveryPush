package moe.peanutmelonseedbigalmond.push.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import moe.peanutmelonseedbigalmond.push.utils.notification.NotificationUtil

/**
 * 通知移除回调
 */
class NotificationDismissBroadcastReceiver : BroadcastReceiver() {
    companion object {
        const val NOTIFICATION_DELETED_ACTION =
            "moe.peanutmelonseedbigalmond.push.action.NOTIFICATION_DELETED"
    }

    enum class NotificationType {
        NOTIFICATION, // 消息通知
        NOTIFICATION_SUMMARY, // 摘要通知
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return
        if (intent.action != NOTIFICATION_DELETED_ACTION) return

        @Suppress("DEPRECATION")
        val notificationType = intent.getSerializableExtra("notificationType")
        if (notificationType !is NotificationType) return
        val notificationId = intent.getIntExtra("notificationId", 0)
        val notificationGroupId = intent.getStringExtra("notificationGroupId") ?: return

        when (notificationType) {
            NotificationType.NOTIFICATION -> {
                NotificationUtil.cancelNotification(notificationId, notificationGroupId)
            }

            NotificationType.NOTIFICATION_SUMMARY -> {
                NotificationUtil.cancelNotificationSummary(notificationGroupId)
            }
        }
    }
}