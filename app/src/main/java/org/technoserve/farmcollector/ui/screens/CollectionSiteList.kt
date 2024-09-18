package org.technoserve.farmcollector.ui.screens

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.CollectionSite
import org.technoserve.farmcollector.database.FarmViewModel
import org.technoserve.farmcollector.database.FarmViewModelFactory
import org.technoserve.farmcollector.ui.composes.UpdateCollectionDialog
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.Alignment.Companion.BottomEnd

@Composable
fun CollectionSiteList(navController: NavController) {
    val context = LocalContext.current
    val farmViewModel: FarmViewModel =
        viewModel(
            factory = FarmViewModelFactory(context.applicationContext as Application),
        )
    val selectedIds = remember { mutableStateListOf<Long>() }
    val showDeleteDialog = remember { mutableStateOf(false) }

    val listItems by farmViewModel.readAllSites.observeAsState(listOf())

    val (searchQuery, setSearchQuery) = remember { mutableStateOf("") }

    fun onDelete() {
        val toDelete = mutableListOf<Long>()
        toDelete.addAll(selectedIds)
        farmViewModel.deleteListSite(toDelete)
        selectedIds.removeAll(selectedIds)
        showDeleteDialog.value = false
    }

    fun refreshListItems() {
        // TODO: update saved predictions list when db gets updated
        //  currently using a terrible makeshift solution
        navController.navigate("home")
        navController.navigate("farmList") {
            navController.graph.startDestinationRoute?.let { route ->
                popUpTo(route) {
                    saveState = true
                }
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    // State to manage the loading status
    val isLoading = remember { mutableStateOf(true) }

    // Simulate a network request or data loading
    LaunchedEffect(Unit) {
        // Simulate a delay for loading
        delay(500) // Adjust the delay as needed
        // After loading data, set isLoading to false
        isLoading.value = false
    }
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//    ) {
    Scaffold(
        topBar = {
        FarmListHeader(
            title = stringResource(id = R.string.collection_site_list),
            onSearchQueryChanged = setSearchQuery,
            onAddFarmClicked = { navController.navigate("addSite") },
            onBackSearchClicked = { navController.navigate("siteList") },
            onBackClicked = { navController.navigate("home") },
            showAdd = true,
            showSearch = true,
        )
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate("addSite")
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(end= 0.dp, bottom = 48.dp)
                        .background(MaterialTheme.colorScheme.background).align(BottomEnd)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add a Site")
                }
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                Spacer(modifier = Modifier.height(8.dp))

//                if (isSearchActive) {
//                    OutlinedTextField(
//                        value = searchQuery,
//                        onValueChange = { setSearchQuery = it },
//                        placeholder = { Text("Search...") },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 8.dp)
//                            .padding(top = 8.dp),
//                        singleLine = true,
//                        leadingIcon = {
//                            IconButton(onClick = { isSearchActive = false; setSearchQuery = "" }) {
//                                Icon(Icons.Default.ArrowBack, contentDescription = "Close Search")
//                            }
//                        },
//                        trailingIcon = {
//                            if (searchQuery.isNotEmpty()) {
//                                IconButton(onClick = { setSearchQuery = "" }) {
//                                    Icon(Icons.Default.Clear, contentDescription = "Clear Search")
//                                }
//                            }
//                        }
//                    )
//                }

                // Show loader while data is loading
                if (isLoading.value) {
                    // Show loader while data is loading
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    if (listItems.isNotEmpty()) {
                        // Show list of items after loading is complete
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            // Filter the list based on the search query
                            val filteredList = listItems.filter {
                                it.name.contains(searchQuery, ignoreCase = true)
                            }

                            // Display a message if no results are found
                            if (searchQuery.isNotEmpty() && filteredList.isEmpty()) {
                                item {
                                    Text(
                                        text = stringResource(R.string.no_results_found),
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            } else {
                                // Display the list of filtered items
                                items(filteredList) { site ->
                                    siteCard(
                                        site = site,
                                        onCardClick = {
                                            navController.navigate("farmList/${site.siteId}")
                                        },
                                        onDeleteClick = {
                                            selectedIds.add(site.siteId)
                                            showDeleteDialog.value = true
                                        },
                                        farmViewModel = farmViewModel,
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Image(
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                                .padding(16.dp, 8.dp),
                            painter = painterResource(id = R.drawable.no_data2),
                            contentDescription = null,
                        )
                    }
                }
            }
        },
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primary)
            .fillMaxWidth(),
       //  modifier = Modifier.padding(16.dp).background(MaterialTheme.colorScheme.background)
    )

        // Display delete dialog if showDeleteDialog is true
        if (showDeleteDialog.value) {
            DeleteAllDialogPresenter(showDeleteDialog, onProceedFn = { onDelete() })
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun siteCard(
    site: CollectionSite,
    onCardClick: () -> Unit,
    onDeleteClick: () -> Unit,
    farmViewModel: FarmViewModel,
) {
    val showDialog = remember { mutableStateOf(false) }
    if (showDialog.value) {
        UpdateCollectionDialog(
            site = site,
            showDialog = showDialog,
            farmViewModel = farmViewModel,
        )
    }
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val iconColor = if (isDarkTheme) Color.White else Color.Black

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(top = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ElevatedCard(
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 6.dp,
                ),
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth() // 90% of the screen width
                    .padding(8.dp),
            onClick = {
                onCardClick()
            },
        ) {
            Column(
                modifier =
                    Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .weight(1.1f)
                                .padding(bottom = 4.dp),
                    ) {
                        Text(
                            text = site.name,
                            style =
                                MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                ),
                            modifier =
                                Modifier
                                    .padding(bottom = 1.dp),
                        )
                        Text(
                            text = "${stringResource(id = R.string.agent_name)}: ${site.agentName}",
                            style = MaterialTheme.typography.bodySmall.copy(color = textColor),
                            modifier =
                                Modifier
                                    .padding(bottom = 1.dp),
                        )
                        if (site.phoneNumber.isNotEmpty()) {
                            Text(
                                text = "${stringResource(id = R.string.phone_number)}: ${site.phoneNumber}",
                                style = MaterialTheme.typography.bodySmall.copy(color = textColor),
                            )
                        }
                    }
                    // Edit collection sites name
                    IconButton(
                        onClick = {
                            showDialog.value = true
                        },
                        modifier =
                            Modifier
                                .size(24.dp)
                                .padding(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Update",
                            tint = iconColor,
                        )
                    }
                    Spacer(modifier = Modifier.padding(10.dp))
                    // Delete collection sites name
                    IconButton(
                        onClick = {
                            onDeleteClick()
                        },
                        modifier =
                            Modifier
                                .size(24.dp)
                                .padding(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                        )
                    }
                }
            }
        }
    }
}
