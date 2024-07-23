package moe.peanutmelonseedbigalmond.push.bean

import moe.peanutmelonseedbigalmond.push.emuration.MessageType

data class MessageDetail(
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