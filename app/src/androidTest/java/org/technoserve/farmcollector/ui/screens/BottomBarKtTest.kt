package org.technoserve.farmcollector.ui.screens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import org.junit.Rule
import org.junit.Test
import io.mockk.mockk
import io.mockk.verify
import org.technoserve.farmcollector.ui.screens.settings.BottomSidebar


class BottomBarKtTest{

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bottomSidebar_clickSettingsIcon_navigatesToSettings() {
        // Mock NavController
        val mockNavController = mockk<NavController>(relaxed = true)

        composeTestRule.setContent {
            BottomSidebar(navController = mockNavController)
        }

        // Find the settings icon by its content description and click it
        composeTestRule.onNodeWithContentDescription("Settings").performClick()

        // Verify that the NavController navigates to "settings"
        verify { mockNavController.navigate("settings") }
    }
}