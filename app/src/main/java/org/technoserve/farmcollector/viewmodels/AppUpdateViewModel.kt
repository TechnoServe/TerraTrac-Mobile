package org.technoserve.farmcollector.viewmodels

import android.app.Activity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.technoserve.farmcollector.R

/**
 * ViewModel for managing app update functionality.
 *
 * This ViewModel provides a state flow that indicates whether an update is available.
 * When an update is available, it triggers the app update flow using the provided activity.
 *
 */
class AppUpdateViewModel : ViewModel() {
    private val _updateAvailable = MutableStateFlow(false)
    val updateAvailable = _updateAvailable.asStateFlow()

    private lateinit var appUpdateManager: AppUpdateManager
    private val updateType = AppUpdateType.IMMEDIATE

    fun initializeAppUpdateCheck(activity: Activity) {
        appUpdateManager = AppUpdateManagerFactory.create(activity)
        checkForUpdate(activity)
    }

    private fun checkForUpdate(activity: Activity) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                _updateAvailable.value = true
                // Force update
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    updateType,
                    activity,
                    APP_UPDATE_REQUEST_CODE
                )
            }
        }
    }

    companion object {
        const val APP_UPDATE_REQUEST_CODE = 123
    }
}

@Composable
fun UpdateAlert(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Update Available") },
            text = { Text("A new version of the app is available. Please update to continue using the app.") },
            confirmButton = {
                Button(onClick = onConfirm) {
                    Text("Update Now")
                }
            },
            dismissButton = null // No dismiss button for forced update
        )
    }
}
