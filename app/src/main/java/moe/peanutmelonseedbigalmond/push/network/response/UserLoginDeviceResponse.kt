package moe.peanutmelonseedbigalmond.push.network.response

data class UserLoginDeviceResponse(
    val token: String,
    val name: String,
    val platform: String,
    val updatedAt: Long,
    val current:Boolean,
)