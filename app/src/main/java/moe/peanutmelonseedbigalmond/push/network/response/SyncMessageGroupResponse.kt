package moe.peanutmelonseedbigalmond.push.network.response

data class SyncMessageGroupResponse(
    val deleted:List<Item>,
    val created:List<Item>,
    val renamed:List<Item>,
){
    data class Item(
        val id:String,
        val name:String,
    )
}