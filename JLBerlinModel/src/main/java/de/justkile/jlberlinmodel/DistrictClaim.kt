package de.justkile.jlberlinmodel

import kotlinx.serialization.Serializable

@Serializable
data class DistrictClaim (
    val districtName: String = "",
    val claimTimeInSeconds: Int = 0,
    val teamName: String? = null
) {
    fun isClaimed() : Boolean {
        return teamName != null
    }
}
