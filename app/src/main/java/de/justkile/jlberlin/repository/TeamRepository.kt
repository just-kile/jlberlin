package de.justkile.jlberlin.repository

import android.util.Log
import de.justkile.jlberlin.datasource.TeamRemoteDataSource
import de.justkile.jlberlinmodel.Team
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TeamRepository (
    private val teamRemoteDataSource: TeamRemoteDataSource
){

    private var _teams = MutableStateFlow(emptyList<Team>())
    val teams = _teams.asStateFlow()

    suspend fun getTeams() = teamRemoteDataSource.getTeams()

    fun createNewTeam(teamName: String) = teamRemoteDataSource.createNewTeam(Team(teamName))

    fun listenForNewTeams() {
        teamRemoteDataSource.addListener(
            onTeamsChanged = {
                Log.i("TeamRepository", "New teams: $it")
                _teams.value = it
                             },
            onError = { Log.e("TeamRepository", "Error while listening for new teams", it) }
        )
    }

}