package moe.peanutmelonseedbigalmond.push.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import coil.imageLoader
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.simple.ext.SimpleExtPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.repository.AppConfigurationRepository
import moe.peanutmelonseedbigalmond.push.ui.component.LocalActivity
import moe.peanutmelonseedbigalmond.push.ui.component.LocalActivityCoroutineScope
import moe.peanutmelonseedbigalmond.push.ui.component.LocalGlobalViewModel
import moe.peanutmelonseedbigalmond.push.ui.component.LocalMarkwon
import moe.peanutmelonseedbigalmond.push.ui.component.MyApp
import moe.peanutmelonseedbigalmond.push.ui.viewmodel.GlobalViewModel
import moe.peanutmelonseedbigalmond.push.utils.NotificationUtil

class MainActivity : AppCompatActivity(), CoroutineScope by CoroutineScope(Dispatchers.Main) {
    private lateinit var viewModel: GlobalViewModel
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, R.string.allow_necessary_premissions, Toast.LENGTH_SHORT).show()
            finish()
        } else {
            NotificationUtil.setupDefaultNotificationChannel()
        }
    }
    private lateinit var markwon: Markwon

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val locale = AppCompatDelegate.getApplicationLocales()
        Log.i("TAG", "onCreate: Locale:$locale")

        viewModel = ViewModelProvider(this)[GlobalViewModel::class.java]

        viewModel.oneTapClient = Identity.getSignInClient(this)
        viewModel.signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.Builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()
        viewModel.auth = FirebaseAuth.getInstance()

        viewModel.loadConfig()

        viewModel.fcmToken = AppConfigurationRepository.fcmPushToken

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) {
                val token = it.result
                viewModel.fcmToken = token
            }
        }

        markwon = Markwon.builder(this)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(this))
            .usePlugin(TaskListPlugin.create(this))
            .usePlugin(HtmlPlugin.create())
            .usePlugin(ImagesPlugin.create())
            .usePlugin(CoilImagesPlugin.create(this, this.imageLoader))
            .usePlugin(LinkifyPlugin.create())
            .usePlugin(SimpleExtPlugin.create())
            .build()

        setContent {
            CompositionLocalProvider(
                LocalGlobalViewModel provides viewModel,
                LocalActivityCoroutineScope provides this,
                LocalMarkwon provides markwon,
                LocalActivity provides this
            ) {
                MyApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        askNotificationPermission()
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
                NotificationUtil.setupDefaultNotificationChannel()
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                AlertDialog.Builder(this)
                    .setMessage(R.string.allow_app_notification_premission)
                    .setPositiveButton(R.string.confirm) { _, _ ->
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    .setNegativeButton(R.string.cancel) { _, _ ->
                        finish()
                    }.create().show()
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            NotificationUtil.setupDefaultNotificationChannel()
        }
    }
}