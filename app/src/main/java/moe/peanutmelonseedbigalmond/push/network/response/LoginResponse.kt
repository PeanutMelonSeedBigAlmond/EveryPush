package moe.peanutmelonseedbigalmond.push.network.response

data class LoginResponse(
    val token:String,
    val expiredAt:Long,
)