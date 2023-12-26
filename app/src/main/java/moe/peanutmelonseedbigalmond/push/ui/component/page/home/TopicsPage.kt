package moe.peanutmelonseedbigalmond.push.ui.component.page.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.ui.component.LocalAppNavHostController
import moe.peanutmelonseedbigalmond.push.ui.component.LocalGlobalViewModel
import moe.peanutmelonseedbigalmond.push.ui.component.page.LocalHomePageSnackBarHostState
import moe.peanutmelonseedbigalmond.push.ui.component.page.LocalHomePageViewModel
import moe.peanutmelonseedbigalmond.push.ui.component.page.Page
import moe.peanutmelonseedbigalmond.push.ui.component.widget.LimitedLengthOutlinedTextField
import moe.peanutmelonseedbigalmond.push.ui.component.widget.MyAlertDialog
import moe.peanutmelonseedbigalmond.push.ui.component.widget.TopicItem
import moe.peanutmelonseedbigalmond.push.ui.data.MessageData
import moe.peanutmelonseedbigalmond.push.ui.data.TopicData

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TopicsPage() {
    val homePageViewModel = LocalHomePageViewModel.current
    val globalViewModel = LocalGlobalViewModel.current
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = LocalHomePageSnackBarHostState.current
    val appNavController = LocalAppNavHostController.current
    var topicsList by remember { mutableStateOf(globalViewModel.topicsList) }
    var isRefreshing by remember { mutableStateOf(false) }
    var addTopicDialogShow by remember { mutableStateOf(false) }
    var renamingTopicIdAndName by remember { mutableStateOf<Pair<String, String>?>(null) }
    homePageViewModel.appBarTitle = stringResource(id = R.string.title_topics)

    fun showSnackBar(msg: String) {
        coroutineScope.launch {
            snackBarHostState.showSnackbar(msg)
        }
    }

    fun showSnackBar(th: Throwable) {
        showSnackBar(th.message ?: th.toString())
    }

    suspend fun getTopics() {
        isRefreshing = true
        try {
            val tList = globalViewModel.client.listTopicAndLatestMessage().map {
                val latestMessage = it.latestMessage?.let {
                    return@let MessageData(it.type, it.title, it.text, it.id, it.sendAt)
                }
                return@map TopicData(it.id, it.name, latestMessage)
            }
            topicsList = sortTopicsByLatestMessageSendTimeDesc(tList)
            globalViewModel.topicsList = topicsList
        } catch (_: CancellationException) {
        } catch (e: Exception) {
            showSnackBar(e)
        } finally {
            isRefreshing = false
        }
    }

    suspend fun deleteTopic(id: String) {
        isRefreshing = true
        try {
            globalViewModel.client.deleteTopic(id)
            getTopics()
        } catch (_: CancellationException) {
        } catch (e: Exception) {
            e.printStackTrace()
            showSnackBar(e)
        } finally {
            isRefreshing = false
        }
    }


    suspend fun addTopic(id: String, name: String) {
        isRefreshing = true
        try {
            globalViewModel.client.addTopic(id, name)
            getTopics()
        } catch (_: CancellationException) {
        } catch (e: Exception) {
            e.printStackTrace()
            showSnackBar(e)
        } finally {
            isRefreshing = false
        }
    }

    val refreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { coroutineScope.launch { getTopics() } }
    )

    homePageViewModel.topicPageOnFabClick = {
        addTopicDialogShow = true
    }

    LaunchedEffect(Unit) {
        getTopics()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(refreshState)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(count = topicsList?.size ?: 0, key = { it }) {
                TopicItem(
                    data = topicsList!![it],
                    onClick = {
                        appNavController.navigate(Page.TopicDetail.buildRouteWithArgs(it.id)) {
                            restoreState = true
                        }
                    },
                    onRenameAction = {
                        if (it.id != null && it.name != null) {
                            renamingTopicIdAndName = it.id to it.name
                        }
                    },
                    onDeleteAction = {
                        val id = it.id
                        if (id != null) {
                            coroutineScope.launch {
                                deleteTopic(id)
                            }
                        }
                    }
                )
            }
        }
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = refreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
    if (addTopicDialogShow) {
        AddTopicDialog(
            onCancel = { addTopicDialogShow = false },
            onConfirm = {
                coroutineScope.launch {
                    addTopic(it.first, it.second)
                }
                addTopicDialogShow = false
            })
    }

    if (renamingTopicIdAndName != null) {
        RenameTopicDialog(
            topicIdAndName = renamingTopicIdAndName!!,
            onCancel = { renamingTopicIdAndName = null },
            onConfirm = { renamingTopicIdAndName = null }
        )
    }
}

@Composable
fun AddTopicDialog(onCancel: () -> Unit, onConfirm: (Pair<String, String>) -> Unit) {
    val context = LocalContext.current
    var idContent by remember { mutableStateOf("") }
    var nameContent by remember { mutableStateOf("") }
    var idErrorContent by remember { mutableStateOf<String?>(null) }
    var nameErrorContent by remember { mutableStateOf<String?>(null) }
    MyAlertDialog(
        title = { Text(stringResource(id = R.string.title_create_topic)) },
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LimitedLengthOutlinedTextField(
                    limit = 20,
                    placeholder = { Text(text = stringResource(id = R.string.input_hint_topic_id)) },
                    isError = idErrorContent != null && idErrorContent?.isNotBlank() == true,
                    supportText = {
                        if (idErrorContent != null && idErrorContent?.isNotBlank() == true) {
                            Text(
                                text = idErrorContent!!
                            )
                        } else {
                            Text(text = stringResource(id = R.string.tip_id_cannot_changed))
                        }
                    },
                    value = idContent,
                    onValueChange = { idContent = it },
                    contentLimitation = Regex("[0-9a-zA-Z_\\-.]*")
                )
                LimitedLengthOutlinedTextField(
                    limit = 40,
                    placeholder = { Text(text = stringResource(id = R.string.input_hint_topic_name)) },
                    isError = nameErrorContent != null && nameErrorContent?.isNotBlank() == true,
                    supportText = {
                        if (nameErrorContent != null && nameErrorContent?.isNotBlank() == true)
                            Text(
                                text = nameErrorContent!!
                            )
                    },
                    value = nameContent,
                    onValueChange = { nameContent = it }
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
                if (idContent.isBlank()) {
                    idErrorContent = context.getString(R.string.field_is_required)
                    return@TextButton
                } else {
                    if (idContent.matches(Regex("[0-9a-zA-Z_\\-.]"))) {
                        idErrorContent = context.getString(R.string.tip_malformed_input)
                        return@TextButton
                    } else {
                        idErrorContent = null
                    }
                }

                if (nameContent.isBlank()) {
                    nameErrorContent = context.getString(R.string.field_is_required)
                    return@TextButton
                } else {
                    nameErrorContent = null
                }
                onConfirm(idContent.trim() to nameContent.trim())
            }) {
                Text(text = stringResource(id = R.string.confirm))
            }
        },
        onDismiss = onCancel
    )
}

@Composable
fun RenameTopicDialog(
    topicIdAndName: Pair<String, String>,
    onCancel: () -> Unit,
    onConfirm: (Pair<String, String>) -> Unit
) {
    val context = LocalContext.current
    var nameContent by remember { mutableStateOf(topicIdAndName.second) }
    var nameErrorContent by remember { mutableStateOf<String?>(null) }
    MyAlertDialog(
        title = { Text(text = stringResource(id = R.string.rename)) },
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    placeholder = { Text(text = stringResource(id = R.string.input_hint_topic_id)) },
                    supportingText = {
                        Text(text = stringResource(id = R.string.tip_field_cannot_be_changed))
                    },
                    value = topicIdAndName.first,
                    onValueChange = {},
                    enabled = false
                )
                LimitedLengthOutlinedTextField(
                    limit = 40,
                    placeholder = { Text(text = stringResource(id = R.string.input_hint_topic_name)) },
                    isError = nameErrorContent != null && nameErrorContent?.isNotBlank() == true,
                    supportText = {
                        if (nameErrorContent != null && nameErrorContent?.isNotBlank() == true)
                            Text(
                                text = nameErrorContent!!
                            )
                    },
                    value = nameContent,
                    onValueChange = { nameContent = it }
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
                if (nameContent.isBlank()) {
                    nameErrorContent = context.getString(R.string.field_is_required)
                    return@TextButton
                } else {
                    nameErrorContent = null
                }
                onConfirm(topicIdAndName.first.trim() to nameContent.trim())
            }) {
                Text(text = stringResource(id = R.string.confirm))
            }
        },
        onDismiss = onCancel
    )
}

private fun sortTopicsByLatestMessageSendTimeDesc(data: List<TopicData>): List<TopicData> {
    return data.sortedWith { o1, o2 ->
        val o1SendTime = o1.latestMessage?.sendTime ?: -1L
        val o2SendTime = o2.latestMessage?.sendTime ?: -1L
        return@sortedWith (o1SendTime - o2SendTime).toInt()
    }
}