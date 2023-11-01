package moe.peanutmelonseedbigalmond.push.ui.component.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import moe.peanutmelonseedbigalmond.push.App
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.ui.component.LocalAppNavHostController
import moe.peanutmelonseedbigalmond.push.ui.component.LocalGlobalViewModel
import moe.peanutmelonseedbigalmond.push.ui.component.widget.ImageWidget
import moe.peanutmelonseedbigalmond.push.ui.component.widget.view.SelectableAndClickableTextView
import moe.peanutmelonseedbigalmond.push.ui.data.MessageData

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MessageDetailPage(messageId: Long, messageBody: MessageData?) {
    check(messageBody != null || messageId > 0L) { "Invalid args: messageId=$messageId,messageBody=$messageBody" }

    var body by remember { mutableStateOf(messageBody) }
    val navController = LocalAppNavHostController.current
    val globalViewModel = LocalGlobalViewModel.current
    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    var isRefreshing by remember { mutableStateOf(false) }

    fun showMessage(msg: String) {
        coroutineScope.launch {
            snackBarHostState.showSnackbar(msg)
        }
    }

    fun showMessage(th: Throwable) {
        showMessage(th.localizedMessage ?: th.toString())
    }

    suspend fun getMessageDetail(id: Long) {
        isRefreshing = true
        try {
            body = globalViewModel.client.queryMessage(id).let {
                return@let MessageData(it.type, it.title, it.text, it.id, it.sendAt)
            }
        } catch (_: CancellationException) {
        } catch (e: Exception) {
            e.printStackTrace()
            showMessage(e)
        } finally {
            isRefreshing = false
        }
    }

    val refreshState = rememberPullRefreshState(refreshing = isRefreshing, onRefresh = {
        coroutineScope.launch {
            getMessageDetail(messageId)
        }
    })

    LaunchedEffect(Unit) {
        if (body == null) {
            getMessageDetail(messageId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (body?.title?.isNotEmpty() == true) {
                            body?.title!!
                        } else {
                            stringResource(id = R.string.title_message_detail)
                        },
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "")
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .padding(8.dp)
                .fillMaxSize()
                .pullRefresh(refreshState)
        ) {
            LazyColumn {
                if (body != null) {
                    item {
                        DetailBody(messageData = body!!)
                    }
                }
            }
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = refreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}


@Composable
private fun DetailBody(
    messageData: MessageData,
) {
    if (messageData.type == MessageData.Type.IMAGE) {
        ImageWidget(
            imageUrl = messageData.content, modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(8.dp)
                )
        )
    } else if (messageData.type == MessageData.Type.MARKDOWN) {
        AndroidView(factory = {
            SelectableAndClickableTextView(it).also {
                it.setTextIsSelectable(true)
            }
        }, modifier = Modifier.fillMaxWidth()) {
            App.markwon.setMarkdown(it, messageData.content)
        }
    } else { // text and else
        SelectionContainer {
            Text(text = messageData.content, modifier = Modifier.fillMaxWidth())
        }
    }
}
