package org.technoserve.farmcollector.ui.components

import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.viewmodels.FarmViewModel

/**
 * This composable function creates an import file dialog for selecting a file type and invoking the file picker.
 *
 * @param siteId The ID of the collection site to which the imported data will be added.
 * @param onDismiss A function to be called when the dialog is dismissed.
 * @param navController The navigation controller to navigate to the refreshed farm list.
 */
@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun ImportFileDialog(
    siteId: Long,
    onDismiss: () -> Unit,
    navController: NavController,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val farmViewModel: FarmViewModel = viewModel()
    var selectedFileType by remember { mutableStateOf("") }
    var isDropdownMenuExpanded by remember { mutableStateOf(false) }
    // var importCompleted by remember { mutableStateOf(false) }

    // Create a launcher to handle the file picker result
    val importLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            uri?.let {
                coroutineScope.launch {
                    try {
                        val result = farmViewModel.importFile(context, it, siteId)
                        Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                        navController.navigate("farmList/$siteId") // Navigate to the refreshed farm list
                        onDismiss() // Dismiss the dialog after import is complete
                    } catch (e: Exception) {
                        Toast.makeText(context, R.string.import_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    // Create a launcher to handle the file creation result
    val createDocumentLauncher =
        rememberLauncherForActivityResult(
            CreateDocument("todo/todo"),
        ) { uri: Uri? ->
            uri?.let {
                // Get the template content based on the selected file type
                val templateContent = farmViewModel.getTemplateContent(selectedFileType)
                // Save the template content to the created document
                coroutineScope.launch {
                    try {
                        farmViewModel.saveFileToUri(context, it, templateContent)
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            R.string.template_download_failed,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    onDismiss() // Dismiss the dialog
                }
            }
        }

    // Function to download the template file
    fun downloadTemplate() {
        coroutineScope.launch {
            try {
                // Prompt the user to select where to save the file
                createDocumentLauncher.launch(
                    when (selectedFileType) {
                        "csv" -> "farm_template.csv"
                        "geojson" -> "farm_template.geojson"
                        else -> throw IllegalArgumentException("Unsupported file type: $selectedFileType")
                    },
                )
            } catch (e: Exception) {
                Toast.makeText(context, R.string.template_download_failed, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = {
//            onDismiss()
        },
        title = { Text(text = stringResource(R.string.import_file)) },
        text = {
            Column(
                modifier =
                Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
            ) {
                Box(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                        .clickable { isDropdownMenuExpanded = true }
                        .padding(16.dp),
                ) {
                    Text(
                        text = if (selectedFileType.isNotEmpty()) selectedFileType else stringResource(
                            R.string.select_file_type
                        ),
                        color = if (selectedFileType.isNotEmpty()) Color.Black else Color.Gray,
                    )
                    DropdownMenu(
                        expanded = isDropdownMenuExpanded,
                        onDismissRequest = { isDropdownMenuExpanded = false },
                    ) {
                        DropdownMenuItem(onClick = {
                            selectedFileType = "csv"
                            isDropdownMenuExpanded = false
                        }, text = { Text("CSV") })
                        DropdownMenuItem(onClick = {
                            selectedFileType = "geojson"
                            isDropdownMenuExpanded = false
                        }, text = { Text("GeoJSON") })
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { downloadTemplate() },
                    enabled = selectedFileType.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                ) {
                    Text(stringResource(R.string.download_template))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.select_file_to_import),
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                importLauncher.launch("*/*")
            }) {
                Text(stringResource(R.string.select_file))
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(stringResource(R.string.cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.background, // Background that adapts to light/dark
        tonalElevation = 6.dp // Adds a subtle shadow for better UX
    )
}