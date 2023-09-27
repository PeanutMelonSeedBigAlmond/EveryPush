package moe.peanutmelonseedbigalmond.push.ui.data

data class MessageData(
    val type: String,
    val title: String,
    val content: String,
    val id: Long,
    val sendTime: Long,
) {
    object Type {
        const val TEXT = "text"
        const val IMAGE = "image"
        const val MARKDOWN = "markdown"
    }
}