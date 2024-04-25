package org.technoserve.farmcollector.ui.composes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.CollectionSite
import org.technoserve.farmcollector.database.FarmViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateCollectionDialog(
    site: CollectionSite, showDialog: MutableState<Boolean>,
    farmViewModel: FarmViewModel
) {
    var name by rememberSaveable { mutableStateOf("") }
    var agentName by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var village by rememberSaveable { mutableStateOf("") }
    var district by rememberSaveable { mutableStateOf("") }

    name = site.name
    agentName = site.agentName
    phoneNumber = site.phoneNumber
    email = site.email
    village = site.village
    district = site.district

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
                    )
                    Spacer(modifier = Modifier.padding(vertical = 10.dp))
                    TextField(
                        value = agentName,
                        onValueChange = { agentName = it },
                        label = { Text(stringResource(id = R.string.agent_name)) },
                    )
                    Spacer(modifier = Modifier.padding(vertical = 10.dp))
                    TextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text(stringResource(id = R.string.phone_number)) },
                    )
                    Spacer(modifier = Modifier.padding(vertical = 10.dp))
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(id = R.string.email)) },
                    )
                    Spacer(modifier = Modifier.padding(vertical = 10.dp))
                    TextField(
                        value = village,
                        onValueChange = { village = it },
                        label = { Text(stringResource(id = R.string.village)) },
                    )
                    Spacer(modifier = Modifier.padding(vertical = 10.dp))
                    TextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text(stringResource(id = R.string.district)) },
                    )
                }
            },

            confirmButton = {
                TextButton(onClick = {
                    site.name = name
                    site.agentName = agentName
                    site.phoneNumber = phoneNumber
                    site.email = email
                    site.village = village
                    site.district = district
                    farmViewModel.updateSite(site)
                    showDialog.value = false
                }) {
                    Text(text = stringResource(id = R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text(text = stringResource(id = R.string.no))
                }
            }
        )
    }
}