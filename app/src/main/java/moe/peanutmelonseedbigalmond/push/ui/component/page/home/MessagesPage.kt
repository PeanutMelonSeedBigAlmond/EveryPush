package moe.peanutmelonseedbigalmond.push.ui.component.page.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.ui.component.LocalGlobalViewModel
import moe.peanutmelonseedbigalmond.push.ui.component.page.LocalHomePageSnackBarHostState
import moe.peanutmelonseedbigalmond.push.ui.component.page.LocalHomePageViewModel
import moe.peanutmelonseedbigalmond.push.ui.component.widget.MessageItem
import moe.peanutmelonseedbigalmond.push.ui.data.MessageData

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MessagesPage() {
    //region 变量
    val globalViewModel = LocalGlobalViewModel.current
    val homePageViewModel = LocalHomePageViewModel.current
    val snackBarHostState = LocalHomePageSnackBarHostState.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tokenList by remember(homePageViewModel) { homePageViewModel.tokenList }
    var messages by remember(homePageViewModel) { homePageViewModel.messageList }
    var isRefreshing by remember { mutableStateOf(false) }
    var pushMessageDialogShow by remember { mutableStateOf(false) }
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
     * 获取所有消息
     * @return Job
     */
    suspend fun fetchMessages() {
        isRefreshing = true
        try {
            messages = globalViewModel.client.listMessages().map {
                return@map MessageData(it.type, it.title ?: "", it.text, it.id, it.pushTime)
            }.sortedBy { it.sendTime }.reversed()
        } catch (_: CancellationException) {
        } catch (e: Exception) {
            e.printStackTrace()
            showMessageWithSnackBar(e.toString())
        } finally {
            isRefreshing = false
        }
    }

    val refreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                fetchMessages()
            }
        }
    )

    /**
     * 推送消息
     * @param token String
     * @param message String
     */
    suspend fun pushMessage(token: String, message: String) {
        isRefreshing = true
        try {
            val pushMessageResponse = globalViewModel.client.pushTextMessage(
                token,
                message
            )
        } catch (_: CancellationException) {
        } catch (e: Exception) {
            e.printStackTrace()
            showMessageWithSnackBar(e.toString())
        } finally {
            isRefreshing = false
        }
    }

    suspend fun deleteMessage(messageId: Long) {
        isRefreshing = true
        try {
            globalViewModel.client.deleteMessage(messageId)
        } catch (_: CancellationException) {
        } catch (e: Exception) {
            e.printStackTrace()
            showMessageWithSnackBar(e.toString())
        } finally {
            isRefreshing = false
        }
    }

    // fab 回调：
    homePageViewModel.messagePageOnFabClick = {
        if (tokenList.isEmpty()) {
            showMessageWithSnackBar(R.string.create_or_get_keys_first)
        } else {
            pushMessageDialogShow = true
        }
    }
    //endregion

    LaunchedEffect(Unit) {
        fetchMessages()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(state = refreshState)
    ) {
        homePageViewModel.appBarTitle = stringResource(id = R.string.title_messages)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Text(
                        text = stringResource(id = R.string.tip_no_message),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(count = messages.size, key = { it }) {
                    MessageItem(messageData = messages[it]) {
                        coroutineScope.launch {
                            deleteMessage(it.id)
                            fetchMessages()
                        }
                    }
                }
            }
        }
        PullRefreshIndicator(
            refreshing = isRefreshing, state = refreshState, modifier = Modifier.align(
                Alignment.TopCenter
            )
        )
    }

    if (pushMessageDialogShow) {
        PushMessageDialog(
            onDismiss = { pushMessageDialogShow = false },
            onConfirm = { message ->
                pushMessageDialogShow = false
                val pushToken = tokenList.first().key
                coroutineScope.launch {
                    pushMessage(pushToken, message)
                    fetchMessages()
                }
            }
        )
    }
}

@Composable
fun PushMessageDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val context = LocalContext.current
    var messageContent by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun onConfirmButtonClicked() {
        if (messageContent.isNotBlank()) {
            errorMessage = null
            onConfirm(messageContent.trim())
        } else {
            errorMessage = context.getString(R.string.field_is_required)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss::invoke,
        confirmButton = {
            TextButton(onClick = ::onConfirmButtonClicked) {
                Text(text = stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss::invoke) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        text = {
            Column {
                Text(text = stringResource(id = R.string.message_will_be_sent_as_text))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = messageContent,
                    onValueChange = { messageContent = it },
                    label = { Text(text = stringResource(id = R.string.message_content)) },
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(text = errorMessage!!)
                        }
                    }
                )
            }
        }
    )
}