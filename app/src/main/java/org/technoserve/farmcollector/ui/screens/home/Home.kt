package org.technoserve.farmcollector.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import org.technoserve.farmcollector.R
import org.technoserve.farmcollector.database.models.Language
import org.technoserve.farmcollector.ui.theme.Teal
import org.technoserve.farmcollector.ui.theme.Turquoise
import org.technoserve.farmcollector.ui.theme.White
import org.technoserve.farmcollector.ui.screens.settings.LanguageSelector
import org.technoserve.farmcollector.viewmodels.LanguageViewModel
import java.util.Locale


/**
 *
 *  This function is used to Display the home page of our application
 *
 *  @param navController: NavigationController to navigate between screens
 *  @param languageViewModel: ViewModel for managing language settings
 *  @param languages: List of supported languages for the application
 */

@Composable
fun Home(
    navController: NavController,
    languageViewModel: LanguageViewModel,
    languages: List<Language>
) {

    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    val context = LocalContext.current


    LaunchedEffect(currentLanguage) {
        languageViewModel.updateLocale(context = context, Locale(currentLanguage.code))
    }

    Column(
        Modifier
            .padding(top = 20.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Add language selector here and align on the right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            LanguageSelector(viewModel = languageViewModel, languages = languages)
        }

        Column(
            Modifier
                .fillMaxWidth()
               // .fillMaxHeight(0.4f)
                .weight(0.4f)
                .padding(top = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Centering the app icon and text
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp) // Spacing between icon and text
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_icon),
                        contentDescription = null,
//                        modifier = Modifier
//                            .width(80.dp)
//                            .height(80.dp)
                        modifier = Modifier
                            .width(when (LocalConfiguration.current.screenWidthDp) {
                                in 0..320 -> 60.dp // Small screens
                                in 321..600 -> 80.dp // Medium screens
                                else -> 100.dp // Large screens
                            })
                            .height(when (LocalConfiguration.current.screenWidthDp) {
                                in 0..320 -> 60.dp
                                in 321..600 -> 80.dp
                                else -> 100.dp
                            })
                            .padding(bottom = 10.dp)
                    )

                    Text(
                        text = stringResource(id = R.string.app_name),
                        fontWeight = FontWeight.Bold,
                        color = Turquoise, // Using the custom Turquoise color
                        // style = TextStyle(fontSize = 24.sp)
                        style = TextStyle(
                            fontSize = when (LocalConfiguration.current.screenWidthDp) {
                                in 0..320 -> 20.sp
                                in 321..600 -> 24.sp
                                else -> 28.sp
                            }
                        )
                    )
                }
            }

        }

        Box(
            modifier = Modifier
                .padding(30.dp)
                .background(
                    color = Teal,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable {
                    navController.navigate("siteList")
                }
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.get_started),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = White
                ),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.fillMaxHeight(0.2f))

        Box(
//            modifier = Modifier
//                .fillMaxWidth(0.8f)
//                .padding(20.dp)
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(when (LocalConfiguration.current.screenWidthDp) {
                    in 0..320 -> 12.dp
                    in 321..600 -> 16.dp
                    else -> 20.dp
                })
        ) {
            Text(
                text = stringResource(id = R.string.app_intro),
//                style = TextStyle(
//                    fontWeight = FontWeight.Bold,
//                    color = MaterialTheme.colorScheme.onBackground
//                ),
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = when (LocalConfiguration.current.screenWidthDp) {
                        in 0..320 -> 14.sp
                        in 321..600 -> 16.sp
                        else -> 18.sp
                    }
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
                text = stringResource(id = R.string.developed_by),
                color = Teal // Apply Teal color for the developer label
            )
            Image(
                painter = painterResource(id = R.drawable.tns_labs),
                contentDescription = null,
//                modifier = Modifier
//                    .width(130.dp)
//                    .height(20.dp)
                modifier = Modifier
                    .width(when (LocalConfiguration.current.screenWidthDp) {
                        in 0..320 -> 100.dp
                        in 321..600 -> 120.dp
                        else -> 130.dp
                    })
                    .height(when (LocalConfiguration.current.screenWidthDp) {
                        in 0..320 -> 15.dp
                        in 321..600 -> 20.dp
                        else -> 20.dp
                    })
            )
        }

        Spacer(modifier = Modifier.height(5.dp))
    }
}
