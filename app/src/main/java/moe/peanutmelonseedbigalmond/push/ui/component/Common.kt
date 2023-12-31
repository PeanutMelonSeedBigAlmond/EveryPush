package moe.peanutmelonseedbigalmond.push.ui.component

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import moe.peanutmelonseedbigalmond.push.ui.viewmodel.GlobalViewModel

val LocalGlobalViewModel = compositionLocalOf<GlobalViewModel> { error("Not initialized") }
val LocalAppNavHostController = compositionLocalOf<NavHostController> { error("Not initialized") }
val LocalActivityCoroutineScope = compositionLocalOf<CoroutineScope> { error("Not initialized") }
val LocalActivity = compositionLocalOf<AppCompatActivity> { error("Not initialized") }