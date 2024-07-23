package moe.peanutmelonseedbigalmond.push.repository.config

import com.dylanc.mmkv.MMKVOwner
import com.dylanc.mmkv.mmkvString

object ServerConfig : MMKVOwner("RemoteConfig") {
    var serverUrl by mmkvString("")
    var token by mmkvString("")

    fun saveServerConfig(token: String, serverAddress: String) {
        serverUrl = serverAddress
        ServerConfig.token = token
    }

    fun isConfigValid(): Boolean {
        return serverUrl.isNotBlank()
                && (serverUrl.startsWith("http://") || serverUrl.startsWith("https://"))
                && token.isNotBlank()
    }
}