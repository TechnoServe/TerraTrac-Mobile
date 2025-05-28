package org.technoserve.farmcollector.ui.screens.collectionsites

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ContentValues.TAG
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import org.joda.time.Instant
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.models.CollectionSite
import org.technoserve.farmcollector.database.models.Commodity
import org.technoserve.farmcollector.ui.components.FarmListHeader
import org.technoserve.farmcollector.ui.components.SiteForm
import org.technoserve.farmcollector.utils.isSystemInDarkTheme

import org.technoserve.farmcollector.viewmodels.FarmViewModel
import org.technoserve.farmcollector.viewmodels.FarmViewModelFactory

/**
 * This function is used to add a new collection site.
 *
 * @param navController the navigation controller to navigate to other screens.
 */
@Composable
fun AddSite(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
//            .statusBarsPadding()
//            .padding(WindowInsets.safeDr awing.asPaddingValues()) // Respect safe areas,
            .fillMaxWidth()
    ) {
        FarmListHeader(
            title = stringResource(id = R.string.add_site),
            onSearchQueryChanged = {},
            onBackClicked = { navController.popBackStack() },
            showSearch = false,
            showRestore = false,
            onRestoreClicked = {},
            isBackupEnabled = false,
            showLastSync = false,
            lastSyncTime="",
            onBackupToggleClicked= {}
        )
        Spacer(modifier = Modifier.height(16.dp))
        SiteForm(navController)
    }
}

fun addSite(
    farmViewModel: FarmViewModel,
    name: String,
    agentName: String,
    phoneNumber: String,
    email: String,
    village: String,
    district: String,
    commodity: Commodity
): CollectionSite {
    val site = CollectionSite(
        name,
        agentName,
        phoneNumber,
        email,
        village,
        district,
        createdAt = Instant.now().millis,
        updatedAt = Instant.now().millis,
        commodity = commodity
    )
    farmViewModel.addSite(site) { isAdded ->
        if (isAdded) {
            Log.d(TAG, " site added")
        }
    }
    return site
}