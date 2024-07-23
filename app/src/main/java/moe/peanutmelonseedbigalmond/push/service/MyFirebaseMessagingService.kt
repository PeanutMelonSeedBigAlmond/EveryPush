package moe.peanutmelonseedbigalmond.push.service

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.JsonObject
import moe.peanutmelonseedbigalmond.push.emuration.MessageType
import moe.peanutmelonseedbigalmond.push.notification.PushMessageNotificationBean
import moe.peanutmelonseedbigalmond.push.utils.NotificationUtil
import moe.peanutmelonseedbigalmond.push.utils.SystemUtils

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val gson = Gson()

    @SuppressLint("MissingPermission")
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val messageData = gson
            .fromJson(message.data["data"]!!,JsonObject::class.java)
        val command=messageData["command"].asString
        val messageBean=gson.fromJson(messageData["data"].asJsonObject,FCMMessageBean::class.java)
        Log.v("MyFirebaseMessagingService", "command=$command, data=$messageBean")
        val pushMessageNotificationBean = PushMessageNotificationBean(
            messageBean.id.toInt(),
            messageBean.title,
            messageBean.coverImgUrl,
            messageBean.excerpt,
            messageBean.type,
            messageBean.messageGroupId,
            messageBean.messageGroupTitle,
            messageBean.priority
        )
        if (SystemUtils.hasNotificationPermission()) {
            NotificationUtil.pushNotificationByMessageGroup(pushMessageNotificationBean)
        }
    }

    data class FCMMessageBean(
        val uid: String,
        val title: String?,
        val excerpt: String,
        val type: MessageType,
        val priority: Int,
        val coverImgUrl: String?,
        val encrypted: Boolean,
        val messageGroupId: String?,
        val messageGroupTitle: String?,
        val id: Long,
    )
}