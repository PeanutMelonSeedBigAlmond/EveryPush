package moe.peanutmelonseedbigalmond.push.repository

import com.dylanc.mmkv.MMKVOwner
import com.dylanc.mmkv.mmkvLong
import com.dylanc.mmkv.mmkvString

object UserTokenRepository : MMKVOwner(mmapID = "FCMUser") {
    var token by mmkvString("")
    var expiredAt by mmkvLong(0L)
}