package org.technoserve.farmcollector.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.models.Farm
import org.technoserve.farmcollector.ui.screens.farms.formatInput
/**
 * A card displaying a farm's information.
 *
 * @param farm The Farm object to display.
 * @param onCardClick A callback to be invoked when the card is clicked.
 * @param onDeleteClick A callback to be invoked when the delete icon is clicked.
 */

@Composable
fun FarmCard(
    farm: Farm,
    onCardClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    val textColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        ElevatedCard(
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCardClick() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // Top Row with Farmer Name, Size, and Delete Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween, // To separate left and right items
                    verticalAlignment = Alignment.Top
                ) {
                    // Column for Farmer Name and Location (Village, District)
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        // Farmer Name
                        Text(
                            text = farm.farmerName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            ),
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .basicMarquee(), // Scrolls long text
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Location Information - Village and District below Farmer Name
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp) // Add spacing between the items
                        ) {
                            // Village Information
                            LocationInfoChip(
                                label = stringResource(id = R.string.village),
                                value = farm.village,
                                modifier = Modifier.fillMaxWidth() // Ensure it takes the full width
                            )

                            // District Information
                            LocationInfoChip(
                                label = stringResource(id = R.string.district),
                                value = farm.district,
                                modifier = Modifier.fillMaxWidth() // Ensure it takes the full width
                            )
                        }
                    }

                    // Center the Farm Size between the Farmer Name/Location and Delete Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally, // Center the size text
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight() // To vertically center the size text
                            .padding(horizontal = 8.dp) // Optional: to give some padding
                    ) {
                        Text(
                            text = "${stringResource(id = R.string.size)}: ${formatInput(farm.size.toString())} ${stringResource(id = R.string.ha)}",
                            style = MaterialTheme.typography.bodySmall.copy(color = textColor),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .padding(4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Delete Button - Compact and accessible on the right side
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.delete_this_farm),
                            tint = MaterialTheme.colorScheme.error,
                            // modifier = Modifier.size(24.dp)
                        )
                    }
                }


                // Needs Update Indicator - Responsive and Subtle
                if (farm.needsUpdate) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 8.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = stringResource(id = R.string.needs_update),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}

// Composable for Location Information Chips
@Composable
fun LocationInfoChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.background)
            .padding(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.wrapContentWidth()
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .weight(1f)
                .basicMarquee(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
