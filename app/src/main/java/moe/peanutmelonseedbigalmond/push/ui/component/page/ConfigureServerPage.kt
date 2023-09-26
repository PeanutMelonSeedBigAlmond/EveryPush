package moe.peanutmelonseedbigalmond.push.ui.component.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.network.Client
import moe.peanutmelonseedbigalmond.push.ui.component.LocalGlobalViewModel
import moe.peanutmelonseedbigalmond.push.ui.component.LocalNavHostController
import moe.peanutmelonseedbigalmond.push.ui.component.widget.LoadingDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigureServerPage() {
    val snackBarHostState = remember { SnackbarHostState() }
    val globalViewModel = LocalGlobalViewModel.current
    val navHostController = LocalNavHostController.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var serverEndpoint by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var loadingDialogShow by remember { mutableStateOf(false) }

    fun showMessageWithSnackBar(message: String) = coroutineScope.launch {
        snackBarHostState.showSnackbar(message)
    }

    fun onConfigureServerSuccess() {
        val endpointUrl = serverEndpoint.trim()
        globalViewModel.url = endpointUrl
        navHostController.navigate(Page.GoogleLogin.route) {
            restoreState = true
        }
    }

    fun tryConnectToServer() {
        if (serverEndpoint.isBlank()) {
            errorMessage = context.getString(R.string.field_is_required)
            return
        } else if (!serverEndpoint.startsWith("http://") && !serverEndpoint.startsWith("https://")) {
            errorMessage = context.getString(R.string.url_must_starts_with_http_or_https)
            return
        }

        errorMessage = null
        coroutineScope.launch {
            loadingDialogShow = true
            val endpointUrl = serverEndpoint.trim()
            try {
                val client = Client(endpointUrl)
                withContext(Dispatchers.IO) { client.ping() }
                onConfigureServerSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                showMessageWithSnackBar(e.toString())
            } finally {
                loadingDialogShow = false
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(text = stringResource(id = R.string.configuring_server_title)) }) },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .padding(start = 8.dp, end = 8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (loadingDialogShow) {
                LoadingDialog()
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = serverEndpoint,
                onValueChange = { value -> serverEndpoint = value },
                isError = !errorMessage.isNullOrEmpty(),
                supportingText = { if (errorMessage != null) Text(text = errorMessage!!) },
                label = { Text(text = "Endpoint") },
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = ::tryConnectToServer) {
                    Text(text = stringResource(id = R.string.test_server))
                }
            }
        }
    }
}
