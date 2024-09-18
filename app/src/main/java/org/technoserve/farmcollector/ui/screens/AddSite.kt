package org.technoserve.farmcollector.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ContentValues.TAG
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import org.joda.time.Instant
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.CollectionSite
import org.technoserve.farmcollector.database.FarmViewModel
import org.technoserve.farmcollector.database.FarmViewModelFactory

@Composable
fun AddSite(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
//            .padding(16.dp)
    ) {
        FarmListHeader(
            title = stringResource(id = R.string.add_site),
            onSearchQueryChanged = {},
            onAddFarmClicked = { /* Handle adding a farm here */ },
            onBackSearchClicked = {},
            onBackClicked = { navController.popBackStack() },
            showAdd = false,
            showSearch = false,
            showRestore = false,
            onRestoreClicked = {}
        )
        Spacer(modifier = Modifier.height(16.dp))
        SiteForm(navController)
    }
}

@SuppressLint("MissingPermission", "Recycle")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SiteForm(navController: NavController) {
    val context = LocalContext.current as Activity
    var isValid by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var agentName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var showDisclaimerPhone by remember { mutableStateOf(false) }
    var showDisclaimerEmail by remember { mutableStateOf(false) }


    val farmViewModel: FarmViewModel = viewModel(
        factory = FarmViewModelFactory(context.applicationContext as Application)
    )

    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val regex = Regex("^\\+?(?:[0-9] ?){6,14}[0-9]\$")
        return regex.matches(phoneNumber)
    }

    fun validateForm(): Boolean {
        isValid = true  // Reset isValid to true before starting validation

        if (name.isBlank()) {
            isValid = false
            // You can display an error message for this field if needed
        }

        if (agentName.isBlank()) {
            isValid = false
            // You can display an error message for this field if needed
        }
        if (village.isBlank()) {
            isValid = false
            // You can display an error message for this field if needed
        }

        if (district.isBlank()) {
            isValid = false
            // You can display an error message for this field if needed
        }

        if (email.isNotBlank() && !email.contains("@")) {
            isValid = false
            // You can display an error message for this field if needed
        }


        return isValid
    }


    val scrollState = rememberScrollState()
    val fillForm = stringResource(id = R.string.fill_form)

    val (focusRequester1) = FocusRequester.createRefs()
    val (focusRequester2) = FocusRequester.createRefs()
    val (focusRequester3) = FocusRequester.createRefs()
    val (focusRequester4) = FocusRequester.createRefs()
    val (focusRequester5) = FocusRequester.createRefs()
    val (focusRequester6) = FocusRequester.createRefs()

    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val inputLabelColor = if (isDarkTheme) Color.LightGray else Color.DarkGray
    val inputTextColor = if (isDarkTheme) Color.White else Color.Black
    val inputBorder = if (isDarkTheme) Color.LightGray else Color.DarkGray

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(state = scrollState)
    ) {
        Row {
            TextField(
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onDone = { focusRequester1.requestFocus() }
                ),
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(id = R.string.site_name) + " (*)",color = inputLabelColor ) },
                supportingText = { if (!isValid && name.isBlank()) Text(stringResource(R.string.error_site_name_empty)) },
                isError = !isValid && name.isBlank(),
                colors = TextFieldDefaults.textFieldColors(
                    errorLeadingIconColor = Color.Red,
                    cursorColor = inputTextColor,
                    errorCursorColor = Color.Red,
                    focusedIndicatorColor = inputBorder,
                    unfocusedIndicatorColor = inputBorder,
                    errorIndicatorColor = Color.Red
                ),
                modifier = Modifier
                    .fillMaxWidth()
//                    .padding(bottom = 16.dp)
                    .onKeyEvent {
                        if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                            focusRequester1.requestFocus()
                        }
                        false
                    }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onDone = { focusRequester2.requestFocus() }
            ),
            value = agentName,
            onValueChange = { agentName = it },
            label = { Text(stringResource(id = R.string.agent_name) + " (*)",color = inputLabelColor) },
            supportingText = { if (!isValid && agentName.isBlank()) Text(stringResource(R.string.error_agent_name_empty)) },
            isError = !isValid && agentName.isBlank(),
            colors = TextFieldDefaults.textFieldColors(
                errorLeadingIconColor = Color.Red,
                cursorColor = inputTextColor,
                errorCursorColor = Color.Red,
                focusedIndicatorColor = inputBorder,
                unfocusedIndicatorColor = inputBorder,
                errorIndicatorColor = Color.Red
            ),
            modifier = Modifier
                    .fillMaxWidth() // Make the element fill the width of its parent
//                    .padding(bottom = 8.dp) // Add bottom padding
                    .focusRequester(focusRequester2) // Attach a focus requester
                    .onKeyEvent { keyEvent ->
                    // Handle key events
                    if (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                        focusRequester2.requestFocus() // Request focus when Enter key is pressed
                    }
                    false // Indicate that the event is not consumed
                }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Phone
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusRequester3.requestFocus() }
                ),
                value = phoneNumber,
                onValueChange = {
                    phoneNumber = it
                    isValid = phoneNumber.isBlank() || isValidPhoneNumber(phoneNumber)
                },
                label = {
                    Text(
                        stringResource(id = R.string.phone_number,),
                        color = inputLabelColor
                    )
                },
                supportingText = {
                    if (!isValid && phoneNumber.isNotEmpty() && !isValidPhoneNumber(phoneNumber)) Text(
                        stringResource(R.string.error_invalid_phone_number, phoneNumber)
                    )
                },
                isError = !isValid && phoneNumber.isNotEmpty() && !isValidPhoneNumber(phoneNumber),
                colors = TextFieldDefaults.textFieldColors(
                    errorLeadingIconColor = Color.Red,
                    cursorColor = inputTextColor,
                    errorCursorColor = Color.Red,
                    focusedIndicatorColor = inputBorder,
                    unfocusedIndicatorColor = inputBorder,
                    errorIndicatorColor = Color.Red
                ),
                trailingIcon = {
                IconButton(onClick = { showDisclaimerPhone = !showDisclaimerPhone }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.phone_info),
                        tint = inputLabelColor
                    )
                }
            },
            modifier = Modifier
                        .fillMaxWidth() // Make the element fill the width of its parent
//                        .padding(bottom = 16.dp) // Add bottom padding
                        .focusRequester(focusRequester3) // Attach a focus requester
                         .onKeyEvent { keyEvent ->
                        // Handle key events
                        if (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                            focusRequester3.requestFocus() // Request focus when Enter key is pressed
                        }
                        false // Indicate that the event is not consumed
                    }
            )
        if (showDisclaimerPhone) {
            AlertDialog(
                onDismissRequest = { showDisclaimerPhone = false },
                title = {Text(stringResource(id=R.string.phone_number)) },
                text = { Text(stringResource(id=R.string.phone_info)) },
                confirmButton = {
                    TextButton(onClick = { showDisclaimerPhone = false }) {
                        Text(stringResource(id=R.string.ok))
                    }
                },
                containerColor = MaterialTheme.colorScheme.background, // Background that adapts to light/dark
                tonalElevation = 6.dp // Adds a subtle shadow for better UX
            )

        }
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Email
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusRequester4.requestFocus() },
            ),
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(id = R.string.email),color = inputLabelColor) },
            supportingText = {
                if (!isValid && email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(
                        email
                    ).matches()
                )
                    Text(stringResource(R.string.error_invalid_email_address))
            },
            isError = !isValid && email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(
                email
            ).matches(),
            colors = TextFieldDefaults.textFieldColors(
                errorLeadingIconColor = Color.Red,
                cursorColor = inputTextColor,
                errorCursorColor = Color.Red,
                focusedIndicatorColor = inputBorder,
                unfocusedIndicatorColor = inputBorder,
                errorIndicatorColor = Color.Red
            ),
            trailingIcon = {
                IconButton(onClick = { showDisclaimerEmail = !showDisclaimerEmail }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.email_info),
                        tint = inputLabelColor
                    )
                }
            },
            modifier = Modifier
                    .fillMaxWidth() // Make the element fill the width of its parent
//                .padding(bottom = 16.dp) // Add bottom padding
                .focusRequester(focusRequester4) // Attach a focus requester
                .onKeyEvent { keyEvent ->
                    // Handle key events
                    if (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                        focusRequester4.requestFocus() // Request focus when Enter key is pressed
                    }
                    false // Indicate that the event is not consumed
                }
        )
        if (showDisclaimerEmail) {
            AlertDialog(
                onDismissRequest = { showDisclaimerEmail = false },
                title = {Text(stringResource(id=R.string.email)) },
                text = { Text(stringResource(id=R.string.email_info)) },
                confirmButton = {
                    TextButton(onClick = { showDisclaimerEmail = false }) {
                        Text("OK")
                    }
                },
                containerColor = MaterialTheme.colorScheme.background, // Background that adapts to light/dark
                tonalElevation = 6.dp // Adds a subtle shadow for better UX
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onDone = { focusRequester5.requestFocus() }
            ),
            value = village,
            onValueChange = { village = it },
            label = { Text(stringResource(id = R.string.village) + " (*)",color = inputLabelColor) },
            supportingText = { if (!isValid && village.isBlank()) Text(stringResource(R.string.error_village_empty)) },
            isError = !isValid && village.isBlank(),
            colors = TextFieldDefaults.textFieldColors(
                errorLeadingIconColor = Color.Red,
                cursorColor = inputTextColor,
                errorCursorColor = Color.Red,
                focusedIndicatorColor = inputBorder,
                unfocusedIndicatorColor = inputBorder,
                errorIndicatorColor = Color.Red
            ),
            modifier = Modifier
                .fillMaxWidth() // Make the element fill the width of its parent
//                .padding(bottom = 16.dp) // Add bottom padding
                .focusRequester(focusRequester5) // Attach a focus requester
                .onKeyEvent { keyEvent ->
                    // Handle key events
                    if (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                        focusRequester5.requestFocus() // Request focus when Enter key is pressed
                    }
                    false // Indicate that the event is not consumed
                }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusRequester6.requestFocus() }
            ),
            value = district,
            onValueChange = { district = it },
            label = { Text(stringResource(id = R.string.district) + " (*)",color = inputLabelColor) },
            supportingText = { if (!isValid && district.isBlank()) Text(stringResource(R.string.error_district_empty)) },
            isError = !isValid && district.isBlank(),
            colors = TextFieldDefaults.textFieldColors(
                errorLeadingIconColor = Color.Red,
                cursorColor = inputTextColor,
                errorCursorColor = Color.Red,
                focusedIndicatorColor = inputBorder,
                unfocusedIndicatorColor = inputBorder,
                errorIndicatorColor = Color.Red
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester6)
//                .padding(bottom = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (validateForm() && (phoneNumber.isEmpty() || isValidPhoneNumber(phoneNumber))) {
                    addSite(
                        farmViewModel,
                        name,
                        agentName,
                        phoneNumber,
                        email,
                        village,
                        district
                    )
                    val returnIntent = Intent()
                    context.setResult(Activity.RESULT_OK, returnIntent)
                    // context.finish()  // Uncomment if you want to finish the activity
                    navController.navigate("siteList")
                    // Show toast indicating success
                    Toast.makeText(context, R.string.site_added_successfully, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, fillForm, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = stringResource(id = R.string.add_site))
        }
    }
}

fun addSite(
    farmViewModel: FarmViewModel,
    name: String,
    agentName: String,
    phoneNumber: String,
    email: String,
    village: String,
    district: String,
): CollectionSite {
    val site = CollectionSite(
        name,
        agentName,
        phoneNumber,
        email,
        village,
        district,
        createdAt = Instant.now().millis,
        updatedAt = Instant.now().millis
    )
    farmViewModel.addSite(site){isAdded->
        if (isAdded) {
            Log.d(TAG, " site added")
        }
    }
    return site
}