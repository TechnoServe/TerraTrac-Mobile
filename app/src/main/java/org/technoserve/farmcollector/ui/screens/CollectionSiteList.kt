package org.technoserve.farmcollector.ui.screens

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.CollectionSite
import org.technoserve.farmcollector.database.Farm
import org.technoserve.farmcollector.database.FarmViewModel
import org.technoserve.farmcollector.database.FarmViewModelFactory
import org.technoserve.farmcollector.ui.composes.UpdateCollectionDialog

@Composable
fun CollectionSiteList(navController: NavController) {
    val context = LocalContext.current
    val farmViewModel: FarmViewModel = viewModel(
        factory = FarmViewModelFactory(context.applicationContext as Application)
    )
    val selectedIds = remember { mutableStateListOf<Long>() }
    val showDeleteDialog = remember { mutableStateOf(false) }
    var isDialogVisible by remember { mutableStateOf(false) }
    var selectedFarm: Farm? by remember { mutableStateOf(null) }

    val listItems by farmViewModel.readAllSites.observeAsState(listOf())

    fun onDelete(){
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
    if(listItems.size>0){
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                FarmListHeader(
                    title = stringResource(id = R.string.collection_site_list),
                    onAddFarmClicked = { navController.navigate("addSite") },
                  //  onBackClicked = { navController.navigateUp() }
                    onBackClicked = { navController.navigate("home") },
                    showAdd = true
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(listItems) { site ->
                SiteCard(site = site, onCardClick = {
                    // When a SiteCard is clicked, show the dialog
                    navController.navigate("farmList/${site.siteId}")

                }, onDeleteClick = {
                    // When the delete icon is clicked, invoke the onDelete function
                    selectedIds.add(site.siteId)
                    showDeleteDialog.value = true
                })
                Spacer(modifier = Modifier.height(16.dp))
            }


        }
        if (showDeleteDialog.value) {
            DeleteAllDialogPresenter(showDeleteDialog, onProceedFn = { onDelete() })
        }
        if (isDialogVisible) {
            FarmDialog(
                navController = navController,
                farm = selectedFarm,
                onDismiss = {
                    // Dismiss the dialog when needed
                    isDialogVisible = false
                    selectedFarm = null
                }
            )
        }
    }else{


        Column(modifier = Modifier
            .fillMaxSize() ){
            FarmListHeader(
                title = stringResource(id = R.string.collection_site_list),
                onAddFarmClicked = { navController.navigate("addSite") },
                onBackClicked = { navController.navigateUp() },
                showAdd = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp, 8.dp)

                ,
                painter = painterResource(id = R.drawable.no_data2),
                contentDescription = null
            )
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteCard(site: CollectionSite, onCardClick: () -> Unit, onDeleteClick: () -> Unit) {
    val showDialog = remember { mutableStateOf(false) }
    if(showDialog.value)
    {
        UpdateCollectionDialog(
            showDialog = showDialog,
            name = site.name,
            onProceedFn = fun(){})
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .background(Color.White)
                .fillMaxWidth() // 90% of the screen width
                .padding(8.dp),
            onClick = {
                onCardClick()
            }
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = site.name,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .weight(1.1f)
                            .padding(bottom = 4.dp)
                    )
                    // Edit collection sites name
                    IconButton(
                        onClick = {
                            showDialog.value = true
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Update",
                            tint = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.padding(10.dp))
                    // Delete collection sites name
                    IconButton(
                        onClick = {
                            onDeleteClick()
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                }

            }
        }
    }
}