package moe.peanutmelonseedbigalmond.push.network.response

data class MessageGroupDetailResponse(
    val groupId: String?,
    val id: Long?,
    val name: String?,
    val messageCount: Int,
)