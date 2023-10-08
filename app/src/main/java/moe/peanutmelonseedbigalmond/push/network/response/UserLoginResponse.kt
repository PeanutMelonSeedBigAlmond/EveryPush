package moe.peanutmelonseedbigalmond.push.network.response

data class UserLoginResponse(
    val token: String,
    val expiredAt: Long,
)