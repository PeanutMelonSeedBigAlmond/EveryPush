package moe.peanutmelonseedbigalmond.push.bean

data class UserLoginDeviceInfo(
    val token: String,
    val name: String,
    val platform: String,
    val updatedAt: Long,
    val current:Boolean,
)