package moe.peanutmelonseedbigalmond.push.network.response

data class ResponseWrapper<Response>(
    val message: String? = null,
    val data: Response? = null,
)