package com.ozansan.weatherexampleapp.di

import com.ozansan.weatherexampleapp.permissions.AndroidPermissionChecker
import com.ozansan.weatherexampleapp.permissions.PermissionChecker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PermissionsModule {

    @Binds
    @Singleton
    abstract fun bindPermissionChecker(
        androidPermissionChecker: AndroidPermissionChecker
    ): PermissionChecker
}
