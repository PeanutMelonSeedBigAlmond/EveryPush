package moe.peanutmelonseedbigalmond.push.network.response

data class MessageResponse(
    val id: Long,
    val title: String,
    val text: String,
    val type: String,
    val sendAt: Long,
)