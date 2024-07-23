package moe.peanutmelonseedbigalmond.push.viewmodel

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import moe.peanutmelonseedbigalmond.push.BaseApp
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.network.Client

class LoginPageViewModel : ViewModel() {
    private val auth = Firebase.auth
    private val credentialManager = CredentialManager.create(BaseApp.context)

    val loading = mutableStateOf(false)

    suspend fun loginWithGoogle(activity: Activity): String? {
        val googleSignInOption = GetGoogleIdOption.Builder()
            .setServerClientId(BaseApp.context.getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleSignInOption)
            .build()
        val result = credentialManager.getCredential(activity, request)
        val credential = result.credential

        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val googleIdToken = googleIdTokenCredential.idToken
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            return auth.signInWithCredential(firebaseCredential).await()
                .user!!
                .getIdToken(true)
                .await()
                .token
        } else {
            Log.i("loginWithGoogle", "credential is not GoogleIdTokenCredential")
            return null
        }
    }


    suspend fun loginWithMicrosoft(activity: Activity): String? = withContext(Dispatchers.IO) {
        val provider = OAuthProvider.newBuilder("microsoft.com").build()
        val pendingResultTask = auth.pendingAuthResult
        if (pendingResultTask != null) { // 已经存在登录请求
            return@withContext pendingResultTask.await()
                .user!!
                .getIdToken(true)
                .await()
                .token
        } else {
            auth.startActivityForSignInWithProvider(activity, provider)
                .await()
                .user!!
                .getIdToken(true)
                .await()
                .token
        }
    }

    suspend fun login(idToken: String): String {
        val response = Client.login(idToken)
        val token = response.token
        return token
    }
}