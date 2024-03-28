package org.technoserve.farmcollector.ui.composes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
fun UpdateCollectionDialog(site: CollectionSite, showDialog: MutableState<Boolean>,
                           farmViewModel: FarmViewModel,
                  onProceedFn: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    name = site.name
    if(showDialog.value)
    {
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
                }
            },

            confirmButton = {
                TextButton(onClick = {
                    site.name = name
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