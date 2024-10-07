package org.technoserve.farmcollector.ui.composes

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.CollectionSite
import org.technoserve.farmcollector.database.FarmViewModel

fun isValidPhoneNumber(phoneNumber: String): Boolean {
    val regex = Regex("^\\+?(?:[0-9] ?){6,14}[0-9]\$")
    return regex.matches(phoneNumber)
}

fun validateForm(
    name: String,
    agentName: String,
    phoneNumber: String,
    email: String,
    village: String,
    district: String,
): Boolean {
    var isValid = true // Reset isValid to true before starting validation
    val textWithNumbersRegex = Regex(".*[a-zA-Z]+.*") // Ensures there is at least one letter

    if (name.isBlank() || !name.matches(textWithNumbersRegex)) {
        isValid = false
    }

    if (agentName.isBlank() || !agentName.matches(textWithNumbersRegex)) {
        isValid = false
    }

    if (village.isBlank() || !village.matches(textWithNumbersRegex)) {
        isValid = false
    }

    if (district.isBlank() || !district.matches(textWithNumbersRegex)) {
        isValid = false
    }

    if (phoneNumber.isNotBlank() && !isValidPhoneNumber(phoneNumber)) {
        isValid = false
    }

    if (email.isNotBlank() && !email.contains("@")) {
        isValid = false
    }

    return isValid
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateCollectionDialog(
    site: CollectionSite,
    showDialog: MutableState<Boolean>,
    farmViewModel: FarmViewModel,
) {
    val context = LocalContext.current as Activity
    var name by rememberSaveable { mutableStateOf(site.name) }
    var agentName by rememberSaveable { mutableStateOf(site.agentName) }
    var phoneNumber by rememberSaveable { mutableStateOf(site.phoneNumber) }
    var email by rememberSaveable { mutableStateOf(site.email) }
    var village by rememberSaveable { mutableStateOf(site.village) }
    var district by rememberSaveable { mutableStateOf(site.district) }
    var isValid by remember { mutableStateOf(true) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // FocusRequester for each TextField
    val nameFocusRequester = remember { FocusRequester() }
    val agentNameFocusRequester = remember { FocusRequester() }
    val phoneNumberFocusRequester = remember { FocusRequester() }
    val emailFocusRequester = remember { FocusRequester() }
    val villageFocusRequester = remember { FocusRequester() }
    val districtFocusRequester = remember { FocusRequester() }

    if (showDialog.value) {
        AlertDialog(
            modifier = Modifier.padding(horizontal = 10.dp),
            onDismissRequest = { showDialog.value = false },
            title = { Text(stringResource(id = R.string.update_site)) },
            text = {
                // Scrollable column
                Column(
                    modifier = Modifier
                        .fillMaxHeight(0.75f) // Set max height to limit size
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(stringResource(id = R.string.confirm_update_site))
                    Spacer(modifier = Modifier.padding(vertical = 10.dp))

                    // Name Field
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(id = R.string.site_name)) },
                        isError = name.isBlank(),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { agentNameFocusRequester.requestFocus() }
                        ),
                        modifier = Modifier.focusRequester(nameFocusRequester)
                    )
                    Spacer(modifier = Modifier.padding(vertical = 10.dp))

                    // Agent Name Field
                    TextField(
                        value = agentName,
                        onValueChange = { agentName = it },
                        label = { Text(stringResource(id = R.string.agent_name)) },
                        isError = agentName.isBlank(),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { phoneNumberFocusRequester.requestFocus() }
                        ),
                        modifier = Modifier.focusRequester(agentNameFocusRequester)
                    )
                    Spacer(modifier = Modifier.padding(vertical = 10.dp))

                    // Phone Number Field
                    TextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text(stringResource(id = R.string.phone_number)) },
                        supportingText = {
                            if (!isValidPhoneNumber(phoneNumber) && phoneNumber.isNotBlank()) Text(stringResource(id = R.string.invalid_phone_number))
                        },
                        isError = phoneNumber.isNotBlank() && !isValidPhoneNumber(phoneNumber),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { emailFocusRequester.requestFocus() }
                        ),
                        modifier = Modifier.focusRequester(phoneNumberFocusRequester)
                    )
                    Spacer(modifier = Modifier.padding(vertical = 10.dp))

                    // Email Field
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(id = R.string.email)) },
                        supportingText = {
                            if (email.isNotBlank() && !email.contains("@")) Text(stringResource(id = R.string.error_invalid_email_address))
                        },
                        isError = email.isNotBlank() && !email.contains("@"),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { villageFocusRequester.requestFocus() }
                        ),
                        modifier = Modifier.focusRequester(emailFocusRequester)
                    )
                    Spacer(modifier = Modifier.padding(vertical = 10.dp))

                    // Village Field
                    TextField(
                        value = village,
                        onValueChange = { village = it },
                        label = { Text(stringResource(id = R.string.village)) },
                        isError = village.isBlank(),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { districtFocusRequester.requestFocus() }
                        ),
                        modifier = Modifier.focusRequester(villageFocusRequester)
                    )
                    Spacer(modifier = Modifier.padding(vertical = 10.dp))

                    // District Field
                    TextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text(stringResource(id = R.string.district)) },
                        isError = district.isBlank(),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        modifier = Modifier.focusRequester(districtFocusRequester)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (validateForm(name, agentName, phoneNumber, email, village, district)) {
                        showConfirmDialog = true
                    } else {
                        Toast.makeText(context, R.string.fill_form, Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(text = stringResource(id = R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text(text = stringResource(id = R.string.no))
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        )
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(stringResource(id = R.string.confirm_update)) },
            text = { Text(stringResource(id = R.string.are_you_sure_update)) },
            confirmButton = {
                TextButton(onClick = {
                    site.name = name
                    site.agentName = agentName
                    site.phoneNumber = phoneNumber
                    site.email = email
                    site.village = village
                    site.district = district
                    farmViewModel.updateSite(site)
                    showConfirmDialog = false
                    showDialog.value = false
                }) {
                    Text(text = stringResource(id = R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(text = stringResource(id = R.string.no))
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        )
    }
}