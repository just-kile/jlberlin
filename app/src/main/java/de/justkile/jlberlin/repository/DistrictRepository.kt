package de.justkile.jlberlin.repository

import de.justkile.jlberlin.datasource.DistrictLocalDataSource

class DistrictRepository(
    private val districtLocalDataSource: DistrictLocalDataSource
) {

    fun getDistricts() = districtLocalDataSource.districts

}