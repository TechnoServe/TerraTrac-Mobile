//package androidTest.navigation
//
//import android.app.Application
//import androidx.compose.ui.test.assertIsDisplayed
//import androidx.compose.ui.test.junit4.createAndroidComposeRule
//import androidx.compose.ui.test.onNodeWithText
//import androidx.compose.ui.test.performClick
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import androidx.test.core.app.ApplicationProvider
//import org.junit.Rule
//import org.junit.Test
//import org.technoserve.farmcollector.MainActivity
//import org.technoserve.farmcollector.Routes
//import org.technoserve.farmcollector.ui.screens.CollectionSiteList
//import org.technoserve.farmcollector.ui.screens.Home
//import org.technoserve.farmcollector.utils.LanguageViewModel
//import org.technoserve.farmcollector.utils.getLocalizedLanguages
//
//class MainActivityTest {
//
//    @get:Rule
//    val composeTestRule = createAndroidComposeRule<MainActivity>()
//
//    private lateinit var languageViewModel: LanguageViewModel
//
//    private val applicationContext = ApplicationProvider.getApplicationContext<Application>()
//
//    val languages = getLocalizedLanguages(applicationContext)
//
//
//    @Test
//    fun testHomeScreenDisplayed() {
//        // Launch the activity
//        composeTestRule.setContent {
//            val navController = rememberNavController()
//            // Add your navigation setup for the test
//            NavHost(navController = navController, startDestination = Routes.HOME) {
//                composable(Routes.HOME) {
//                    Home(navController,languageViewModel, languages)
//                }
//                // Add other destinations if needed
//            }
//        }
//
//        // Assert that the Home screen is displayed
//        composeTestRule.onNodeWithText("Basic mobile app for traceability for compliance with EU deforestation regulation").assertIsDisplayed()
//    }
//
//    @Test
//    fun testNavigationToSiteList() {
//        // Launch the activity
//        composeTestRule.setContent {
//            val navController = rememberNavController()
//            NavHost(navController = navController, startDestination = Routes.HOME) {
//                composable(Routes.HOME) {
//                    Home(navController,languageViewModel, languages)
//                }
//                composable(Routes.SITE_LIST) {
//                    CollectionSiteList(navController)
//                }
//            }
//        }
//
//        // Navigate to the Site List
//        composeTestRule.onNodeWithText("Go to Site List").performClick() // Adjust button text
//        composeTestRule.onNodeWithText("Site List").assertIsDisplayed() // Adjust to your actual text
//    }
//}