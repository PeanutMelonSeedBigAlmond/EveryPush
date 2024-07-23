package moe.peanutmelonseedbigalmond.push.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import moe.peanutmelonseedbigalmond.push.ui.component.MessageBody
import moe.peanutmelonseedbigalmond.push.ui.component.MessageDetailHeader
import moe.peanutmelonseedbigalmond.push.viewmodel.MessageShowPageViewModel
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun MessageShowPage(
    messageId: Long,
    parentNavHostController: NavHostController,
    modifier: Modifier = Modifier
) {
    val viewModel = viewModel(modelClass = MessageShowPageViewModel::class.java)
    val messageDetail by viewModel.message.observeAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    var refreshing by remember { viewModel.refreshing }
    val coroutineScope = rememberCoroutineScope()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            coroutineScope.launch {
                try {
                    refreshing = true
                    viewModel.refreshMessageDetail(messageId)
                } catch (_: CancellationException) {
                } catch (e: Exception) {
                    e.printStackTrace()
                    launch {
                        snackBarHostState.showSnackbar(e.localizedMessage ?: e.toString())
                    }
                } finally {
                    refreshing = false
                }
            }
        })

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { }, navigationIcon = {
                IconButton(onClick = { parentNavHostController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            })
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        },
    ) {
        Box(modifier = Modifier.padding(it)) {
            LazyColumn(
                contentPadding = PaddingValues(8.dp),
                modifier = modifier
                    .fillMaxWidth()
                    .pullRefresh(pullRefreshState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (messageDetail != null) {
                    item {
                        MessageDetailHeader(
                            messageDetail = messageDetail!!,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }

                    item {
                        MessageBody(
                            messageDetail = messageDetail!!,
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = refreshing, state = pullRefreshState, modifier = Modifier.align(
                    Alignment.TopCenter
                )
            )
        }
    }

    LaunchedEffect(key1 = Unit) {
        try {
            refreshing = true
            viewModel.refreshMessageDetail(messageId)
        } catch (_: CancellationException) {
        } catch (e: Exception) {
            e.printStackTrace()
            launch {
                snackBarHostState.showSnackbar(e.localizedMessage ?: e.toString())
            }
        } finally {
            refreshing = false
        }
    }
}