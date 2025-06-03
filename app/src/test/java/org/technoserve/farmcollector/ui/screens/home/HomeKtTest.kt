package org.technoserve.farmcollector.ui.screens.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.technoserve.farmcollector.database.models.Language
import org.technoserve.farmcollector.viewmodels.LanguageViewModel

/*
//@RunWith(RobolectricTestRunner::class)
//@Config(sdk = [33])
@RunWith(AndroidJUnit4::class)
class HomeKtTest{
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var languageViewModel: LanguageViewModel
    private lateinit var navController: NavController
    private lateinit var mockContext: Context

    @Before
    fun setUp() {
        // Mock dependencies
        languageViewModel = mockk(relaxed = true)
        navController = mockk(relaxed = true)
        mockContext = mockk(relaxed = true)
    }

    @Test
    fun testLanguageChangeUpdatesUI() {
        // Arrange: Create a list of languages to test
        val languages = listOf(
            Language("en", "English"),
            Language("fr", "French")
        )

        // Start the Composable
        composeTestRule.setContent {
            Home(
                navController = navController,
                languageViewModel = languageViewModel,
                languages = languages
            )
        }

        // Act: Change the language to French
        val frenchLanguage = languages.find { it.code == "fr" }!!
        languageViewModel.selectLanguage(frenchLanguage, mockContext)

        // Assert: Verify that the currentLanguage state is updated to "fr"
        assert(languageViewModel.currentLanguage.value.code == "fr")
    }

    @SuppressLint("CheckResult")
    @Test
    fun testNavigationWhenGetStartedClicked() {
        // Arrange: Create a list of languages to test
        val languages = listOf(
            Language("en", "English"),
            Language("fr", "French")
        )

        // Start the Composable
        composeTestRule.setContent {
            Home(
                navController = navController,
                languageViewModel = languageViewModel,
                languages = languages
            )
        }

        // Act: Perform click on the "Get Started" button
        composeTestRule.onNodeWithText("Get Started").performClick()

        // Assert: Verify that the navController.navigate method was called with the expected route
        verify { navController.navigate("siteList") }
    }

    @Test
    fun testLanguageSelectorDisplayed() {
        // Arrange: Create a list of languages to test
        val languages = listOf(
            Language("en", "English"),
            Language("fr", "French")
        )

        // Start the Composable
        composeTestRule.setContent {
            Home(
                navController = navController,
                languageViewModel = languageViewModel,
                languages = languages
            )
        }

        // Act: Check if the language selector is displayed
        composeTestRule.onNodeWithText("English").assertIsDisplayed()
        composeTestRule.onNodeWithText("French").assertIsDisplayed()
    }

    @Test
    fun testAppNameDisplayed() {
        // Arrange: Create a list of languages to test
        val languages = listOf(
            Language("en", "English"),
            Language("fr", "French")
        )

        // Start the Composable
        composeTestRule.setContent {
            Home(
                navController = navController,
                languageViewModel = languageViewModel,
                languages = languages
            )
        }

        // Assert: Verify that the app name text is displayed
        composeTestRule.onNodeWithText("App Name").assertIsDisplayed()
    }

    @Test
    fun testDeveloperLabelDisplayed() {
        // Arrange: Create a list of languages to test
        val languages = listOf(
            Language("en", "English"),
            Language("fr", "French")
        )

        // Start the Composable
        composeTestRule.setContent {
            Home(
                navController = navController,
                languageViewModel = languageViewModel,
                languages = languages
            )
        }

        // Assert: Verify that the "Developed by" text is displayed
        composeTestRule.onNodeWithText("Developed by").assertIsDisplayed()
    }
}

 */

//import androidx.compose.ui.test.*
//import androidx.compose.ui.test.junit4.createComposeRule
//import androidx.navigation.testing.TestNavHostController
//import androidx.test.core.app.ApplicationProvider
//import org.junit.Assert.assertEquals
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.mockito.Mockito.mock
//import org.robolectric.RobolectricTestRunner
//import org.robolectric.annotation.Config
//import org.technoserve.farmcollector.database.models.Language
//import org.technoserve.farmcollector.ui.screens.home.Home
//import org.technoserve.farmcollector.viewmodels.LanguageViewModel
//
//@RunWith(RobolectricTestRunner::class)
//@Config(sdk = [33])
//class HomeKtTest {
//
//    @get:Rule
//    val composeTestRule = createComposeRule()
//
//    private lateinit var navController: TestNavHostController
//    private lateinit var languageViewModel: LanguageViewModel
//    private val testLanguages = listOf(
//        Language("en", "English"),
//        Language("es", "Spanish")
//    )
//
//    @Before
//    fun setup() {
//        // Use ApplicationProvider for a context in unit tests
//        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
//        navController = TestNavHostController(context)
//        languageViewModel = mock(LanguageViewModel::class.java)
//    }
//
//    @Test
//    fun homeScreen_displaysAppName() {
//        composeTestRule.setContent {
//            Home(
//                navController = navController,
//                languageViewModel = languageViewModel,
//                languages = testLanguages
//            )
//        }
//
//        composeTestRule
//            .onNodeWithText("TerraTrac") // Replace with the actual string if not resource-based
//            .assertExists()
//            .assertIsDisplayed()
//    }
//
//    @Test
//    fun homeScreen_displaysGetStartedButton() {
//        composeTestRule.setContent {
//            Home(
//                navController = navController,
//                languageViewModel = languageViewModel,
//                languages = testLanguages
//            )
//        }
//
//        composeTestRule
//            .onNodeWithText("Get Started")
//            .assertExists()
//            .assertIsDisplayed()
//            .assertHasClickAction()
//    }
//
//    @Test
//    fun homeScreen_clickGetStartedNavigatesToSiteList() {
//        composeTestRule.setContent {
//            Home(
//                navController = navController,
//                languageViewModel = languageViewModel,
//                languages = testLanguages
//            )
//        }
//
//        composeTestRule
//            .onNodeWithText("Get Started") // Replace with actual string if not resource-based
//            .performClick()
//
//        // Verify navigation occurred
//        assertEquals("siteList", navController.currentDestination?.route)
//    }
//}

