package moe.peanutmelonseedbigalmond.push.viewmodel

import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import moe.peanutmelonseedbigalmond.push.bean.MessageGroup
import moe.peanutmelonseedbigalmond.push.network.Client
import moe.peanutmelonseedbigalmond.push.repository.paging.MessageGroupListPagingRepository
import moe.peanutmelonseedbigalmond.push.utils.NotificationUtil

class MessageGroupPageViewModel : ViewModel() {
    val messageGroupList = Pager(PagingConfig(pageSize = 20)) {
        MessageGroupListPagingRepository()
    }.flow.cachedIn(viewModelScope)
    val refreshing = mutableStateOf(false)

    val addMessageGroupDialogShow = mutableStateOf(false)
    val currentRenamingMessageGroup = mutableStateOf<MessageGroup?>(null)

    suspend fun addMessageGroup(id: String, name: String) {
        Client.createMessageGroup(id, name)
    }

    suspend fun deleteMessageGroup(id: String) {
        Client.removeMessageGroup(id)
    }

    suspend fun renameMessageGroup(id: String, name: String) {
        Client.renameMessageGroup(id, name)
    }

    suspend fun syncMessageGroups() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationGroups = NotificationUtil.getMessageGroups()
            val response = Client.syncMessageGroups(notificationGroups)
            response.deleted.forEach {
                NotificationUtil.deleteNotificationChannelGroup(it.id, it.name)
            }
            response.created.forEach {
                NotificationUtil.createNotificationChannelGroupByTopicId(it.id, it.name)
            }
            response.renamed.forEach {
                NotificationUtil.renameNotificationChannelGroup(it.id, it.name)
            }
        }
    }
}