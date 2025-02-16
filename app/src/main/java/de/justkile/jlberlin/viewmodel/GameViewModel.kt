package de.justkile.jlberlin.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.justkile.jlberlin.model.District
import de.justkile.jlberlin.model.HistoryEntry
import de.justkile.jlberlin.repository.ClaimRepository
import de.justkile.jlberlin.repository.DistrictRepository
import de.justkile.jlberlin.repository.HistoryRepository
import de.justkile.jlberlin.repository.LocationDataRepository
import de.justkile.jlberlin.repository.TeamRepository
import de.justkile.jlberlin.ui.theme.TeamColors
import de.justkile.jlberlinmodel.DistrictClaim
import de.justkile.jlberlinmodel.Team
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClaimState(
    val team: Team?,
    val claimTimeInSeconds: Int = 0
) {
    fun isClaimed() = team != null
}

class ColoredTeam (
    val name: String,
    val color: Color
)

class GameViewModel(
    private val districtRepository: DistrictRepository,
    private val teamRepository: TeamRepository,
    private val locationRepository: LocationDataRepository,
    private val claimRepository: ClaimRepository,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    // static data
    val districts = districtRepository.getDistricts()

    private val _currentLocation = locationRepository.currentLocation.asStateFlow()
    private val _currentDistrict = MutableStateFlow<District?>(null)
    val currentDistrict = _currentDistrict.asStateFlow()

    init {
        viewModelScope.launch {
            _currentLocation.collect { coordinate ->
                if (coordinate != null) {
                    val newDistrict = districts.findDistrictByCoordinate(coordinate)
                    if (newDistrict != _currentDistrict.value) {
                        _currentDistrict.value = newDistrict
                    }
                }
            }
        }
    }

    val teams = teamRepository.teams

    init {
        viewModelScope.launch {
            teamRepository.listenForNewTeams()
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
            val team = teams.value.find { it.name == claim.teamName }
            _district2claimState[district]!!.value = ClaimState(team, claim.claimTimeInSeconds)
        }
    }

    private var _team : MutableStateFlow<Team?> = MutableStateFlow(null)
    val team = _team.asStateFlow()

    fun selectTeam(team: Team) {
        _team.value = team
    }

    fun createNewTeam(teamName: String) = viewModelScope.launch {
        teamRepository.createNewTeam(teamName)
    }

    fun claimDistrict(district: District, team: Team, claimTimeInSeconds: Int) {
        Log.i("GameViewModel", "Claiming district $district for team $team")
        _district2claimState[district]!!.value = ClaimState(team)

        viewModelScope.launch {
            claimRepository.createOrUpdate(DistrictClaim(district.name, claimTimeInSeconds, team.name))
            historyRepository.createHistory(team.name, district.name, claimTimeInSeconds)
        }
    }

    private var _team2Score : MutableStateFlow<Map<ColoredTeam, Int>> = MutableStateFlow(emptyMap())
    val team2Score = _team2Score.asStateFlow()

    init {
        viewModelScope.launch {
            val claims = claimRepository.getDistrictClaims()
            calculateScoreBoard(claims)
        }

        viewModelScope.launch {
            claimRepository.listenForNewClaims()
        }

        viewModelScope.launch {
            claimRepository.currentClaims.collect(::calculateScoreBoard)
        }
    }

    fun calculateScoreBoard(claims: List<DistrictClaim>) {
        val team2teamColor = teams.value.map { it to TeamColors[teams.value.indexOf(it)] }.toMap()

        _team2Score.value= teams.value.map { team ->
            ColoredTeam(team.name, team2teamColor[team]!!) to claims.count{ it.teamName == team.name}
        }.sortedBy { (_, value) -> -value }.toMap()
    }

    val history = historyRepository.history

    init {
        viewModelScope.launch {
            historyRepository.listenForUpdates()
        }
    }


}