package org.technoserve.farmcollector.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.technoserve.farmcollector.R

@Composable
fun Home(navController: NavController) {
    Column(
        Modifier
            .padding(top = 20.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .padding(top = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_icon),
                null,
                modifier = Modifier
                    .width(80.dp)
                    .height(80.dp)
                    .padding(bottom = 10.dp)
            )
            Text(
                text = stringResource(id = R.string.app_name),
                modifier = Modifier
                    .padding(top = 10.dp)
                    .align(Alignment.CenterHorizontally),
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.light_blue_900),
                style = TextStyle(fontSize = 24.sp)
            )
        }
        Button(
            onClick = {
                navController.navigate("siteList")
            },
            modifier = Modifier
                .padding(30.dp),
            shape = RoundedCornerShape(10.dp),
            enabled = true,
        ) {
            Text(
                text = stringResource(id = R.string.get_started),
                modifier = Modifier.padding(12.dp, 4.dp, 12.dp, 4.dp),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.fillMaxHeight(0.2f))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(id = R.string.app_intro),
                style = TextStyle(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                modifier = Modifier.padding(start = 20.dp, end = 5.dp),
                text = stringResource(id = R.string.developed_by)
            )
            Image(
                painter = painterResource(id = R.drawable.tns_labs),
                null,
                modifier = Modifier
                    .width(130.dp)
                    .height(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
    }
}



