package com.ozansan.weatherexampleapp.geo

import android.location.Address

interface GeocodingService {
    suspend fun reverseGeocode(latitude: Double, longitude: Double): List<Address>?
}
