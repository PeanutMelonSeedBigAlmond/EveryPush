package moe.peanutmelonseedbigalmond.push.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import moe.peanutmelonseedbigalmond.push.ui.component.page.Page
import moe.peanutmelonseedbigalmond.push.ui.data.DeviceData
import moe.peanutmelonseedbigalmond.push.ui.data.MessageData
import moe.peanutmelonseedbigalmond.push.ui.data.TokenData
import kotlin.properties.Delegates

class HomePageViewModel : ViewModel() {
    lateinit var onAppBarTitleChanged: (String) -> Unit
    var appBarTitle by Delegates.observable("") { _, _, newValue ->
        onAppBarTitleChanged(newValue)
    }

    val deviceList = mutableStateOf(emptyList<DeviceData>())
    val tokenList = mutableStateOf(emptyList<TokenData>())
    val messageList = mutableStateOf(emptyList<MessageData>())
    lateinit var devicePageOnFabClick: () -> Unit
    lateinit var keyPageOnFabClick: () -> Unit
    lateinit var messagePageOnFabClick: () -> Unit
    fun onFabClick(currentRoute: String?) {
        when (currentRoute) {
            Page.MainPage.Device.route -> devicePageOnFabClick()
            Page.MainPage.Keys.route -> keyPageOnFabClick()
            Page.MainPage.Message.route -> messagePageOnFabClick()
            else -> {}
        }
    }
}