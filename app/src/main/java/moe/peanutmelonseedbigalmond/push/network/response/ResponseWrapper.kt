package moe.peanutmelonseedbigalmond.push.network.response

data class ResponseWrapper<T>(
    val data: T,
    val errorCode: String,
    val message: String?,
)