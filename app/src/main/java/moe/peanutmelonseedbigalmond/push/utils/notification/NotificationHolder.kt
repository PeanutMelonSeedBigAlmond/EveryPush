package moe.peanutmelonseedbigalmond.push.utils.notification

data class NotificationHolder(
    val title: String,
    val content: CharSequence,
    val id: Int,
    val topicId: String?,
    val messageId: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NotificationHolder) return false

        if (title != other.title) return false
        if (content != other.content) return false
        if (id != other.id) return false
        if (topicId != other.topicId) return false
        if (messageId != other.messageId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + id
        result = 31 * result + (topicId?.hashCode() ?: 0)
        result = 31 * result + messageId.hashCode()
        return result
    }
}