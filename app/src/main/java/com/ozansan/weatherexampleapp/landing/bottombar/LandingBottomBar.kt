package com.ozansan.weatherexampleapp.landing.bottombar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ozansan.weatherexampleapp.landing.state.DailyWeatherInfo
import kotlinx.collections.immutable.ImmutableList

@Composable
fun LandingBottomBar(
    weatherInfo: ImmutableList<DailyWeatherInfo>
) {
    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        itemsIndexed(weatherInfo) { index, dailyInfo ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = dailyInfo.dateDisplayName,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Icon(
                        painter = painterResource(id = dailyInfo.weatherIcon),
                        contentDescription = null, // Decorative icon
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "${dailyInfo.temperatureMax.toInt()}/${dailyInfo.temperatureMin.toInt()}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (index < weatherInfo.size - 1) {
                    VerticalDivider(
                        modifier = Modifier.height(80.dp),
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}