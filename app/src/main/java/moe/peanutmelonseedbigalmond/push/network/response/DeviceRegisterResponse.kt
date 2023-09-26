package moe.peanutmelonseedbigalmond.push.network.response

data class DeviceRegisterResponse(
    val id: Long,
    val uid: Long,
    val name: String,
    val deviceId: String,
)