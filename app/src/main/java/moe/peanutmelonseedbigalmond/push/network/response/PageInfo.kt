package moe.peanutmelonseedbigalmond.push.network.response

data class PageInfo (
    val hasNextPage:Boolean,
    val hasPreviousPage:Boolean,
    val count:Int,
    val total:Int,
    val firstCursor:String?,
    val lastCursor:String?,
)