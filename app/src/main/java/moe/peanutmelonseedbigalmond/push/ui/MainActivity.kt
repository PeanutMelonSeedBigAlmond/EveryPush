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
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import moe.peanutmelonseedbigalmond.push.BaseApp
import moe.peanutmelonseedbigalmond.push.R
import moe.peanutmelonseedbigalmond.push.repository.AppConfigurationRepository
import moe.peanutmelonseedbigalmond.push.ui.component.LocalActivity
import moe.peanutmelonseedbigalmond.push.ui.component.LocalActivityCoroutineScope
import moe.peanutmelonseedbigalmond.push.ui.component.LocalGlobalViewModel
import moe.peanutmelonseedbigalmond.push.ui.component.MyApp
import moe.peanutmelonseedbigalmond.push.ui.viewmodel.GlobalViewModel
import moe.peanutmelonseedbigalmond.push.utils.notification.NotificationUtil

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

        setContent {
            CompositionLocalProvider(
                LocalGlobalViewModel provides viewModel,
                LocalActivityCoroutineScope provides this,
                LocalActivity provides this
            ) {
                MyApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        askNotificationPermission()
        BaseApp.summaryNotifications.keys.forEach {
            NotificationUtil.cancelNotificationSummary(it)
        }
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