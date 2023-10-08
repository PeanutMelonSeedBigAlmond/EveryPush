package moe.peanutmelonseedbigalmond.push.network.response

data class TopicDetailResponse(
    val id: String?,
    val name: String?,
    val messages: List<MessageResponse>,
)