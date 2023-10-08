package moe.peanutmelonseedbigalmond.push.network.response

data class TopicResponse(
    val id: String,
    val name: String,
    val latestMessage: MessageResponse?,
)