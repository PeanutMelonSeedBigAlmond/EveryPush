package moe.peanutmelonseedbigalmond.push.ui.component.page.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.network.Client
import moe.peanutmelonseedbigalmond.push.ui.component.LocalGlobalViewModel
import moe.peanutmelonseedbigalmond.push.ui.component.page.LocalHomePageSnackBarHostState
import moe.peanutmelonseedbigalmond.push.ui.component.page.LocalHomePageViewModel
import moe.peanutmelonseedbigalmond.push.ui.component.widget.KeyItem
import moe.peanutmelonseedbigalmond.push.ui.component.widget.MyAlertDialog
import moe.peanutmelonseedbigalmond.push.ui.data.TokenData

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TokenPage() {
    //region 变量
    val globalViewModel= LocalGlobalViewModel.current
    val homePageViewModel = LocalHomePageViewModel.current
    val snackBarHostState = LocalHomePageSnackBarHostState.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var keys by remember(homePageViewModel) { homePageViewModel.tokenList }
    var isRefreshing by remember { mutableStateOf(false) }
    var currentRenamingKey by remember { mutableStateOf<TokenData?>(null) }
    //endregion

    //region 函数
    /**
     * 显示提示信息
     * @param message String
     * @return Job
     */
    fun showMessageWithSnackBar(message: String) = coroutineScope.launch {
        snackBarHostState.showSnackbar(message)
    }

    fun showMessageWithSnackBar(@StringRes id: Int) = showMessageWithSnackBar(context.getString(id))

    /**
     * 列出推送密钥
     */
    suspend fun listKeys(){
        isRefreshing = true
        try {
            val refreshKeyResponse = globalViewModel.client.listToken()
            keys = refreshKeyResponse.map {
                return@map TokenData(it.name, it.generatedAt, it.id, it.token)
            }
        } catch (_: CancellationException) {
        } catch (e: Exception) {
            e.printStackTrace()
            showMessageWithSnackBar(e.toString())
        } finally {
            isRefreshing = false
        }
    }

    /**
     * 生成新密钥
     */
    suspend fun generateKeys() {
        isRefreshing = true
        try {
            val generateTokenResponse = globalViewModel.client.generateKey()
            val key = TokenData(
                generateTokenResponse.name,
                generateTokenResponse.generatedAt,
                generateTokenResponse.id,
                generateTokenResponse.token
            )
        } catch (_: CancellationException) {
        } catch (e: Exception) {
            e.printStackTrace()
            showMessageWithSnackBar(e.toString())
        } finally {
            isRefreshing = false
        }
    }

    // fab 点击回调：添加密钥
    homePageViewModel.keyPageOnFabClick = {
        coroutineScope.launch {
            generateKeys()
            listKeys()
        }
    }

    val refreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                listKeys()
            }
        }
    )
    //endregion

    LaunchedEffect(Unit) {
        listKeys()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(state = refreshState),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier=Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (keys.isEmpty()) {
                item {
                    Text(
                        text = stringResource(id = R.string.tip_tap_add_to_create_key),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(keys.size, key = { it }) {
                    KeyItem(
                        key = keys[it],
                        onResetAction = {
                            coroutineScope.launch {
                                isRefreshing = true
                                try {
                                    globalViewModel.client.reGenerateToken(it.id)
                                    listKeys()
                                } catch (_: CancellationException) {
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    showMessageWithSnackBar(e.toString())
                                } finally {
                                    isRefreshing = false
                                }
                            }
                        },
                        onDeleteAction = {
                            coroutineScope.launch {
                                isRefreshing = true
                                try {
                                    globalViewModel.client.revokeToken(it.id)
                                    listKeys()
                                } catch (_: CancellationException) {
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    showMessageWithSnackBar(it.toString())
                                } finally {
                                    isRefreshing = false
                                }
                            }
                        },
                        onCopyAction = { tokenData ->
                            val cbm =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cbm.setPrimaryClip(ClipData.newPlainText("key", tokenData.key))

                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                                showMessageWithSnackBar(R.string.tip_copied)
                            }
                        },
                        onItemClick = { tokenData -> currentRenamingKey = tokenData }
                    )
                }
            }
        }
        PullRefreshIndicator(
            refreshing = isRefreshing, state = refreshState, modifier = Modifier.align(
                Alignment.TopCenter
            )
        )
    }

    if (currentRenamingKey != null) {
        RenamingTokenDialog(
            tokenData = currentRenamingKey!!,
            onComplete = { key, newName ->
                currentRenamingKey = null
                coroutineScope.launch {
                    isRefreshing = true
                    try {
                        globalViewModel.client.renameToken(key.id, newName)
                        listKeys()
                    } catch (_: CancellationException) {
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showMessageWithSnackBar(e.toString())
                    } finally {
                        isRefreshing = false
                    }
                }
            },
            onCancel = { currentRenamingKey = null }
        )
    }
}

@Composable
private fun RenamingTokenDialog(
    tokenData: TokenData,
    onComplete: (TokenData, String) -> Unit,
    onCancel: () -> Unit
) {
    val context= LocalContext.current
    var newName by remember { mutableStateOf(tokenData.name) }
    var inputError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    MyAlertDialog(
        title = { Text(text = stringResource(id = R.string.rename)) },
        content = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                isError = inputError,
                supportingText = {
                    if (inputError) Text(text = errorMessage)
                })
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (newName.isEmpty()) {
                    inputError = true
                    errorMessage = context.getString(R.string.field_is_required)
                } else {
                    inputError = false
                    onComplete(tokenData, newName)
                }
            }) {
                Text(text = stringResource(id = R.string.confirm))
            }
        },
        onDismiss = onCancel
    )
}
