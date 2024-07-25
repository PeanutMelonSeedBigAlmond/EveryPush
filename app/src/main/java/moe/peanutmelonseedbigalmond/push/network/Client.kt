package moe.peanutmelonseedbigalmond.push.network

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.peanutmelonseedbigalmond.push.exception.ApiException
import moe.peanutmelonseedbigalmond.push.network.response.ResponseWrapper
import moe.peanutmelonseedbigalmond.push.network.response.UserLoginResponse
import moe.peanutmelonseedbigalmond.push.utils.DeviceUtil
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Client {
    var serverAddress = "http://127.0.0.1"
        set(value) {
            if (
                value != field && value.isNotBlank()
                && (value.startsWith("http://") || value.startsWith("https://"))
            ) {
                field = value
                service = Retrofit.Builder()
                    .client(okhttpClient)
                    .baseUrl(value)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build()
                    .create(Api::class.java)
            }
        }
    var userToken = ""
        set(value) {
            if (field != value) {
                field = value
            }
        }
    private val gson = Gson()
    private val okhttpClient = OkHttpClient.Builder().build()
    private var service = Retrofit.Builder()
        .client(okhttpClient)
        .baseUrl(serverAddress)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(Api::class.java)

    suspend fun ping() = withContext(Dispatchers.IO) {
        val response = service.pingServer().body()
        require(response == "pong")
    }

    suspend fun login(idToken: String): UserLoginResponse = withContext(Dispatchers.IO) {
        val response = service.login(idToken, "Android", DeviceUtil.getDeviceName()).body()
        return@withContext response
    }

    suspend fun listDevices(pageIndex: Int = 0, pageSize: Int = 20) = withContext(Dispatchers.IO) {
        val response = service.listDevices(userToken, pageIndex, pageSize).body()
        return@withContext response
    }

    suspend fun registerDevice(deviceToken: String) = withContext(Dispatchers.IO) {
        service.registerDevice(deviceToken, userToken, "Android", DeviceUtil.getDeviceName())
            .body()
    }

    suspend fun renameDevice(id: Long, name: String) = withContext(Dispatchers.IO) {
        service.renameDevice(userToken, id, name).body()
    }

    suspend fun removeDevice(id: Long) = withContext(Dispatchers.IO) {
        service.removeDevice(id, userToken).body()
    }

    suspend fun listKey(pageIndex: Int = 0, pageSize: Int = 20) = withContext(Dispatchers.IO) {
        val response = service.listKey(userToken, pageIndex, pageSize).body()
        return@withContext response
    }

    suspend fun removeKey(id: Long) = withContext(Dispatchers.IO) {
        val response = service.removeKey(id, userToken).body()
        return@withContext response
    }

    suspend fun renameKey(id: Long, name: String) = withContext(Dispatchers.IO) {
        val response = service.renameKey(userToken, id, name).body()
        return@withContext response
    }

    suspend fun generateKey() = withContext(Dispatchers.IO) {
        val response = service.generateKey(userToken).body()
        return@withContext response
    }

    suspend fun resetKey(id: Long) = withContext(Dispatchers.IO) {
        val response = service.resetKey(id, userToken).body()
        return@withContext response
    }

    suspend fun listMessageGroup(pageIndex: Int = 0, pageSize: Int = 20) =
        withContext(Dispatchers.IO) {
            val response = service.listMessageGroup(userToken, pageIndex, pageSize).body()
            return@withContext response
        }

    suspend fun renameMessageGroup(id: String, name: String) = withContext(Dispatchers.IO) {
        val response = service.renameMessageGroup(id, userToken, name).body()
        return@withContext response
    }

    suspend fun removeMessageGroup(id: String) = withContext(Dispatchers.IO) {
        val response = service.removeMessageGroup(id, userToken).body()
        return@withContext response
    }

    suspend fun syncMessageGroups(clientMessageGroups: Map<String, String>) =
        withContext(Dispatchers.IO) {
            val response = service.syncMessageGroups(
                userToken,
                ids = clientMessageGroups.keys.toList(),
                names = clientMessageGroups.values.toList()
            ).body()
            return@withContext response
        }

    suspend fun messageGroupInfo(id: String) = withContext(Dispatchers.IO) {
        val response = service.messageGroupInfo(id, userToken).body()
        return@withContext response
    }

    suspend fun listMessage(groupId: String?, pageIndex: Int = 0, pageSize: Int = 20) =
        withContext(Dispatchers.IO) {
            return@withContext service.listMessages(groupId, userToken, pageIndex, pageSize).body()
        }

    suspend fun listAllMessage(pageIndex: Int = 0, pageSize: Int = 20) =
        withContext(Dispatchers.IO) {
            return@withContext service.listAllMessages(userToken, pageIndex, pageSize).body()
        }

    suspend fun messageDetail(id: Long) = withContext(Dispatchers.IO) {
        val response = service.messageDetail(id, userToken).body()
        return@withContext response
    }

    suspend fun deleteMessage(id: Long) = withContext(Dispatchers.IO) {
        val response = service.deleteMessage(id, userToken).body()
        return@withContext response
    }

    suspend fun userInfo() = withContext(Dispatchers.IO) {
        val response = service.userInfo(userToken).body()
        return@withContext response
    }

    suspend fun userClients() = withContext(Dispatchers.IO) {
        val response = service.userClients(userToken).body()
        return@withContext response
    }

    suspend fun logoutOthers() = withContext(Dispatchers.IO) {
        val response = service.logoutOthers(userToken).body()
        return@withContext response
    }

    suspend fun logout(token: String = userToken) = withContext(Dispatchers.IO) {
        val response = service.logout(token).body()
        return@withContext response
    }

    @Throws(ApiException::class)
    private inline fun <reified T> ResponseWrapper<T>.body(): T {
        if (errorCode != "00000") {
            throw ApiException(errorCode, message!!)
        } else {
            return data
        }
    }
}