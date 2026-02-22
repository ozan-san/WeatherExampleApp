package com.ozansan.weatherexampleapp.landing

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ozansan.weatherexampleapp.landing.bottombar.LandingBottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingScreen(viewModel: LandingViewModel = viewModel()) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            viewModel.onPermissionResult(isGranted)
        }
    )

    val hasLocationPermission by viewModel.hasLocationPermission.collectAsState()
    val locationAddress by viewModel.locationAddress.collectAsState()
    val weatherInfo by viewModel.weatherInfo.collectAsState()
    val weeklyWeatherInfo by viewModel.weeklyWeatherInfo.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = locationAddress,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            weatherInfo?.let {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = it.weatherDescription,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Icon(
                        painter = painterResource(id = it.weatherIcon),
                        contentDescription = it.weatherDescription,
                        modifier = Modifier.size(128.dp)
                    )
                    Text(
                        text = "${it.temperature}°C",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            weeklyWeatherInfo?.let {
                LandingBottomBar(it)
            }


            // The button is only shown if permission is denied, allowing the user to retry.
            if (!hasLocationPermission) {
                Button(
                    onClick = {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Request Location Permission")
                }
            }
        }
    }
}
