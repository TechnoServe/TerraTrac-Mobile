package org.technoserve.farmcollector.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.technoserve.farmcollector.R

@Composable
fun SetPolygon(navController: NavController)
{
    // Screen for displaying and setting farm polygon coordinates
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()// Occupy the entire width of the screen
                .fillMaxHeight(0.7f)// Half of the screen height
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Oops! You are Offine!!")
        }
        Column(
            modifier = Modifier.background(Color.DarkGray)
                .padding(16.dp)
                .fillMaxWidth()
                .fillMaxHeight(),

        ) {
            Text(
                text = "Coordinates of the farm polygon",
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "[90.80], [909,989], [9090,9.343], [4343.102.45]")
            Row(
                modifier = Modifier.fillMaxWidth(0.6f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { /*TODO*/ }) {
                    Text(text = "Start")
                }
                Spacer(modifier = Modifier.width(16.dp)) // Add space between the latitude and longitude input fields
                Button(onClick = { /*TODO*/ }) {
                    Text(text = "Add Point")
                }
            }
        }
    }


}