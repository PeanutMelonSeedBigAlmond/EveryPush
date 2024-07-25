package moe.peanutmelonseedbigalmond.push.network

import moe.peanutmelonseedbigalmond.push.network.response.DeviceInfoResponse
import moe.peanutmelonseedbigalmond.push.network.response.KeyResponse
import moe.peanutmelonseedbigalmond.push.network.response.MessageDetailResponse
import moe.peanutmelonseedbigalmond.push.network.response.MessageExcerptInfoResponse
import moe.peanutmelonseedbigalmond.push.network.response.MessageGroupDetailResponse
import moe.peanutmelonseedbigalmond.push.network.response.MessageGroupResponse
import moe.peanutmelonseedbigalmond.push.network.response.ResponseWrapper
import moe.peanutmelonseedbigalmond.push.network.response.SyncMessageGroupResponse
import moe.peanutmelonseedbigalmond.push.network.response.UserInfoResponse
import moe.peanutmelonseedbigalmond.push.network.response.UserLoginDeviceResponse
import moe.peanutmelonseedbigalmond.push.network.response.UserLoginResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface Api {
    @GET("api/serverInfo/ping")
    suspend fun pingServer(): ResponseWrapper<String>

    @POST("api/user/login")
    @FormUrlEncoded
    suspend fun login(
        @Field("idToken") idToken: String,
        @Field("platform") platform: String,
        @Field("deviceName") deviceName: String
    ): ResponseWrapper<UserLoginResponse>

    @POST("api/device/list")
    @FormUrlEncoded
    suspend fun listDevices(
        @Field("token") token: String,
        @Field("pageIndex") pageIndex: Int,
        @Field("pageSize") pageSize: Int
    ): ResponseWrapper<List<DeviceInfoResponse>>

    @POST("api/device/register")
    @FormUrlEncoded
    suspend fun registerDevice(
        @Field("deviceToken") deviceToken: String,
        @Field("token") token: String,
        @Field("platform") platform: String,
        @Field("name") name: String,
    ): ResponseWrapper<Unit>

    @POST("api/device/rename")
    @FormUrlEncoded
    suspend fun renameDevice(
        @Field("token") token: String,
        @Field("id") id: Long,
        @Field("name") name: String,
    ): ResponseWrapper<Unit>

    @POST("api/device/remove")
    @FormUrlEncoded
    suspend fun removeDevice(
        @Field("id") id: Long,
        @Field("token") token: String,
    ): ResponseWrapper<Unit>

    @POST("api/key/list")
    @FormUrlEncoded
    suspend fun listKey(
        @Field("token") token: String,
        @Field("pageIndex") pageIndex: Int,
        @Field("pageSize") pageSize: Int
    ): ResponseWrapper<List<KeyResponse>>

    @POST("api/key/generate")
    @FormUrlEncoded
    suspend fun generateKey(
        @Field("token") token: String,
    ): ResponseWrapper<Unit>

    @POST("api/key/rename")
    @FormUrlEncoded
    suspend fun renameKey(
        @Field("token") token: String,
        @Field("id") id: Long,
        @Field("name") name: String,
    ): ResponseWrapper<Unit>

    @POST("api/key/remove")
    @FormUrlEncoded
    suspend fun removeKey(
        @Field("id") id: Long,
        @Field("token") token: String,
    ): ResponseWrapper<Unit>

    @POST("api/key/reset")
    @FormUrlEncoded
    suspend fun resetKey(
        @Field("id") id: Long,
        @Field("token") token: String,
    ): ResponseWrapper<Unit>

    @POST("api/messageGroup/remove")
    @FormUrlEncoded
    suspend fun removeMessageGroup(
        @Field("id") id: String,
        @Field("token") token: String,
    ): ResponseWrapper<Unit>

    @POST("api/messageGroup/list")
    @FormUrlEncoded
    suspend fun listMessageGroup(
        @Field("token") token: String,
        @Field("pageIndex") pageIndex: Int,
        @Field("pageSize") pageSize: Int,
    ): ResponseWrapper<List<MessageGroupResponse>>

    @POST("api/messageGroup/rename")
    @FormUrlEncoded
    suspend fun renameMessageGroup(
        @Field("id") id: String,
        @Field("token") token: String,
        @Field("name") name: String,
    ): ResponseWrapper<Unit>

    @POST("api/messageGroup/sync")
    @FormUrlEncoded
    suspend fun syncMessageGroups(
        @Field("token") token: String,
        @Field("clientMessageGroupsId") ids: List<String>,
        @Field("clientMessageGroupsName") names: List<String>
    ): ResponseWrapper<SyncMessageGroupResponse>

    @POST("api/messageGroup/info")
    @FormUrlEncoded
    suspend fun messageGroupInfo(
        @Field("id") groupId: String,
        @Field("token") token: String,
    ): ResponseWrapper<MessageGroupDetailResponse>

    @POST("api/message/list")
    @FormUrlEncoded
    suspend fun listMessages(
        @Field("messageGroupId") groupId: String?,
        @Field("token") token: String,
        @Field("pageIndex") pageIndex: Int,
        @Field("pageCount") pageCount: Int,
    ): ResponseWrapper<List<MessageExcerptInfoResponse>>

    @POST("api/message/remove")
    @FormUrlEncoded
    suspend fun deleteMessage(
        @Field("id") id: Long,
        @Field("token") token: String,
    ): ResponseWrapper<Unit>

    @POST("api/message/listAll")
    @FormUrlEncoded
    suspend fun listAllMessages(
        @Field("token") token: String,
        @Field("pageIndex") pageIndex: Int,
        @Field("pageCount") pageCount: Int,
    ): ResponseWrapper<List<MessageExcerptInfoResponse>>

    @POST("api/message/detail")
    @FormUrlEncoded
    suspend fun messageDetail(
        @Field("id") id: Long,
        @Field("token") token: String,
    ): ResponseWrapper<MessageDetailResponse>

    @POST("api/user/info")
    @FormUrlEncoded
    suspend fun userInfo(
        @Field("token") token: String,
    ): ResponseWrapper<UserInfoResponse>

    @POST("api/user/clients")
    @FormUrlEncoded
    suspend fun userClients(
        @Field("token") token: String,
    ): ResponseWrapper<List<UserLoginDeviceResponse>>

    @POST("api/user/logoutOthers")
    @FormUrlEncoded
    suspend fun logoutOthers(
        @Field("token") token: String,
    ): ResponseWrapper<Unit>

    @POST("api/user/logout")
    @FormUrlEncoded
    suspend fun logout(
        @Field("token") token: String,
    ): ResponseWrapper<Unit>
}