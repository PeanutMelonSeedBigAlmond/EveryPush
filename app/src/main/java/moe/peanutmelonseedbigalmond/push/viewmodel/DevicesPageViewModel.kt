package moe.peanutmelonseedbigalmond.push.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import moe.peanutmelonseedbigalmond.push.bean.DeviceInfo
import moe.peanutmelonseedbigalmond.push.network.Client
import moe.peanutmelonseedbigalmond.push.repository.paging.DeviceListPagingRepository

class DevicesPageViewModel : ViewModel() {
    val refreshing = mutableStateOf(false)

    val showRenameDialog = mutableStateOf(false)

    val devices = Pager(PagingConfig(pageSize = 20)) {
        DeviceListPagingRepository()
    }.flow.cachedIn(viewModelScope)

    val currentRenameDevice = mutableStateOf<DeviceInfo?>(null)

    suspend fun getDeviceToken():String{
        return FirebaseMessaging.getInstance().token.await()
    }

    suspend fun registerDevice() {
        val deviceToken = getDeviceToken()
        Client.registerDevice(deviceToken)
    }

    suspend fun renameDevice(id: Long, name: String) {
        Client.renameDevice(id, name)
    }

    suspend fun removeDevice(id: Long){
        Client.removeDevice(id)
    }
}