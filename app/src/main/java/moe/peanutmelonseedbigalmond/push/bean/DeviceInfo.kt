package moe.peanutmelonseedbigalmond.push.bean

import androidx.compose.runtime.Immutable

@Immutable
data class DeviceInfo(
    val id: Long,
    val uid: String,
    val name: String,
    val platform: String,
    val deviceToken: String,
)