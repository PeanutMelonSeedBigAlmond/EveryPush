package moe.peanutmelonseedbigalmond.push.network

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.peanutmelonseedbigalmond.push.App
import moe.peanutmelonseedbigalmond.push.network.exception.ServerException
import moe.peanutmelonseedbigalmond.push.network.response.DeviceResponse
import moe.peanutmelonseedbigalmond.push.network.response.GenericUserInfoResponse
import moe.peanutmelonseedbigalmond.push.network.response.GraphqlError
import moe.peanutmelonseedbigalmond.push.network.response.KeyResponse
import moe.peanutmelonseedbigalmond.push.network.response.MessageResponse
import moe.peanutmelonseedbigalmond.push.network.response.PingResponse
import moe.peanutmelonseedbigalmond.push.network.response.TopicDetailResponse
import moe.peanutmelonseedbigalmond.push.network.response.TopicResponse
import moe.peanutmelonseedbigalmond.push.network.response.UserLoginResponse
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.nio.charset.Charset
import kotlin.reflect.KClass

class Client(baseUrl: String) {
    var tokenExpiredAt = 0L
    var token = ""
        get() = if (tokenExpiredAt <= System.currentTimeMillis()) throw Exception("Token expired") else field
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
    private val gson = Gson()

    @Throws(ServerException::class)
    private suspend inline fun <reified T : Any> doGraphqlQuery(
        query: String,
        operationName: String = "",
        variables: Map<String, Any?> = emptyMap()
    ) =
        withContext(Dispatchers.IO) {
            val mediaType = "application/json".toMediaType()
            val requestMap = mapOf(
                "query" to query,
                "operationName" to operationName,
                "variables" to variables
            )
            val body = gson.toJson(requestMap).toRequestBody(mediaType)

            val response = service.graphql(body)
            response.getAsJsonArray("errors")
                ?.map { it.toObject(GraphqlError::class) }
                ?.firstOrNull()
                ?.run { throw ServerException(message) }
            return@withContext response.getAsJsonObject("data")!!
                .toObject<T>(T::class)
        }

    suspend fun userLogin(fcmToken: String) = withContext(Dispatchers.IO) {
        val data = doGraphqlQuery<JsonObject>(
            query = readQueryStatement("user_login"),
            variables = mapOf("firebaseToken" to fcmToken)
        ).getAsJsonObject("login")

        return@withContext gson.fromJson<UserLoginResponse>(data, UserLoginResponse::class.java)
    }

    suspend fun ping() = withContext(Dispatchers.IO) {
        return@withContext doGraphqlQuery<PingResponse>(readQueryStatement("ping"))
    }

    suspend fun listDevices() = withContext(Dispatchers.IO) {
        val data = doGraphqlQuery<JsonObject>(
            readQueryStatement("list_device"), variables = mapOf(
                "token" to token
            )
        )
        return@withContext data.getAsJsonArray("devices").map { it.toObject(DeviceResponse::class) }
    }

    suspend fun registerDevice(deviceId: String, name: String) = withContext(Dispatchers.IO) {
        doGraphqlQuery<DeviceResponse>(
            readQueryStatement("device_register"),
            variables = mapOf(
                "params" to mapOf(
                    "token" to token,
                    "deviceId" to deviceId,
                    "type" to "android",
                    "name" to name
                )
            )
        )
    }

    suspend fun removeDevice(id: Long) = withContext(Dispatchers.IO) {
        doGraphqlQuery<DeviceResponse>(
            readQueryStatement("remove_device"),
            variables = mapOf(
                "token" to token,
                "id" to id
            )

        )
    }

    suspend fun renameDevice(id: Long, newName: String) = withContext(Dispatchers.IO) {
        doGraphqlQuery<DeviceResponse>(
            readQueryStatement("rename_device"),
            variables = mapOf(
                "params" to mapOf(
                    "token" to token,
                    "id" to id,
                    "newName" to newName
                )
            )
        )
    }

    suspend fun generateKey() = withContext(Dispatchers.IO) {
        doGraphqlQuery<JsonObject>(
            readQueryStatement("generate_key"),
            variables = mapOf(
                "token" to token
            )
        ).getAsJsonObject("token")
            .toObject(KeyResponse::class)
    }

    suspend fun listToken() = withContext(Dispatchers.IO) {
        doGraphqlQuery<JsonObject>(
            readQueryStatement("list_token"),
            variables = mapOf(
                "token" to token
            )
        ).getAsJsonArray("tokenList")
            .map { it.toObject(KeyResponse::class) }
    }

    suspend fun reGenerateToken(id: Long) = withContext(Dispatchers.IO) {
        doGraphqlQuery<JsonObject>(
            readQueryStatement("regenerate_token"),
            variables = mapOf(
                "token" to token,
                "id" to id
            )
        ).getAsJsonObject("token")
            .toObject(KeyResponse::class)
    }

    suspend fun revokeToken(id: Long) = withContext(Dispatchers.IO) {
        doGraphqlQuery<JsonObject>(
            readQueryStatement("delete_token"),
            variables = mapOf(
                "token" to token,
                "id" to id
            )
        ).getAsJsonObject("token")
            .toObject(KeyResponse::class)
    }

    suspend fun renameToken(id: Long, newName: String) = withContext(Dispatchers.IO) {
        doGraphqlQuery<JsonObject>(
            readQueryStatement("rename_token"),
            variables = mapOf(
                "param" to mapOf(
                    "token" to token,
                    "id" to id,
                    "newName" to newName
                )
            )
        ).getAsJsonObject("token")
            .toObject(KeyResponse::class)
    }

    suspend fun getUserInfo() = withContext(Dispatchers.IO) {
        doGraphqlQuery<GenericUserInfoResponse>(
            readQueryStatement("generic_user_info"),
            variables = mapOf(
                "token" to token
            )
        )
    }

    suspend fun listTopicAndLatestMessage() = withContext(Dispatchers.IO) {
        doGraphqlQuery<JsonObject>(
            readQueryStatement("list_topic_and_latest_message"),
            variables = mapOf(
                "token" to token
            )
        ).getAsJsonArray("topics")
            .map { it.toObject(TopicResponse::class) }
    }

    suspend fun topicDetail(topicId: String?) = withContext(Dispatchers.IO) {
        doGraphqlQuery<JsonObject>(
            readQueryStatement("topic_detail"),
            variables = mapOf(
                "token" to token,
                "topicId" to topicId
            )
        ).getAsJsonObject("topic")
            .toObject(TopicDetailResponse::class)
    }

    suspend fun deleteMessage(id: Long) = withContext(Dispatchers.IO) {
        doGraphqlQuery<JsonObject>(
            readQueryStatement("delete_message"),
            variables = mapOf(
                "token" to token,
                "id" to id
            )
        ).getAsJsonObject("message")
            .toObject(MessageResponse::class)
    }

    suspend fun pushMessage(
        pushToken: String,
        text: String,
        title: String? = null,
        type: String? = "text",
        topicId: String? = null
    ) = withContext(Dispatchers.IO) {
        doGraphqlQuery<JsonObject>(
            readQueryStatement("push_message"),
            variables = mapOf(
                "params" to mapOf(
                    "pushToken" to pushToken,
                    "text" to text,
                    "title" to title,
                    "type" to type,
                    "topicId" to topicId,
                )
            )
        ).getAsJsonObject("message")
    }

    suspend fun deleteTopic(
        topicId: String
    ) = withContext(Dispatchers.IO) {
        doGraphqlQuery<JsonObject>(
            readQueryStatement("delete_topic"),
            variables = mapOf(
                "id" to topicId,
                "token" to token
            )
        ).getAsJsonArray("topics")
    }

    suspend fun addTopic(
        topicId: String,
        topicName: String,
    ) = withContext(Dispatchers.IO) {
        doGraphqlQuery<JsonObject>(
            readQueryStatement("add_topic"),
            variables = mapOf(
                "id" to topicId,
                "name" to topicName,
                "token" to token
            )
        ).getAsJsonObject("topic")
    }

    private fun readQueryStatement(name: String): String {
        return App.context.assets.open("graphql/$name.graphql.query").use {
            return@use it.readBytes().toString(Charset.defaultCharset())
        }
    }

    private fun <T : Any> JsonElement.toObject(type: KClass<T>): T {
        return gson.fromJson(this, type.java)
    }
}