package de.justkile.jlberlin.repository

import android.util.Log
import de.justkile.jlberlin.datasource.ClaimRemoteDataSource
import de.justkile.jlberlinmodel.DistrictClaim
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ClaimRepository (
    private val claimRemoteDataSource: ClaimRemoteDataSource,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
){

    private var _currentClaims = MutableStateFlow(emptyList<DistrictClaim>())
    val currentClaims = _currentClaims.asStateFlow()

    suspend fun getDistrictClaims() = claimRemoteDataSource.getDistrictClaims()

    suspend fun createOrUpdate(claim: DistrictClaim) = claimRemoteDataSource.createOrUpdate(claim)

    fun listenForNewClaims() {
        claimRemoteDataSource.addListener(
            onClaimsChanged = { _currentClaims.value = it },
            onError = { Log.e("ClaimRepository","Error while listening for new claims", it) }
        )
    }

}