package de.justkile.jlberlin.datasource

import android.annotation.SuppressLint
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import de.justkile.jlberlinmodel.Coordinate

class LocationRemoteDataSource(
    private val locationProvider: FusedLocationProviderClient
) {

    @SuppressLint("MissingPermission")
    fun observeLocation(listener: (Coordinate) -> Unit) =
        locationProvider.requestLocationUpdates(
            LocationRequest.Builder(1000)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build(),
            { location ->
                listener(Coordinate(location.latitude, location.longitude))
            },
            Looper.getMainLooper()
        )

}