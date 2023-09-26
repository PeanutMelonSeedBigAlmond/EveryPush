package moe.peanutmelonseedbigalmond.push.repository

import com.dylanc.mmkv.MMKVOwner
import com.dylanc.mmkv.mmkvString

object AppConfigurationRepository : MMKVOwner(mmapID = "AppConfiguration") {
    var endpointUrl by mmkvString("http://localhost")
    var fcmPushToken by mmkvString()
}