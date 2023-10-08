package moe.peanutmelonseedbigalmond.push.network

import com.google.gson.JsonObject
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface Api {
    @POST("/graphql")
    suspend fun graphql(@Body body: RequestBody): JsonObject
}