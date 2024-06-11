package org.technoserve.farmcollector.ui.screens

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ScreenWithSidebar(
    navController: NavController, content: @Composable () -> Unit
) {
    Scaffold(bottomBar = { BottomSidebar(navController) }) {
        content()
    }
}
