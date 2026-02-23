package com.ozansan.weatherexampleapp.geo


import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class GeocodingRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : GeocodingRepository {

    private val geocoder = Geocoder(context)

    override suspend fun reverseGeocode(latitude: Double, longitude: Double): List<Address>? =
        withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(
                            latitude,
                            longitude,
                            1,
                            object : Geocoder.GeocodeListener {
                                override fun onGeocode(addresses: MutableList<Address>) {
                                    continuation.resume(addresses)
                                }

                                override fun onError(errorMessage: String?) {
                                    continuation.resumeWithException(IOException(errorMessage))
                                }
                            })
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitude, longitude, 1)
                }
            } catch (e: IOException) {
                // Handle exceptions
                null
            }
        }
}
