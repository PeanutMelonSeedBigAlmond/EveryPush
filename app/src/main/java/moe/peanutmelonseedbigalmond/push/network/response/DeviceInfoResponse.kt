package moe.peanutmelonseedbigalmond.push.network.response

data class DeviceInfoResponse(
    val id: Long,
    val uid: String,
    val name: String,
    val platform: String,
    val deviceToken: String,
)