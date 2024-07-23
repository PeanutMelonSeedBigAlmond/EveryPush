package moe.peanutmelonseedbigalmond.push.bean

import androidx.compose.runtime.Immutable

@Immutable
data class KeyInfo(
    val id: Long,
    val name: String,
    val key: String,
    val createdAt: Long,
    val uid: String,
)