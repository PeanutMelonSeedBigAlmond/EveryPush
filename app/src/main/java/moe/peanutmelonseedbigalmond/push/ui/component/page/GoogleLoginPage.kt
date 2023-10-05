package moe.peanutmelonseedbigalmond.push.ui.component.page

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthCredential
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.launch
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.ui.component.LocalActivity
import moe.peanutmelonseedbigalmond.push.ui.component.LocalGlobalViewModel
import moe.peanutmelonseedbigalmond.push.ui.component.LocalNavHostController
import moe.peanutmelonseedbigalmond.push.ui.component.widget.LoadingDialog

@ExperimentalMaterial3Api
@Composable
@Preview
fun GoogleLoginPage() {
    //region 变量
    val globalViewModel = LocalGlobalViewModel.current
    val navController = LocalNavHostController.current
    val context = LocalContext.current
    val activity = LocalActivity.current
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    var loadingDialogShow by remember { mutableStateOf(false) }
    //endregion

    //region 函数
    /**
     * 显示信息提示
     * @param message String
     * @return Job
     */
    fun showMessageWithSnackBar(message: String) = coroutineScope.launch {
        snackBarHostState.showSnackbar(message)
    }

    fun showMessageWithSnackBar(@StringRes id: Int) = showMessageWithSnackBar(context.getString(id))

    /**
     * 获取 firebase 用户 token 成功回调
     * @param token String
     */
    fun onFirebaseLoginSuccess(token: String) {
        coroutineScope.launch {
            loadingDialogShow = true
            try {
                val loginResponse = globalViewModel.client.login(token)
                globalViewModel.token = loginResponse.token
                globalViewModel.tokenExpiredAt = loginResponse.expiredAt
                globalViewModel.saveUserConfig()
                navController.navigate(Page.Main.route) {
                    popUpTo(Page.Guide.route) {
                        inclusive = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showMessageWithSnackBar(e.toString())
            } finally {
                loadingDialogShow = false
            }
        }
    }

    fun commonWaitForAuthResult(auth: Task<AuthResult>) {
        auth.addOnCompleteListener {
            if (it.isSuccessful) {
                val currentUser = globalViewModel.auth.currentUser
                if (currentUser != null) {
                    currentUser.getIdToken(true)
                        .addOnCompleteListener { task ->
                            loadingDialogShow = false
                            if (task.isSuccessful) {
                                val idToken = task.result.token!!
                                onFirebaseLoginSuccess(idToken)
                            } else {
                                val exception = it.exception
                                exception?.printStackTrace()
                                showMessageWithSnackBar(R.string.error_get_token_failed)
                            }
                        }
                } else {
                    val exception = it.exception
                    exception?.printStackTrace()
                    showMessageWithSnackBar(R.string.error_not_login)
                    loadingDialogShow = false
                }
            } else {
                loadingDialogShow = false
                val exception = it.exception
                exception?.printStackTrace()
                showMessageWithSnackBar(R.string.error_login_cancelled)
            }
        }
    }

    //region Google 登录
    /**
     * 获取到 Google 登录 token 之后，换取 firebase 用户 token
     * @param googleLoginToken String
     */
    fun getFirebaseUserToken(googleLoginToken: String) {
        val firebaseCredential = GoogleAuthProvider.getCredential(googleLoginToken, null)
        commonWaitForAuthResult(globalViewModel.auth.signInWithCredential(firebaseCredential))
    }

    /**
     * Google 登录的结果回调，在此处获取登录 token
     * @param result ActivityResult
     */
    fun onGoogleLoginResult(result: ActivityResult) {
        loadingDialogShow = true
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            try {
                val credential =
                    globalViewModel.oneTapClient.getSignInCredentialFromIntent(result.data)
                val token = credential.googleIdToken
                loadingDialogShow = false
                if (token != null) {
                    getFirebaseUserToken(token)
                } else {
                    showMessageWithSnackBar(R.string.error_get_token_failed)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showMessageWithSnackBar(e.toString())
            }
        } else if (result.resultCode == AppCompatActivity.RESULT_CANCELED) {
            showMessageWithSnackBar(R.string.error_login_cancelled)
            loadingDialogShow = false
        } else {
            showMessageWithSnackBar(R.string.error_unknown_error_occurred)
            loadingDialogShow = false
        }
    }

    val googleLoginActivityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = ::onGoogleLoginResult
    )

    /**
     * 调用 Google 登录接口
     */
    fun loginWithGoogle() {
        loadingDialogShow = true
        globalViewModel.oneTapClient.beginSignIn(globalViewModel.signInRequest)
            .addOnSuccessListener {
                val intentSendRequest =
                    IntentSenderRequest.Builder(it.pendingIntent.intentSender).build()
                googleLoginActivityLauncher.launch(intentSendRequest)
            }
            .addOnFailureListener {
                it.printStackTrace()
                loadingDialogShow = false
                showMessageWithSnackBar(it.toString())
            }
    }
    //endregion

    //region 第三方登录
    /**
     * 第三方登录公用回调
     * @param credential OAuthCredential
     */
    fun onThirdPartyLoginSuccess(credential: OAuthCredential) {
        commonWaitForAuthResult(globalViewModel.auth.signInWithCredential(credential))
    }

    /**
     * Microsoft 账号登录
     */
    fun loginWithMicrosoft() {
        loadingDialogShow = true
        val provider = OAuthProvider.newBuilder("microsoft.com")
        val pendingResultTask = globalViewModel.auth.pendingAuthResult
        if (pendingResultTask == null) { // 没有待处理的登录请求
            globalViewModel.auth.startActivityForSignInWithProvider(activity, provider.build())
                .addOnSuccessListener {
                    val credential = it.credential as OAuthCredential?
                    if (credential == null) {
                        showMessageWithSnackBar(R.string.error_not_login)
                        loadingDialogShow = false
                    } else {
                        commonWaitForAuthResult(globalViewModel.auth.signInWithCredential(credential))
                    }
                }.addOnFailureListener {
                    it.printStackTrace()
                    loadingDialogShow = false
                    showMessageWithSnackBar(it.toString())
                }
        } else {
            pendingResultTask.addOnSuccessListener {
                val credential = it.credential as OAuthCredential?
                if (credential == null) {
                    showMessageWithSnackBar(R.string.error_not_login)
                    loadingDialogShow = false
                } else {
                    commonWaitForAuthResult(globalViewModel.auth.signInWithCredential(credential))
                }
            }.addOnFailureListener {
                it.printStackTrace()
                showMessageWithSnackBar(it.toString())
                loadingDialogShow = false
            }
        }
    }
    //endregion
    //endregion

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.login_title)) }) },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) {
        if (loadingDialogShow) {
            LoadingDialog()
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = ::loginWithGoogle) {
                Text(text = stringResource(id = R.string.login_with_google))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = ::loginWithMicrosoft) {
                Text(text = stringResource(id = R.string.login_with_microsoft))
            }
            Text(
                text = stringResource(id = R.string.login_account_tips),
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

