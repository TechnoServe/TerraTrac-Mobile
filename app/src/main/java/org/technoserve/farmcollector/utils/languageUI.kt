package org.technoserve.farmcollector.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import org.technoserve.farmcollector.R

@Composable
fun LanguageSelector(viewModel: LanguageViewModel, languages: List<Language>) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    //do on click to set the language when activity loads
    viewModel.selectLanguage(currentLanguage, context)

    Row(
        modifier = Modifier
            .padding(16.dp)
            .clickable { expanded = !expanded }
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_global),
            contentDescription = "Global Icon"
        )
        Text(text = currentLanguage.displayName)
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { language ->
                DropdownMenuItem(
                    onClick = {
                        viewModel.selectLanguage(language, context)
                        expanded = false
                    },
                    text = { Text(text = language.displayName) }
                )
            }
        }
    }
}
