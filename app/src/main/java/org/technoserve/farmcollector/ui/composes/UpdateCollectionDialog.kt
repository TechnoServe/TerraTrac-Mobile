package org.technoserve.farmcollector.ui.composes

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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

    if (name.isBlank()) {
        isValid = false
    }

    if (agentName.isBlank()) {
        isValid = false
    }

    if (village.isBlank()) {
        isValid = false
    }

    if (district.isBlank()) {
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

    if (showDialog.value) {
        AlertDialog(
            modifier = Modifier.padding(horizontal = 10.dp),
            onDismissRequest = { showDialog.value = false },
            title = { Text(stringResource(id = R.string.update_site)) },
            text = {
                Column {
                    Text(stringResource(id = R.string.confirm_update_site))
                    Spacer(modifier = Modifier.padding(vertical = 10.dp))
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(id = R.string.site_name)) },
                        isError = name.isBlank(),
                    )
                    Spacer(modifier = Modifier.padding(vertical = 10.dp))
                    TextField(
                        value = agentName,
                        onValueChange = { agentName = it },
                        label = { Text(stringResource(id = R.string.agent_name)) },
                        isError = agentName.isBlank(),
                    )
                    Spacer(modifier = Modifier.padding(vertical = 10.dp))
                    TextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text(stringResource(id = R.string.phone_number)) },
                        supportingText = {
                            if (!isValidPhoneNumber(phoneNumber) && phoneNumber.isNotBlank()) Text(stringResource(id = R.string.invalid_phone_number))
                        },
                        isError = phoneNumber.isNotBlank() && !isValidPhoneNumber(phoneNumber),
                        colors =
                            TextFieldDefaults.textFieldColors(
                                errorLeadingIconColor = Color.Red,
                            ),
                    )
                    Spacer(modifier = Modifier.padding(vertical = 10.dp))
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(id = R.string.email)) },
                        supportingText = {
                            if (email.isNotBlank() && !email.contains("@")) Text(stringResource(id=R.string.error_invalid_email_address))
                        },
                        isError = email.isNotBlank() && !email.contains("@"),
                        colors =
                            TextFieldDefaults.textFieldColors(
                                errorLeadingIconColor = Color.Red,
                            ),
                    )
                    Spacer(modifier = Modifier.padding(vertical = 10.dp))
                    TextField(
                        value = village,
                        onValueChange = { village = it },
                        label = { Text(stringResource(id = R.string.village)) },
                        isError = village.isBlank(),
                    )
                    Spacer(modifier = Modifier.padding(vertical = 10.dp))
                    TextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text(stringResource(id = R.string.district)) },
                        isError = district.isBlank(),
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
        )
    }
}
