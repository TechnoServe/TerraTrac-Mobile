package org.technoserve.farmcollector.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.models.Language
import org.technoserve.farmcollector.viewmodels.LanguageViewModel

/**
 * This composable function is used to display the language selector with dropdown menu
 * It also handles the state change of the selected language and updates the app's language
 *
 * @param viewModel The LanguageViewModel for managing language state
 * @param languages The list of available languages for the dropdown menu
 *
 * Note: This component assumes that the LanguageViewModel is provided with the necessary state and functions.
 * Make sure to inject it into your composable function using dependency injection.
 *
 * Example usage:
 * LanguageSelector(viewModel = languageViewModel, languages = listOf(English, Spanish, French))
 *
 * In the example above, the LanguageSelector composable function will display a dropdown menu with the available languages
 * and update the app's language when a language is selected.
 */

@Composable
fun LanguageSelector(viewModel: LanguageViewModel, languages: List<Language>) {
    // Observe the current language state
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // UI for the language selector
    Row(
        modifier = Modifier
            .padding(16.dp)
            .clickable { expanded = !expanded },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_global),
            contentDescription = "Global Icon"
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = currentLanguage.displayName)

        DropdownMenu(
            expanded = expanded,
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { language ->
                DropdownMenuItem(
                    onClick = {
                        // Change the language when clicked
                        viewModel.selectLanguage(language, context)
                        expanded = false
                    },
                    text = {
                        Text(
                            text = language.displayName,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                )
            }
        }
    }
}
