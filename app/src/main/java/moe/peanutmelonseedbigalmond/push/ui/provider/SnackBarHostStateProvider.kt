package moe.peanutmelonseedbigalmond.push.ui.provider

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf

val LocalSnackBarHostState= compositionLocalOf<SnackbarHostState> { error("Not initialized") }