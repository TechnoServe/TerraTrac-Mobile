package org.technoserve.farmcollector.ui.screens.settings

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

/**
 *  This function is used to add bottom navigation to a screen that we want to have a bottom navigation
 */

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ScreenWithSidebar(
    navController: NavController, content: @Composable () -> Unit
) {
    Scaffold(bottomBar = { BottomSidebar(navController) }) {
        content()
    }
}
