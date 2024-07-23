package moe.peanutmelonseedbigalmond.push.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import moe.peanutmelonseedbigalmond.push.BaseApp
import moe.peanutmelonseedbigalmond.push.bean.KeyInfo
import moe.peanutmelonseedbigalmond.push.network.Client
import moe.peanutmelonseedbigalmond.push.repository.paging.KeyListPagingRepository

class KeysPageViewModel : ViewModel() {
    val refreshing = mutableStateOf(false)
    val currentRenamingKey = mutableStateOf<KeyInfo?>(null)

    val keyList = Pager(PagingConfig(pageSize = 20)) {
        KeyListPagingRepository()
    }.flow.cachedIn(viewModelScope)

    suspend fun generateKey() {
        Client.generateKey()
    }

    suspend fun renameKey(id: Long, name: String) {
        Client.renameKey(id, name)
    }

    fun copyKey(keyInfo: KeyInfo) {
        val clipboardManager =
            BaseApp.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(ClipData.newPlainText("key", keyInfo.key))
    }

    suspend fun deleteKey(id: Long) {
        Client.removeKey(id)
    }

    suspend fun resetKey(id: Long) {
        Client.resetKey(id)
    }
}