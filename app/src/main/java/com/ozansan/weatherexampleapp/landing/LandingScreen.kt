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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LandingScreen(viewModel: LandingViewModel = viewModel()) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            viewModel.onPermissionResult(isGranted)
        }
    )

    LaunchedEffect(Unit) {
        if (!viewModel.hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = viewModel.locationAddress,
            modifier = Modifier.padding(top = 8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        viewModel.weatherInfo?.let { weatherInfo ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = weatherInfo.weatherDescription,
                    style = MaterialTheme.typography.bodySmall
                )
                Icon(
                    painter = painterResource(id = weatherInfo.weatherIcon),
                    contentDescription = weatherInfo.weatherDescription,
                    modifier = Modifier.size(128.dp)
                )
                Text(
                    text = "${weatherInfo.temperature}°C",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (!viewModel.hasLocationPermission) {
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
