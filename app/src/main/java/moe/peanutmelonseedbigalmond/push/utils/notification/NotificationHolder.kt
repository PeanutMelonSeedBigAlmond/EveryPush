package moe.peanutmelonseedbigalmond.push.utils.notification

data class NotificationHolder(
    val title: String,
    val content: CharSequence,
    val id: Int,
    val topicId: String?,
)