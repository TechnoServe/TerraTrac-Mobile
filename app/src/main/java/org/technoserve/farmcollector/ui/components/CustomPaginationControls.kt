package org.technoserve.farmcollector.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.technoserve.farmcollector.R
/**
 * This function is used to create a custom pagination control with navigation buttons
 *
 * @param currentPage the current page number
 * @param totalPages the total number of pages
 * @param onPageChange a function to be called when the page number is changed
 */
@Composable
fun CustomPaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { if (currentPage > 1) onPageChange(currentPage - 1) },
            enabled = currentPage > 1
        ) {
            Icon(
                painter = painterResource(R.drawable.previous),
                contentDescription = "Previous Page"
            )
        }

        Text("Page $currentPage of $totalPages", modifier = Modifier.padding(horizontal = 16.dp))

        IconButton(
            onClick = { if (currentPage < totalPages) onPageChange(currentPage + 1) },
            enabled = currentPage < totalPages
        ) {
            Icon(painter = painterResource(R.drawable.next), contentDescription = "Next Page")
        }
    }
}