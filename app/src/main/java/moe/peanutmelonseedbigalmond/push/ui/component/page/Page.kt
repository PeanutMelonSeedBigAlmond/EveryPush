package moe.peanutmelonseedbigalmond.push.ui.component.page

sealed class Page(val route: String) {
    object Guide : Page("Guide")
    object ConfigureServer : Page("ConfigServer")
    object GoogleLogin : Page("GoogleLogin")
    object Main : Page("Main")

    object TopicDetail : Page("TopicDetail?topicId={topicId}") {
        object Args {
            const val TopicId = "topicId"
        }

        fun buildRouteWithArgs(topicId: String?): String {
            if (topicId == null) {
                return "TopicDetail"
            }
            return "TopicDetail?topicId=${topicId}"
        }
    }

    object Message : Page("Message")

    object MainPage {
        object Device : Page("Device")
        object Keys : Page("Keys")
        object Topics : Page("Topics")
        object Setting : Page("Setting")
    }
}