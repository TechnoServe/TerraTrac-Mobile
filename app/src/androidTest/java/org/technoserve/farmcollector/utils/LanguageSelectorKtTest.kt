package org.technoserve.farmcollector.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModel
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.verify
import org.technoserve.farmcollector.ui.screens.settings.LanguageSelector
import org.technoserve.farmcollector.viewmodels.LanguageViewModel

/*

// Mock Language class
data class Language(val displayName: String)

// Mock ViewModel
class LanguageViewModel : ViewModel() {
    private val _currentLanguage = MutableStateFlow(
        org.technoserve.farmcollector.database.models.Language(
            "English"
        )
    )
    val currentLanguage: StateFlow<Language> get() = _currentLanguage

    fun selectLanguage(language: Language, context: Context) {
        _currentLanguage.value = language
    }
}


class LanguageSelectorKtTest{
    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockContext = mockk<Context>(relaxed = true)

    @Test
    fun languageSelector_displaysCurrentLanguage() {
        val mockViewModel = mockk<LanguageViewModel>(relaxed = true)
        val languages = listOf(
            org.technoserve.farmcollector.database.models.Language("English"),
            org.technoserve.farmcollector.database.models.Language("French")
        )
        val currentLanguage = MutableStateFlow(languages[0]) // English

        every { mockViewModel.currentLanguage } returns currentLanguage

        composeTestRule.setContent {
            LanguageSelector(viewModel = mockViewModel, languages = languages)
        }

        // Assert the current language is displayed
        composeTestRule.onNodeWithText("English").assertExists()
    }

    @Test
    fun languageSelector_opensDropdownOnClick() {
        val mockViewModel = mockk<LanguageViewModel>(relaxed = true)
        val languages = listOf(
            org.technoserve.farmcollector.database.models.Language("English"),
            org.technoserve.farmcollector.database.models.Language("French")
        )
        val currentLanguage = MutableStateFlow(languages[0]) // English

        every { mockViewModel.currentLanguage } returns currentLanguage

        composeTestRule.setContent {
            LanguageSelector(viewModel = mockViewModel, languages = languages)
        }

        // Click on the row to expand the dropdown menu
        composeTestRule.onNodeWithText("English").performClick()

        // Assert that the dropdown is expanded and displays the available languages
        composeTestRule.onNodeWithText("French").assertExists()
    }

    @SuppressLint("CheckResult")
    @Test
    fun languageSelector_selectsLanguageOnClick() {
        val mockViewModel = mockk<LanguageViewModel>(relaxed = true)
        val languages = listOf(
            org.technoserve.farmcollector.database.models.Language("English"),
            org.technoserve.farmcollector.database.models.Language("French")
        )
        val currentLanguage = MutableStateFlow(languages[0]) // English

        every { mockViewModel.currentLanguage } returns currentLanguage
        every { mockViewModel.selectLanguage(any(), any()) } just Runs

        composeTestRule.setContent {
            LanguageSelector(viewModel = mockViewModel, languages = languages)
        }

        // Expand the dropdown
        composeTestRule.onNodeWithText("English").performClick()

        // Select the "French" option
        composeTestRule.onNodeWithText("French").performClick()

        // Verify that selectLanguage was called with the correct language
        verify { mockViewModel.selectLanguage(
            org.technoserve.farmcollector.database.models.Language(
                "French"
            ), mockContext) }
    }
}

 */