package org.technoserve.farmcollector.viewmodels

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.technoserve.farmcollector.database.models.Language
import java.util.Locale

/*
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LanguageViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var languageViewModel: LanguageViewModel
    private lateinit var context: Context

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var resources: Resources

//    @Mock
//    lateinit var sharedPreferences: SharedPreferences

    @Mock
    lateinit var editor: SharedPreferences.Editor


    @Before
    fun setUp() {
        // Mocking Context and SharedPreferences
        context = mock()
        sharedPreferences = mock()
        resources = mock()
        editor = mock() // Initialize editor mock

        // Mocking shared preferences return value
        whenever(
            sharedPreferences.getString(
                "preferred_language",
                Locale.getDefault().language
            )
        ).thenReturn("en")
        whenever(context.getSharedPreferences("settings", Context.MODE_PRIVATE)).thenReturn(
            sharedPreferences
        )
        whenever(context.resources).thenReturn(resources)

        // Initializing the ViewModel
        languageViewModel = LanguageViewModel(ApplicationProvider.getApplicationContext())

        val context = ApplicationProvider.getApplicationContext<Context>()

        // Create the configuration for WorkManager
        val config = androidx.work.Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

        // Initialize WorkManager for testing
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    
    }





    @Test
    fun testGetDefaultLanguage() {
        // Given that the preferred language is "en"
        val defaultLanguage = languageViewModel.getDefaultLanguage()

        // Then it should return the "en" language
        assertEquals("en", defaultLanguage.code)
    }

//    @Test
//    fun testSavePreferredLanguage() {
//        // Given a new language to save
//        val newLanguage = Language("fr", "French")
//
//        // When saving the new language
//        languageViewModel.savePreferredLanguage(newLanguage)
//
//        // Then SharedPreferences should store the new language
//        Mockito.verify(editor).putString("preferred_language", "fr")
//        Mockito.verify(editor).apply()
//
//        // Additionally, verify the current language LiveData is updated
//        assertEquals("fr", languageViewModel.currentLanguage.value?.code)
//    }

//    @Test
//    fun testSelectLanguage() {
//        // Given a language "fr" and a mock context
//        val newLanguage = Language("fr", "French")
//
//        // When selecting the language
//        languageViewModel.selectLanguage(newLanguage, ApplicationProvider.getApplicationContext())
//
//        // Then currentLanguage should be updated
//        assertEquals("fr", languageViewModel.currentLanguage.value?.code)
//
//        // And SharedPreferences should be updated with the new language
//        Mockito.verify(editor).putString("preferred_language", "fr")
//        Mockito.verify(editor).apply()
//    }


    @Test
    fun testUpdateLocale() {
        // Given a new locale "fr" and mock configuration
        val locale = Locale("fr")
        val mockContext = Mockito.mock(Context::class.java)
        val resources = Mockito.mock(Resources::class.java)
        val config = Configuration()

        // Mock resources and configuration
        Mockito.`when`(mockContext.resources).thenReturn(resources)
        Mockito.`when`(resources.configuration).thenReturn(config)

        // When updating the locale
        languageViewModel.updateLocale(mockContext, locale)

        // Then verify the locale and layout direction were updated
        assertEquals(locale, config.locales[0])
        assertEquals(locale.language, config.locale.language)
    }

    @Test
    fun testGetLocalizedLanguages() {
        // Given a mock context with language strings
        val context = ApplicationProvider.getApplicationContext<Context>()
        val languages = languageViewModel.getLocalizedLanguages(context)

        // Then the languages list should contain the predefined languages
        assertEquals(6, languages.size)
        assertTrue(languages.any { it.code == "en" })
        assertTrue(languages.any { it.code == "fr" })
    }
}


//
//@RunWith(RobolectricTestRunner::class)
//@Config(sdk = [33])
//class LanguageViewModelTest {
//
//    @get:Rule
//    val instantTaskExecutorRule = InstantTaskExecutorRule()
//
//    private lateinit var languageViewModel: LanguageViewModel
//
//    @Mock
//    lateinit var sharedPreferences: SharedPreferences
//
//    @Mock
//    lateinit var editor: SharedPreferences.Editor
//
//    @Before
//    fun setUp() {
//        // Initialize Mockito mocks
//        MockitoAnnotations.openMocks(this)
//
//        // Mock SharedPreferences and its Editor
//        Mockito.`when`(sharedPreferences.edit()).thenReturn(editor)
//        Mockito.`when`(editor.putString(Mockito.anyString(), Mockito.anyString())).thenReturn(editor)
//
//        // Mock SharedPreferences behavior for getString
//        Mockito.`when`(
//            sharedPreferences.getString(
//                Mockito.eq("preferred_language"),
//                Mockito.anyString()
//            )
//        ).thenReturn("en")
//
//        // Use Robolectric's Application context
//        val application = ApplicationProvider.getApplicationContext<Application>()
//
//        // Initialize the ViewModel
//        languageViewModel = LanguageViewModel(application)
//    }
//
//    @Test
//    fun testGetDefaultLanguage() {
//        // Given a default language in SharedPreferences is "en"
//        val defaultLanguage = languageViewModel.getDefaultLanguage()
//
//        // Then the default language should match
//        assertEquals("en", defaultLanguage.code)
//    }
//
//    @Test
//    fun testSavePreferredLanguage() {
//        // Given a new language to save
//        val newLanguage = Language("fr", "French")
//
//        // When saving the new language
//        languageViewModel.savePreferredLanguage(newLanguage)
//
//        // Then verify interactions with SharedPreferences
//        Mockito.verify(editor).putString("preferred_language", newLanguage.code)
//        Mockito.verify(editor).apply()
//    }
//
//    @Test
//    fun testSelectLanguage() {
//        // Given a new language "fr"
//        val newLanguage = Language("fr", "French")
//
//        // When selecting the language
//        languageViewModel.selectLanguage(newLanguage, ApplicationProvider.getApplicationContext())
//        // Then verify the current language LiveData is updated
//        assertEquals("fr", languageViewModel.currentLanguage.value?.code)
//
//        // Verify SharedPreferences is updated
//        Mockito.verify(editor).putString("preferred_language", "fr")
//        Mockito.verify(editor).apply()
//    }
//
//    @Test
//    fun testUpdateLocale() {
//        // Given a new locale "fr" and mock configuration
//        val locale = Locale("fr")
//        val mockContext = Mockito.mock(Context::class.java)
//        val resources = Mockito.mock(Resources::class.java)
//        val config = Configuration()
//
//        // Mock resources and configuration
//        Mockito.`when`(mockContext.resources).thenReturn(resources)
//        Mockito.`when`(resources.configuration).thenReturn(config)
//
//        // When updating the locale
//        languageViewModel.updateLocale(mockContext, locale)
//
//        // Then verify the locale and layout direction were updated
//        assertEquals(locale, config.locales[0])
//        assertEquals(locale.language, config.locale.language)
//    }
//
//    @Test
//    fun testGetLocalizedLanguages() {
//        // Given predefined languages in the ViewModel
//        val localizedLanguages = languageViewModel.getLocalizedLanguages(
//            ApplicationProvider.getApplicationContext()
//        )
//
//        // Then verify the list contains expected values
//        assertEquals(6, localizedLanguages.size)
//        assertTrue(localizedLanguages.any { it.code == "en" })
//        assertTrue(localizedLanguages.any { it.code == "fr" })
//    }
//}

 */