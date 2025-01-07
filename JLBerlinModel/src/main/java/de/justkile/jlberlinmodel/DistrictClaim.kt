package de.justkile.jlberlinmodel

import kotlinx.serialization.Serializable

@Serializable
data class DistrictClaim (
    val districtName: String,
    val teamName: String? = null
)
