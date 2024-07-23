package moe.peanutmelonseedbigalmond.push.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import moe.peanutmelonseedbigalmond.push.bean.UserLoginDeviceInfo
import moe.peanutmelonseedbigalmond.push.network.Client

class LoggedInDeviceManagePageViewModel : ViewModel() {
    val loggedInDevices = mutableStateListOf<UserLoginDeviceInfo>()
    val refreshing = mutableStateOf(false)

    suspend fun refreshLoggedInDevices() {
        val response = Client.userClients()
        loggedInDevices.clear()
        loggedInDevices.addAll(response.map {
            return@map UserLoginDeviceInfo(it.token, it.name, it.platform, it.updatedAt, it.current)
        })
    }

    suspend fun logoutOthers(){
        Client.logoutOthers()
    }

    suspend fun logout(token: String){
        Client.logout(token)
    }
}