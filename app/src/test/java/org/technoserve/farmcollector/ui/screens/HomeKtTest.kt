package org.technoserve.farmcollector.ui.screens

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.technoserve.farmcollector.ui.theme.FarmCollectorTheme
import org.technoserve.farmcollector.utils.Language
import org.technoserve.farmcollector.utils.LanguageViewModel
import android.app.Application



class TestApplication : Application() {
    // You can add test-specific setup here if needed
}

@RunWith(AndroidJUnit4::class)
class HomeKtTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private val testApplication = TestApplication()



    @Test
    fun testHomeScreen() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            FarmCollectorTheme {
                Home(
                    navController = navController,
                    languageViewModel = LanguageViewModel(testApplication), // Provide mock or real ViewModel instance
                    languages = listOf(Language("en", "English"), Language("es", "Spanish")) // Provide sample data
                )
            }
        }

        // Verify the main components are visible on the Home screen
        composeTestRule.onNodeWithText("App Name").assertIsDisplayed() // Replace with your app's name string
        composeTestRule.onNodeWithText("Get Started").assertIsDisplayed() // Replace with string resource if needed
        composeTestRule.onNodeWithText("Developed by").assertIsDisplayed() // Replace with string resource if needed
    }

    @Test
    fun testHomeScreenWithNavigation() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            FarmCollectorTheme {
                Home(
                    navController = navController,
                    languageViewModel = LanguageViewModel(testApplication),
                    languages = listOf(Language("en", "English"), Language("es", "Spanish"))
                )
            }
        }

        // Simulate a navigation action by clicking "Get Started"
        composeTestRule.onNodeWithText("Get Started").performClick() // Replace with string resource if needed
        assertEquals("siteList", navController.currentDestination?.route)
    }

    @Test
    fun testHomeScreenWithLanguageChange() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            FarmCollectorTheme {
                Home(
                    navController = navController,
                    languageViewModel = LanguageViewModel(testApplication),
                    languages = listOf(Language("en", "English"), Language("es", "Spanish"))
                )
            }
        }

        // Simulate changing the language in the language selector
        composeTestRule.onNodeWithTag("LanguageSelector").performClick()
        // Additional checks for language change behavior as needed
    }
}


