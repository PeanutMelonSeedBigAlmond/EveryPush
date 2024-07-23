package moe.peanutmelonseedbigalmond.push.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import moe.peanutmelonseedbigalmond.push.network.Client
import moe.peanutmelonseedbigalmond.push.repository.paging.AllMessageListPagingRepository

class AllMessagesListPageViewModel : ViewModel() {
    val refreshing = mutableStateOf(false)

    val messageExcerptList = Pager(PagingConfig(pageSize = 20)) {
        AllMessageListPagingRepository()
    }.flow.cachedIn(viewModelScope)

    suspend fun deleteMessage(messageId: Long){
        Client.deleteMessage(messageId)
    }
}