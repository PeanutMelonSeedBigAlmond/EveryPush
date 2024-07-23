package moe.peanutmelonseedbigalmond.push.bean

import moe.peanutmelonseedbigalmond.push.emuration.MessageType

data class MessageExcerptInfo(
    val id: Long,
    val uid: String,
    val title: String?,
    val excerpt: String,
    val type: MessageType,
    val coverUrl: String?,
    val encrypted: Boolean,
    val messageGroupId: String?,
    val pushedAt: Long,
)