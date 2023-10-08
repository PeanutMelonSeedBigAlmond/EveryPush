package moe.peanutmelonseedbigalmond.push.ui.viewmodel

import androidx.lifecycle.ViewModel
import moe.peanutmelonseedbigalmond.push.ui.component.page.Page
import kotlin.properties.Delegates

class HomePageViewModel : ViewModel() {
    lateinit var onAppBarTitleChanged: (String) -> Unit
    var appBarTitle by Delegates.observable("") { _, _, newValue ->
        onAppBarTitleChanged(newValue)
    }

    lateinit var devicePageOnFabClick: () -> Unit
    lateinit var keyPageOnFabClick: () -> Unit
    lateinit var topicPageOnFabClick: () -> Unit
    fun onFabClick(currentRoute: String?) {
        when (currentRoute) {
            Page.MainPage.Device.route -> devicePageOnFabClick()
            Page.MainPage.Keys.route -> keyPageOnFabClick()
            Page.MainPage.Topics.route -> topicPageOnFabClick()
            else -> {}
        }
    }
}