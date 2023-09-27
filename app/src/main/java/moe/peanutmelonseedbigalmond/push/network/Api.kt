package moe.peanutmelonseedbigalmond.push.network

import com.haroldadmin.cnradapter.NetworkResponse
import moe.peanutmelonseedbigalmond.push.network.response.DeviceRegisterResponse
import moe.peanutmelonseedbigalmond.push.network.response.ErrorResponse
import moe.peanutmelonseedbigalmond.push.network.response.FetchDeviceResponse
import moe.peanutmelonseedbigalmond.push.network.response.FetchMessageResponse
import moe.peanutmelonseedbigalmond.push.network.response.FetchTokenResponse
import moe.peanutmelonseedbigalmond.push.network.response.LoginResponse
import moe.peanutmelonseedbigalmond.push.network.response.PushMessageResponse
import moe.peanutmelonseedbigalmond.push.network.response.ResponseWrapper
import moe.peanutmelonseedbigalmond.push.network.response.UserInfoResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface Api {
    /**
     * 监测连通性
     */
    @GET("/ping")
    suspend fun ping(): NetworkResponse<ResponseWrapper<Unit>, ErrorResponse>

    @POST("/user/login")
    @FormUrlEncoded
    suspend fun login(@Field("token") fcmToken: String): NetworkResponse<ResponseWrapper<LoginResponse>, ErrorResponse>

    @POST("/user/info")
    @FormUrlEncoded
    suspend fun getUserInfo(@Field("token") token: String): NetworkResponse<ResponseWrapper<UserInfoResponse>, ErrorResponse>

    @POST("/device/list")
    @FormUrlEncoded
    suspend fun fetchDevices(@Field("token") token: String): NetworkResponse<ResponseWrapper<List<FetchDeviceResponse>>, ErrorResponse>

    @POST("/device/register")
    @FormUrlEncoded
    suspend fun registerDevice(
        @Field("token") token: String,
        @Field("name") deviceName: String,
        @Field("fcmToken") fcmToken: String,
        @Field("deviceType") deviceType: String = "android"
    ): NetworkResponse<ResponseWrapper<DeviceRegisterResponse>, ErrorResponse>

    @POST("/device/remove")
    @FormUrlEncoded
    suspend fun removeDevice(
        @Field("token") token: String,
        @Field("id") id: Long,
    ): NetworkResponse<ResponseWrapper<Unit>, ErrorResponse>

    @POST("/device/rename")
    @FormUrlEncoded
    suspend fun renameDevice(
        @Field("token") token: String,
        @Field("id") id: Long,
        @Field("name") newName: String
    ): NetworkResponse<ResponseWrapper<FetchDeviceResponse>, ErrorResponse>

    @POST("/token/generate")
    @FormUrlEncoded
    suspend fun generateToken(
        @Field("token") token: String,
    ): NetworkResponse<ResponseWrapper<FetchTokenResponse>, ErrorResponse>

    @POST("/token/all")
    @FormUrlEncoded
    suspend fun listToken(
        @Field("token") token: String,
    ): NetworkResponse<ResponseWrapper<List<FetchTokenResponse>>, ErrorResponse>

    @POST("/token/revoke")
    @FormUrlEncoded
    suspend fun revokeToken(
        @Field("token") token: String,
        @Field("id") id: Long,
    ): NetworkResponse<ResponseWrapper<Unit>, ErrorResponse>

    @POST("/token/reGenerate")
    @FormUrlEncoded
    suspend fun reGenerate(
        @Field("token") token: String,
        @Field("id") id: Long,
    ): NetworkResponse<ResponseWrapper<FetchDeviceResponse>, ErrorResponse>

    @POST("/token/rename")
    @FormUrlEncoded
    suspend fun renameToken(
        @Field("token") token: String,
        @Field("id") id: Long,
        @Field("name") newName: String
    ): NetworkResponse<ResponseWrapper<Unit>, ErrorResponse>

    @POST("/message/all")
    @FormUrlEncoded
    suspend fun listMessages(
        @Field("token") token: String,
    ): NetworkResponse<ResponseWrapper<List<FetchMessageResponse>>, ErrorResponse>

    @POST("/message/push")
    @FormUrlEncoded
    suspend fun pushTextMessage(
        @Field("pushToken") token: String,
        @Field("text") text: String
    ): NetworkResponse<ResponseWrapper<PushMessageResponse>, ErrorResponse>

    @POST("/message/remove")
    @FormUrlEncoded
    suspend fun deleteMessage(
        @Field("token") token: String,
        @Field("id") id: Long
    ): NetworkResponse<ResponseWrapper<Unit>, ErrorResponse>
}