package moe.peanutmelonseedbigalmond.push.network

import com.haroldadmin.cnradapter.NetworkResponse
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.peanutmelonseedbigalmond.push.network.exception.TokenExpiredException
import moe.peanutmelonseedbigalmond.push.network.response.ErrorResponse
import moe.peanutmelonseedbigalmond.push.network.response.ResponseWrapper
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class Client(baseUrl: String) {
    var tokenExpiredAt = 0L
    var token = ""
    private val client = OkHttpClient.Builder()
        .build()
    private var service = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addCallAdapterFactory(NetworkResponseAdapterFactory())
        .addConverterFactory(GsonConverterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()
        .create(Api::class.java)

    suspend fun login(fcmToken: String) = withContext(Dispatchers.IO) {
        getResponseBody(service.login(fcmToken))
    }

    suspend fun getUserInfo()= withContext(Dispatchers.IO){
        checkTokenExpired()
        return@withContext getResponseBody(service.getUserInfo(token))
    }

    suspend fun ping() = withContext(Dispatchers.IO) {
        getResponseBody(service.ping())
    }

    suspend fun fetchDevices() = withContext(Dispatchers.IO) {
        checkTokenExpired()
        return@withContext getResponseBody(service.fetchDevices(token))
    }

    suspend fun registerDevice(name: String, fcmToken: String) = withContext(Dispatchers.IO) {
        checkTokenExpired()
        return@withContext getResponseBody(service.registerDevice(token, name, fcmToken))
    }

    suspend fun renameDevice(newName: String, id: Long) = withContext(Dispatchers.IO) {
        checkTokenExpired()
        return@withContext getResponseBody(service.renameDevice(token, id, newName))
    }

    suspend fun removeDevice(id: Long) = withContext(Dispatchers.IO) {
        checkTokenExpired()
        return@withContext getResponseBody(service.removeDevice(token, id))
    }

    suspend fun listToken() = withContext(Dispatchers.IO) {
        checkTokenExpired()
        return@withContext getResponseBody(service.listToken(token))
    }

    suspend fun generateKey() = withContext(Dispatchers.IO) {
        checkTokenExpired()
        return@withContext getResponseBody(service.generateToken(token))
    }

    suspend fun revokeToken(id: Long) = withContext(Dispatchers.IO) {
        checkTokenExpired()
        return@withContext getResponseBody(service.revokeToken(token, id))
    }

    suspend fun reGenerateToken(id: Long) = withContext(Dispatchers.IO) {
        checkTokenExpired()
        return@withContext getResponseBody(service.reGenerate(token, id))
    }

    suspend fun renameToken(id: Long, newName: String) = withContext(Dispatchers.IO) {
        checkTokenExpired()
        return@withContext getResponseBody(service.renameToken(token, id, newName))
    }

    suspend fun listMessages() = withContext(Dispatchers.IO) {
        checkTokenExpired()
        return@withContext getResponseBody(service.listMessages(token))
    }

    suspend fun pushTextMessage(token: String, message: String) = withContext(Dispatchers.IO) {
        return@withContext getResponseBody(service.pushTextMessage(token, message))
    }

    suspend fun deleteMessage(messageId:Long)= withContext(Dispatchers.IO){
        checkTokenExpired()
        return@withContext getResponseBody(service.deleteMessage(token,messageId))
    }

    private fun checkTokenExpired() {
        if (tokenExpiredAt < System.currentTimeMillis()) throw TokenExpiredException()
    }

    private fun <R> getResponseBody(response: NetworkResponse<ResponseWrapper<R>, ErrorResponse>): R {
        when (response) {
            is NetworkResponse.Success -> {
                val respData = response.body
                return respData.data!!
            }

            is NetworkResponse.ServerError -> {
                val message = response.body?.message
                throw Exception("Http status: ${response.code}, message=$message")
            }

            else -> throw (response as NetworkResponse.Error).error
        }
    }
}