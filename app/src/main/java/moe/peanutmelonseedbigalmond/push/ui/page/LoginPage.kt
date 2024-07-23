package moe.peanutmelonseedbigalmond.push.ui.page

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import moe.peanutmelonseedbigalmond.push.network.Client
import moe.peanutmelonseedbigalmond.push.ui.MainActivity
import moe.peanutmelonseedbigalmond.push.ui.component.LoadingDialog
import moe.peanutmelonseedbigalmond.push.ui.provider.LocalSnackBarHostState
import moe.peanutmelonseedbigalmond.push.viewmodel.LoginPageViewModel

@Composable
fun LoginPage(
    serverAddress: String,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onLoginSuccess: (String) -> Unit = {}
) {
    val activity = LocalContext.current as MainActivity
    val snackBarHostState = LocalSnackBarHostState.current
    val coroutineScope = rememberCoroutineScope()
    val viewModel = viewModel(modelClass = LoginPageViewModel::class.java)
    var loading by remember { viewModel.loading }

    LaunchedEffect(key1 = Unit) {
        Client.serverAddress=serverAddress
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "服务器地址: $serverAddress")
        Button(onClick = {
            loading = true
            coroutineScope.launch {
                try {
                    val token = viewModel.loginWithGoogle(activity)
                    if (token == null) {
                        loading = false
                        launch { snackBarHostState.showSnackbar(message = "Login failed") }
                        Log.i("Login", "token is null")
                    } else {
                        val loginToken = viewModel.login(token)
                        Log.i("Login", "loginToken: $loginToken")
                        loading = false
                        onLoginSuccess(loginToken)
                    }
                } catch (_: CancellationException) {
                } catch (e: Exception) {
                    val message = e.localizedMessage ?: e.message ?: e.toString()
                    launch { snackBarHostState.showSnackbar(message = message) }
                    Log.e("Login", e.message.toString())
                    e.printStackTrace()
                    loading = false
                }
            }
        }) {
            Text(text = "Login with Google")
        }
        Button(onClick = {
            coroutineScope.launch {
                try {
                    loading = true
                    val token = viewModel.loginWithMicrosoft(activity)
                    if (token == null) {
                        loading = false
                        launch { snackBarHostState.showSnackbar(message = "Login failed") }
                        Log.i("Login", "token is null")
                    } else {
                        val loginToken = viewModel.login(token)
                        Log.i("Login", "loginToken: $loginToken")
                        loading = false
                        onLoginSuccess(loginToken)
                    }
                } catch (_: CancellationException) {
                } catch (e: Exception) {
                    e.printStackTrace()
                    loading = false
                    val message = e.localizedMessage ?: e.message ?: e.toString()
                    launch { snackBarHostState.showSnackbar(message = message) }
                }
            }
        }) {
            Text(text = "Login with Microsoft")
        }

        TextButton(onClick = onBack) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Text(text = "上一页")
            }
        }
    }

    LoadingDialog(show = loading)
}

