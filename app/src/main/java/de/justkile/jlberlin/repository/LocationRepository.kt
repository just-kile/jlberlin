package de.justkile.jlberlin.repository

import android.util.Log
import de.justkile.jlberlin.datasource.LocationRemoteDataSource
import de.justkile.jlberlin.model.Coordinate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class LocationDataRepository(
    private val locationRemoteDataSource: LocationRemoteDataSource
) {

    val currentLocation = MutableStateFlow<Coordinate?>(null)

    init {
        locationRemoteDataSource.observeLocation { coordinate ->
            Log.i("LocationDataRepository", "New location: $coordinate")
            currentLocation.value = coordinate
        }
    }
}