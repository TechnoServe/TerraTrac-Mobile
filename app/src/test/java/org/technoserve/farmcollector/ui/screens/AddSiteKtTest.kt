package org.technoserve.farmcollector.ui.screens
/*
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBuild
import org.technoserve.farmcollector.database.sync.SyncWorker


data class SiteFormState(
    val name: String,
    val agentName: String,
    val phoneNumber: String,
    val email: String,
    val village: String,
    val district: String
)

fun validateForm(formState: SiteFormState): Boolean {
    // Validate each field
    val isNameValid = formState.name.isNotBlank()
    val isAgentNameValid = formState.agentName.isNotBlank()
    val isPhoneNumberValid = formState.phoneNumber.isNotBlank() && isValidPhoneNumber(formState.phoneNumber)
    val isEmailValid = formState.email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(formState.email).matches()
    val isVillageValid = formState.village.isNotBlank()
    val isDistrictValid = formState.district.isNotBlank()

    // Return true only if all validations pass
    return isNameValid && isAgentNameValid && isPhoneNumberValid && isEmailValid && isVillageValid && isDistrictValid
}

// Helper function to validate phone number format
fun isValidPhoneNumber(phoneNumber: String): Boolean {
    val phoneRegex = "^\\+?[0-9]{10,15}\$"
    return phoneNumber.matches(phoneRegex.toRegex())
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [29])
class AddSiteKtTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        // Initialize WorkManager manually for Robolectric
        context = ApplicationProvider.getApplicationContext()
        val config = androidx.work.Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
        WorkManager.initialize(context, config)
    }

    @After
    fun tearDown() {
        // Clean up resources if necessary (Robolectric tests are isolated by default)
    }

    @Test
    fun `test WorkManager initialization`() {
        val workManager = WorkManager.getInstance(context)
        val request = OneTimeWorkRequest.Builder(SyncWorker::class.java).build()

        // Enqueue the request
        workManager.enqueue(request)

        // Retrieve the work info synchronously for validation
        val workInfo = workManager.getWorkInfoById(request.id).get()
        if (workInfo != null) {
            com.google.common.truth.Truth.assertThat(workInfo.state).isEqualTo(WorkInfo.State.ENQUEUED)
        }
    }

    @Test
    fun `test validateForm with valid data`() {
        val formState = SiteFormState(
            name = "Farm Name",
            agentName = "Agent Name",
            phoneNumber = "+1234567890",
            email = "test@example.com",
            village = "Village Name",
            district = "District Name"
        )

        val result = validateForm(formState)

        // Assert the form is valid
        com.google.common.truth.Truth.assertThat(result).isTrue()
    }

    @Test
    fun `test validateForm with invalid phone number`() {
        val formState = SiteFormState(
            name = "Farm Name",
            agentName = "Agent Name",
            phoneNumber = "12345", // Invalid phone number
            email = "test@example.com",
            village = "Village Name",
            district = "District Name"
        )

        val result = validateForm(formState)

        // Assert the form is invalid
        com.google.common.truth.Truth.assertThat(result).isFalse()
    }

    @Test
    fun `test validateForm with empty fields`() {
        val formState = SiteFormState(
            name = "",
            agentName = "",
            phoneNumber = "",
            email = "",
            village = "",
            district = ""
        )

        val result = validateForm(formState)

        // Assert the form is invalid
        com.google.common.truth.Truth.assertThat(result).isFalse()
    }

    @Test
    fun `test WorkManager scheduling`() {
        val workManager = WorkManager.getInstance(context)
        val request = OneTimeWorkRequest.Builder(SyncWorker::class.java).build()

        // Enqueue the request
        workManager.enqueue(request)

        // Allow some time for the WorkManager to process
        Thread.sleep(100) // Simulate scheduler delay

        // Retrieve the work info synchronously for validation
        val workInfo = workManager.getWorkInfoById(request.id).get()
        if (workInfo != null) {
            com.google.common.truth.Truth.assertThat(workInfo.state).isEqualTo(WorkInfo.State.ENQUEUED)
        }
    }
}
*/