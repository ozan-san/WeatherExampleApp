package com.ozansan.weatherexampleapp.geo

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationService {
    fun getLocationUpdates(): Flow<Location>
}
