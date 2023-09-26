package moe.peanutmelonseedbigalmond.push.network.response

data class FetchMessageResponse(
    val id: Long,
    val text: String,
    val title: String? = null,
    val type: String = "",
    val pushTime:Long=0L,
)