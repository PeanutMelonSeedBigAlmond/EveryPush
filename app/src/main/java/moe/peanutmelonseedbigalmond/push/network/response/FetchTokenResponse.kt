package moe.peanutmelonseedbigalmond.push.network.response

data class FetchTokenResponse (
    val id: Long,
    val token: String,
    val name: String,
    val generatedAt:Long,
)