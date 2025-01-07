package de.justkile.jlberlin.viewmodel

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.justkile.jlberlin.BackendClient
import de.justkile.jlberlin.model.District
import de.justkile.jlberlin.repository.ClaimRepository
import de.justkile.jlberlin.repository.DistrictRepository
import de.justkile.jlberlin.repository.LocationDataRepository
import de.justkile.jlberlin.repository.TeamRepository
import de.justkile.jlberlinmodel.DistrictClaim
import de.justkile.jlberlinmodel.Team
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClaimState(
    val team: Team?
) {
    fun isClaimed() = team != null
}

class GameViewModel(
    private val districtRepository: DistrictRepository,
    private val teamRepository: TeamRepository,
    private val locationRepository: LocationDataRepository,
    private val claimRepository: ClaimRepository
) : ViewModel() {

    // static data
    val districts = districtRepository.getDistricts()

    val currentLocation = locationRepository.currentLocation.asStateFlow()

    private var _teams = MutableStateFlow<List<Team>>(emptyList())
    val teams = _teams.asStateFlow()

    init {
        viewModelScope.launch {
            _teams.value = teamRepository.getTeams()
        }
    }

    private val _district2claimState: Map<District, MutableStateFlow<ClaimState>> = districts.districts.map { district ->
        district to MutableStateFlow<ClaimState>(ClaimState(null))
    }.toMap()
    fun district2claimState(district: District) = _district2claimState[district]!!.asStateFlow()

    init {
        viewModelScope.launch {
            val claims = claimRepository.getDistrictClaims()
            processClaims(claims)
        }

        viewModelScope.launch {
            claimRepository.listenForNewClaims()
        }

        viewModelScope.launch {
            claimRepository.currentClaims.collect(::processClaims)
        }
    }

    private fun processClaims(claims: List<DistrictClaim>) {
        claims.forEach { claim ->
            val district = districts.districts.find { it.name == claim.districtName }!!
            val team = _teams.value.find { it.name == claim.teamName }
            _district2claimState[district]!!.value = ClaimState(team)
        }
    }

    private var _team : MutableStateFlow<Team?> = MutableStateFlow(null)
    val team = _team.asStateFlow()

    fun selectTeam(team: Team) {
        _team.value = team
    }

    fun createNewTeam(teamName: String) = viewModelScope.launch {
        teamRepository.createNewTeam(teamName)
        _teams.value = teamRepository.getTeams()
    }

    fun claimDistrict(district: District, team: Team) {
        Log.i("GameViewModel", "Claiming district $district for team $team")
        _district2claimState[district]!!.value = ClaimState(team)

        viewModelScope.launch {
            claimRepository.createOrUpdate(DistrictClaim(district.name, team.name))
        }
    }




}