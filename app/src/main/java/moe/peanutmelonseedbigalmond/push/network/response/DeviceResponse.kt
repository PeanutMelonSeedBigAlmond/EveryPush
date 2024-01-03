package moe.peanutmelonseedbigalmond.push.network.response

data class DeviceResponse(
    val items:List<DeviceItem>,
    val pageInfo:PageInfo,
){
    data class DeviceItem(
        val id: Long,
        val deviceId: String,
        val name: String,
        val type: String,
        val cursor:String?,
    )
}