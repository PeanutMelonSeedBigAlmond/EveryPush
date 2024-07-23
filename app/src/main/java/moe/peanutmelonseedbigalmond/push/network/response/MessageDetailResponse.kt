package moe.peanutmelonseedbigalmond.push.network.response

import moe.peanutmelonseedbigalmond.push.emuration.MessageType

data class MessageDetailResponse (
    val id:Long,
    val title:String?,
    val content:String,
    val coverImgUrl:String?,
    val type: MessageType,
    val pushedAt:Long,
    val uid:String,
    val messageGroupId:String?,
    val encrypted:Boolean,
)