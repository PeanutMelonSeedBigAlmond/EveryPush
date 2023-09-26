package moe.peanutmelonseedbigalmond.push.ui.component.page

sealed class Page(val route: String) {
    object Guide : Page("Guide")
    object ConfigureServer : Page("ConfigServer")
    object GoogleLogin : Page("GoogleLogin")
    object Main : Page("Main")

    object MainPage {
        object Device : Page("Device")
        object Keys : Page("Keys")
        object Message : Page("Message")
        object Setting : Page("Setting")
    }
}