package com.ozansan.weatherexampleapp.di

import com.ozansan.weatherexampleapp.geo.GeocodingRepository
import com.ozansan.weatherexampleapp.geo.GeocodingService
import com.ozansan.weatherexampleapp.geo.LocationClient
import com.ozansan.weatherexampleapp.geo.LocationService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    @Binds
    @Singleton
    abstract fun bindGeocodingService(geocodingRepository: GeocodingRepository): GeocodingService

    @Binds
    @Singleton
    abstract fun bindLocationService(locationClient: LocationClient): LocationService
}
