package org.technoserve.farmcollector

/*
import android.Manifest
import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

//@RunWith(AndroidJUnit4::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MainActivityKtTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var darkModePref: SharedPreferences
    private lateinit var app: Application

    @Before
    fun setUp() {
       // Intents.init()
        app = ApplicationProvider.getApplicationContext()
//        sharedPreferences = app.getSharedPreferences("FarmCollector", ComponentActivity.MODE_PRIVATE)
//        darkModePref = app.getSharedPreferences("theme_mode", ComponentActivity.MODE_PRIVATE)
    }
    @After
    fun tearDown() {
       // Intents.release()
    }

    @Test
    fun testDarkModeApplied() {
        // Set dark mode in SharedPreferences
        darkModePref.edit().putBoolean("dark_mode", true).apply()

        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            assertTrue(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    @Test
    fun testLightModeApplied() {
        // Set light mode in SharedPreferences
        darkModePref.edit().putBoolean("dark_mode", false).apply()

        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            assertTrue(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    @Test
    fun testSharedPreferencesKeysRemoved() {
        // Set values in SharedPreferences
        sharedPreferences.edit().putString("plot_size", "100").apply()
        sharedPreferences.edit().putString("selectedUnit", "meters").apply()

        // Launch the activity
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            // Check that keys are removed
            assertTrue(!sharedPreferences.contains("plot_size"))
            assertTrue(!sharedPreferences.contains("selectedUnit"))
        }
    }

    @Test
    fun testPermissionsRequestedOnLaunch() {
        val permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            permissions.forEach { permission ->
                // Check if permission was requested
                assertTrue(activity.shouldShowRequestPermissionRationale(permission))
            }
        }
    }

//    @Test
//    fun testUpdateAlertShownWhenUpdateAvailable() = runBlocking {
//        // Create a mock AppUpdateViewModel
//        val appUpdateViewModel = mock(AppUpdateViewModel::class.java)
//        `when`(appUpdateViewModel.updateAvailable).thenReturn(true)
//
//        val scenario = ActivityScenario.launch(MainActivity::class.java)
//
//        scenario.onActivity { activity ->
//            // Verify that the update alert dialog is shown
//            assertTrue(activity.isDialogVisible ("update_alert_dialog"))
//        }
//    }

//    @Test
//    fun testExitDialogShownOnBackPress() {
//        val scenario = ActivityScenario.launch(MainActivity::class.java)
//
//        scenario.onActivity { activity ->
//            // Simulate back press
//            activity.onBackPressedDispatcher.onBackPressed()
//
//            // Verify that the exit confirmation dialog is shown
//            assertTrue(activity.isDialogVisible("exit_confirmation_dialog"))
//        }
//    }

//    @Test
//    fun testNavigationToHome() {
//        val scenario = ActivityScenario.launch(MainActivity::class.java)
//
//        scenario.onActivity { activity ->
//            val navController = findNavController(activity, R.id.nav_host_fragment)
//
//            // Verify navigation to home screen
//            assertThat(navController.currentDestination?.route).isEqualTo(Routes.HOME)
//        }
//    }

//    @Test
//    fun testLocaleUpdatedOnLanguageChange() = runBlocking {
//        val languageViewModel = mock(LanguageViewModel::class.java)
//        val savedStateHandle = SavedStateHandle().apply {
//            set("language", "es")  // Simulate Spanish language selection
//        }
//
//        val scenario = ActivityScenario.launch(MainActivity::class.java)
//
//        scenario.onActivity { activity ->
//            verify(languageViewModel).updateLocale(app, Locale("es"))
//        }
//    }

//    @Test
//    fun testOpenPlayStoreOnUpdateConfirm() {
//        val scenario = ActivityScenario.launch(MainActivity::class.java)
//
//        scenario.onActivity { activity ->
//            val intent = Intent(Intent.ACTION_VIEW).apply {
//                data = Uri.parse("market://details?id=${activity.packageName}")
//            }
//
//            // Verify that the Play Store intent is correctly created
//            assertThat(intent).hasAction(Intent.ACTION_VIEW)
//            assertThat(intent.data).isEqualTo(Uri.parse("market://details?id=${activity.packageName}"))
//        }
//    }
}
*/