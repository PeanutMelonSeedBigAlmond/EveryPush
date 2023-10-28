package moe.peanutmelonseedbigalmond.push.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import moe.peanutmelonseedbigalmond.push.network.Client
import moe.peanutmelonseedbigalmond.push.repository.AppConfigurationRepository
import moe.peanutmelonseedbigalmond.push.repository.UserTokenRepository
import moe.peanutmelonseedbigalmond.push.ui.data.DeviceData
import moe.peanutmelonseedbigalmond.push.ui.data.TokenData
import moe.peanutmelonseedbigalmond.push.ui.data.TopicData
import moe.peanutmelonseedbigalmond.push.utils.NotificationUtil
import kotlin.properties.Delegates

class GlobalViewModel : ViewModel() {
    lateinit var oneTapClient: SignInClient
    lateinit var signInRequest: BeginSignInRequest
    lateinit var auth: FirebaseAuth
    lateinit var client: Client
    var fcmToken: String? = null

    //region 数据
    val deviceList = mutableStateOf(emptyList<DeviceData>())
    val tokenList = mutableStateOf(emptyList<TokenData>())
    var topicsList by Delegates.observable<List<TopicData>?>(emptyList()) { _, _, newValue ->
        if (!newValue.isNullOrEmpty()) {
            updateAndSetupNotificationChannel(newValue)
        }
    }
    //endregion

    var url by Delegates.observable("") { _, _, newValue ->
        client = Client(newValue)
    }
    var token by Delegates.observable("") { _, _, newValue ->
        client.token = newValue
    }

    fun clearUserConfig() {
        url = "http://localhost"
        token = ""
        saveUserConfig()
    }

    /**
     * 保存设置
     */
    fun saveUserConfig() {
        AppConfigurationRepository.endpointUrl = url
        UserTokenRepository.token = token
    }

    /**
     * 读取设置
     */
    fun loadConfig() {
        url = AppConfigurationRepository.endpointUrl
        token = UserTokenRepository.token
    }

    private fun updateAndSetupNotificationChannel(topicData: List<TopicData>) {
        NotificationUtil.setupNotificationChannels(topicData.associate { it.id to it.name })
    }
}