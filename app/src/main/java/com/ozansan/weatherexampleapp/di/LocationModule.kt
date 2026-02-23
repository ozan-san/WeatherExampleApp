package com.ozansan.weatherexampleapp.di

import com.ozansan.weatherexampleapp.geo.GeocodingRepositoryImpl
import com.ozansan.weatherexampleapp.geo.GeocodingRepository
import com.ozansan.weatherexampleapp.geo.LocationRepositoryImpl
import com.ozansan.weatherexampleapp.geo.LocationRepository
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
    abstract fun bindGeocodingService(geocodingUtilImpl: GeocodingRepositoryImpl): GeocodingRepository

    @Binds
    @Singleton
    abstract fun bindLocationService(locationUtilImpl: LocationRepositoryImpl): LocationRepository
}
