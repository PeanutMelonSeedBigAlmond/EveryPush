package moe.peanutmelonseedbigalmond.push.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import moe.peanutmelonseedbigalmond.push.ui.component.LoadingDialog
import moe.peanutmelonseedbigalmond.push.ui.provider.LocalSnackBarHostState
import moe.peanutmelonseedbigalmond.push.viewmodel.ChooseServerPageViewModel

@Composable
fun ChooseServerPage(
    modifier: Modifier = Modifier,
    onServerSetup: (String) -> Unit = {}
) {
    val viewModel = viewModel(modelClass = ChooseServerPageViewModel::class.java)
    var serverAddress by remember { viewModel.serverAddress }
    val serverAddressValid by remember {
        derivedStateOf {
            serverAddress.startsWith("http://") || serverAddress.startsWith("https://")
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = LocalSnackBarHostState.current
    var loading by remember { viewModel.loading }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = serverAddress,
            onValueChange = { serverAddress = it.trim() },
            label = {
                Text(text = "服务器地址")
            },
            isError = !serverAddressValid,
            supportingText = {
                if (!serverAddressValid) {
                    Text(text = "请输入正确的服务器地址")
                }
            }
        )
        Divider()
        Button(onClick = {
            if (!serverAddressValid) return@Button
            coroutineScope.launch {
                loading = true
                try {
                    viewModel.updateServerBaseUrl(serverAddress)
                    viewModel.pingServer()
                    loading = false
                    launch {
                        snackBarHostState.showSnackbar("连接成功")
                    }
                    onServerSetup(serverAddress)
                } catch (_: CancellationException) {
                    loading = false
                } catch (e: Exception) {
                    e.printStackTrace()
                    launch {
                        snackBarHostState.showSnackbar(e.message ?: "未知错误")
                    }
                    loading = false
                }
            }
        }, enabled = serverAddressValid) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                Text(text = "下一项")
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null
                )
            }
        }
    }
    LoadingDialog(show = loading)
}