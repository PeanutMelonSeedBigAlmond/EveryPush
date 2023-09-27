package moe.peanutmelonseedbigalmond.push.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import moe.peanutmelonseedbigalmond.push.network.Client
import moe.peanutmelonseedbigalmond.push.repository.AppConfigurationRepository
import moe.peanutmelonseedbigalmond.push.repository.UserTokenRepository
import kotlin.properties.Delegates

class GlobalViewModel : ViewModel() {
    lateinit var oneTapClient: SignInClient
    lateinit var signInRequest: BeginSignInRequest
    lateinit var auth: FirebaseAuth
    lateinit var client: Client
    var fcmToken: String? = null

    var url by Delegates.observable("") { _, _, newValue ->
        client = Client(newValue)
    }
    var token by Delegates.observable("") { _, _, newValue ->
        client.token = newValue
    }
    var tokenExpiredAt by Delegates.observable(0L) { _, _, newValue ->
        client.tokenExpiredAt = newValue
    }

    fun clearUserConfig() {
        url = "http://localhost"
        token = ""
        tokenExpiredAt = 0L
        saveUserConfig()
    }

    /**
     * 保存设置
     */
    fun saveUserConfig() {
        AppConfigurationRepository.endpointUrl = url
        UserTokenRepository.token = token
        UserTokenRepository.expiredAt = tokenExpiredAt
    }

    /**
     * 读取设置
     */
    fun loadConfig() {
        url = AppConfigurationRepository.endpointUrl
        token = UserTokenRepository.token
        tokenExpiredAt = UserTokenRepository.expiredAt
    }
}