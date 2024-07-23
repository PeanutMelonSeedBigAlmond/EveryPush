package moe.peanutmelonseedbigalmond.push.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import moe.peanutmelonseedbigalmond.push.network.Client

class ChooseServerPageViewModel:ViewModel() {
    val loading = mutableStateOf(false)
    val serverAddress= mutableStateOf("")
    fun updateServerBaseUrl(baseUrl:String){
        Client.serverAddress=baseUrl
    }
    suspend fun pingServer(){
        Client.ping()
    }
}