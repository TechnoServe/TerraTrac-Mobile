package org.technoserve.farmcollector.ui.components

/*
import android.content.Context
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
//@Config(sdk = [33])
@Config(sdk = [33], manifest = Config.NONE)
class FarmListHeaderKtTest {

    @get:Rule
    val composeTestRule = createComposeRule()

//    @Before
//    fun setUp() {
//        val context = ApplicationProvider.getApplicationContext<Context>()
//
//        // Create the configuration for WorkManager
//        val config = Configuration.Builder()
//            .setMinimumLoggingLevel(android.util.Log.DEBUG)
//            .build()
//
//        // Initialize WorkManager for testing
//        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
//    }

    @Test
    fun farmListHeaderDisplaysTitleCorrectly() {
        val title = "Farm List"

        composeTestRule.setContent {
            FarmListHeader(
                title = title,
                onSearchQueryChanged = {},
                onBackClicked = {},
                showSearch = true,
                showRestore = true,
                onRestoreClicked = {}
            )
        }

        // Verify the title is displayed
        composeTestRule.onNodeWithText(title).assertExists()
    }

//    @Test
//    fun searchButtonTogglesSearchField() {
//        val title = "Farm List"
//        var searchQuery = ""
//
//        composeTestRule.setContent {
//            FarmListHeader(
//                title = title,
//                onSearchQueryChanged = { searchQuery = it },
//                onBackClicked = {},
//                showSearch = true,
//                showRestore = false,
//                onRestoreClicked = {}
//            )
//        }
//
//        // Initially, the search field should not be visible
//        composeTestRule.onNodeWithTag("SearchField").assertDoesNotExist()
//
//        // Click on the search button
//        composeTestRule.onNodeWithContentDescription("Search").performClick()
//
//        // The search field should now be visible
//        composeTestRule.onNodeWithTag("SearchField").assertExists()
//
//        // Perform a search
//        val query = "test query"
//        composeTestRule.onNodeWithTag("SearchField").performTextInput(query)
//
//        // Verify that the search query was passed correctly
//        assert(searchQuery == query)
//
//        // Click on the search button again to hide the search field
//        composeTestRule.onNodeWithContentDescription("Search").performClick()
//
//        // The search field should no longer be visible
//        composeTestRule.onNodeWithTag("SearchField").assertDoesNotExist()
//    }

//    @Test
//    fun searchQueryIsClearedWhenBackClicked() {
//        var searchQuery = "some search query"
//
//        composeTestRule.setContent {
//            FarmListHeader(
//                title = "Farm List",
//                onSearchQueryChanged = { searchQuery = it },
//                onBackClicked = {},
//                showSearch = true,
//                showRestore = false,
//                onRestoreClicked = {}
//            )
//        }
//
//        // Click on the search button to show search field
//        composeTestRule.onNodeWithContentDescription("Search").performClick()
//
//        // Perform some search input
//        composeTestRule.onNodeWithTag("SearchField").performTextInput("new query")
//
//        // Verify the search query is updated
//        composeTestRule.onNodeWithTag("SearchField").assertTextEquals("new query")
//
//        // Click on the back button to clear the query
//        composeTestRule.onNodeWithContentDescription("Back").performClick()
//
//        // Verify that the search query is cleared
//        composeTestRule.onNodeWithTag("SearchField").assertTextEquals("")
//    }

    @Test
    fun farmListHeaderHandlesBackClickCorrectly() {
        var backClicked = false

        composeTestRule.setContent {
            FarmListHeader(
                title = "Farm List",
                onSearchQueryChanged = {},
                onBackClicked = { backClicked = true },
                showSearch = true,
                showRestore = false,
                onRestoreClicked = {}
            )
        }

        // Perform click on the back button
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        // Verify that the back click handler is called
        assert(backClicked)
    }

    @Test
    fun restoreButtonVisibility() {
        var restoreClicked = false

        composeTestRule.setContent {
            FarmListHeader(
                title = "Farm List",
                onSearchQueryChanged = {},
                onBackClicked = {},
                showSearch = false,
                showRestore = true,
                onRestoreClicked = { restoreClicked = true }
            )
        }

        // Verify the restore button is visible
        composeTestRule.onNodeWithContentDescription("Restore").assertExists()

        // Perform click on the restore button
        composeTestRule.onNodeWithContentDescription("Restore").performClick()

        // Verify that the restore click handler is called
        assert(restoreClicked)
    }

    @Test
    fun restoreButtonNotVisibleWhenShowRestoreFalse() {
        composeTestRule.setContent {
            FarmListHeader(
                title = "Farm List",
                onSearchQueryChanged = {},
                onBackClicked = {},
                showSearch = false,
                showRestore = false,
                onRestoreClicked = {}
            )
        }

        // Verify the restore button is NOT visible
        composeTestRule.onNodeWithContentDescription("Restore").assertDoesNotExist()
    }
}

 */