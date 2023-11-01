package moe.peanutmelonseedbigalmond.push.ui.component.page

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.core.os.bundleOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.ui.component.LocalAppNavHostController
import moe.peanutmelonseedbigalmond.push.ui.component.LocalGlobalViewModel
import moe.peanutmelonseedbigalmond.push.ui.component.extension.navigate
import moe.peanutmelonseedbigalmond.push.ui.component.widget.MessageItem
import moe.peanutmelonseedbigalmond.push.ui.component.widget.MyAlertDialog
import moe.peanutmelonseedbigalmond.push.ui.data.MessageData

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun TopicDetailPage(topicId: String?) {
    val navController = LocalAppNavHostController.current
    val globalViewModel = LocalGlobalViewModel.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackBarState = remember { SnackbarHostState() }
    var sendMessageDialogShow by remember { mutableStateOf(false) }
    var subTitle by remember { mutableStateOf<String?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf(listOf<MessageData>()) }
    val pushTokenList by remember(globalViewModel) { globalViewModel.tokenList }

    fun showSnackBar(msg: String) {
        coroutineScope.launch {
            snackBarState.showSnackbar(msg)
        }
    }

    fun showSnackBar(@StringRes id: Int) {
        showSnackBar(context.getString(id))
    }

    fun showSnackBar(e: Exception) {
        showSnackBar(e.message ?: e.toString())
    }

    suspend fun getTopicDetail() {
        isRefreshing = true
        try {
            val topicDetail = globalViewModel.client.topicDetail(topicId)
            subTitle = topicDetail.name ?: context.getString(R.string.title_default_notification)
            messages = topicDetail.messages.map {
                return@map MessageData(it.type, it.title, it.text, it.id, it.sendAt)
            }
        } catch (_: CancellationException) {
        } catch (e: Exception) {
            e.printStackTrace()
            showSnackBar(e)
        } finally {
            isRefreshing = false
        }
    }

    suspend fun deleteMessage(id: Long) {
        isRefreshing = true
        try {
            globalViewModel.client.deleteMessage(id)
            getTopicDetail()
        } catch (_: CancellationException) {
        } catch (e: Exception) {
            e.printStackTrace()
            showSnackBar(e)
        } finally {
            isRefreshing = false
        }
    }

    suspend fun sendMessage(text: String) {
        isRefreshing = true
        try {
            val pushToken = pushTokenList.firstOrNull()
            globalViewModel.client.pushMessage(pushToken!!.key, text, topicId = topicId)
            getTopicDetail()
        } catch (_: CancellationException) {
        } catch (e: Exception) {
            e.printStackTrace()
            showSnackBar(e)
        } finally {
            isRefreshing = false
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch { getTopicDetail() }
        }
    )

    LaunchedEffect(key1 = topicId) {
        getTopicDetail()
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackBarState) },
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = stringResource(id = R.string.title_topic_detail))
                        if (subTitle?.isNotBlank() == true) {
                            Text(
                                text = subTitle!!,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val pushToken = pushTokenList.firstOrNull()
                        if (pushToken == null) {
                            showSnackBar(R.string.create_or_get_keys_first)
                        } else {
                            sendMessageDialogShow = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Send, contentDescription = stringResource(
                                id = R.string.action_send_message
                            )
                        )
                    }
                }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .pullRefresh(state = pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
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
                    items(messages.size, key = { messages[it].id }) {
                        MessageItem(messageData = messages[it], onDeleteAction = {
                            coroutineScope.launch {
                                deleteMessage(it.id)
                            }
                        }) {
                            navController.navigate(
                                route = Page.MessageDetail.route,
                                args = bundleOf(Page.MessageDetail.Args.MessageBody to it)
                            )
                        }
                    }
                }
            }
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
    if (sendMessageDialogShow) {
        SendMessageDialog(onCancel = {
            sendMessageDialogShow = false
        }, onConfirm = {
            sendMessageDialogShow = false
            coroutineScope.launch {
                sendMessage(it)
            }
        })
    }
}

@Composable
private fun SendMessageDialog(onCancel: () -> Unit, onConfirm: (String) -> Unit) {
    val context = LocalContext.current
    var textFieldContent by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    MyAlertDialog(
        title = { Text(text = stringResource(id = R.string.action_send_message)) },
        onDismiss = onCancel,
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = stringResource(id = R.string.message_will_be_sent_as_text))
                OutlinedTextField(
                    value = textFieldContent,
                    onValueChange = { textFieldContent = it },
                    isError = errorMessage != null,
                    supportingText = { if (errorMessage != null) Text(text = errorMessage!!) }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (textFieldContent.isBlank()) {
                    errorMessage = context.getString(R.string.field_is_required)
                } else {
                    onConfirm(textFieldContent)
                }
            }) {
                Text(text = stringResource(id = R.string.confirm))
            }
        }
    )
}