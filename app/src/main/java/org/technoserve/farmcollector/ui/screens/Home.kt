package org.technoserve.farmcollector.ui.screens

import org.technoserve.farmcollector.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun Home(navController: NavController) {
    Column(
        Modifier
            .padding(top = 20.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Image( painter = painterResource(id = R.drawable.technoserve_logo_vector), null, modifier = Modifier.fillMaxHeight(0.4f),)
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
                    text = stringResource(id = R.string.collection_site_list),
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
        Text(modifier = Modifier.padding(20.dp), text = stringResource(id = R.string.developed_by))
        Spacer(modifier = Modifier.height(5.dp))
    }
}



