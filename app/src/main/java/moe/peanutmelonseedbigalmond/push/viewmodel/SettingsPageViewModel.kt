package moe.peanutmelonseedbigalmond.push.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import moe.peanutmelonseedbigalmond.push.bean.UserInfo
import moe.peanutmelonseedbigalmond.push.network.Client
import kotlin.coroutines.cancellation.CancellationException

class SettingsPageViewModel : ViewModel() {
    val userInfo: LiveData<UserInfo> = MutableLiveData(null)

    suspend fun refreshUserInfo() {
        val userInfoResponse = Client.userInfo()
        (this.userInfo as MutableLiveData).postValue(
            UserInfo(
                userInfoResponse.uid,
                userInfoResponse.name,
                userInfoResponse.email,
                userInfoResponse.avatarUrl
            )
        )
    }

    suspend fun logout() {
        try {
            Client.logout()
        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            e.printStackTrace()
        }

    }
}