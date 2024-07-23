package moe.peanutmelonseedbigalmond.push.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import moe.peanutmelonseedbigalmond.push.network.Client
import moe.peanutmelonseedbigalmond.push.repository.paging.MessageExcerptListPagingRepository

class MessageGroupDetailPageViewModel(
    private val groupId: String?
) : ViewModel() {
    val title = mutableStateOf("")
    val refreshing = mutableStateOf(false)

    val messageExcerptList = Pager(PagingConfig(pageSize = 20)) {
        MessageExcerptListPagingRepository(groupId)
    }.flow.cachedIn(viewModelScope)

    suspend fun updateMessageGroupInfo(id: String?) {
        if (id != null) {
            val messageGroupInfo = Client.messageGroupInfo(id)
            title.value = messageGroupInfo.name!!
        } else {
            title.value = "默认"
        }
    }

    suspend fun deleteMessage(messageId: Long){
        Client.deleteMessage(messageId)
    }
}