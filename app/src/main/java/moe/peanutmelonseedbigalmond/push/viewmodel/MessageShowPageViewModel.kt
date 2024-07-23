package moe.peanutmelonseedbigalmond.push.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import moe.peanutmelonseedbigalmond.push.bean.MessageDetail
import moe.peanutmelonseedbigalmond.push.network.Client

class MessageShowPageViewModel : ViewModel() {
    val message: LiveData<MessageDetail> = MutableLiveData(null)
    val refreshing= mutableStateOf(false)

    suspend fun refreshMessageDetail(id: Long) {
        val messageDetailResponse = Client.messageDetail(id)
        (this.message as MutableLiveData).value = MessageDetail(
            messageDetailResponse.id,
            messageDetailResponse.title,
            messageDetailResponse.content,
            messageDetailResponse.coverImgUrl,
            messageDetailResponse.type,
            messageDetailResponse.pushedAt,
            messageDetailResponse.uid,
            messageDetailResponse.messageGroupId,
            messageDetailResponse.encrypted
        )
    }
}