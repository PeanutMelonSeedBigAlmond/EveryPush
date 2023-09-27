package moe.peanutmelonseedbigalmond.push.network.response

data class PushMessageResponse(
    val failedCount: Int,
    val messageId: Long,
    val pushedAt: Long,
)