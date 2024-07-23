package moe.peanutmelonseedbigalmond.push.network.response

import moe.peanutmelonseedbigalmond.push.emuration.MessageType

data class MessageExcerptInfoResponse(
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