package moe.peanutmelonseedbigalmond.push.bean

import androidx.compose.runtime.Immutable

@Immutable
data class MessageGroup(
    val id: Long,
    val uid: String,
    val groupId: String,
    val name: String,
    val createdAt: Long,
)