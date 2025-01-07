package de.justkile.jlberlin.repository

import android.util.Log
import androidx.lifecycle.viewModelScope
import de.justkile.jlberlin.BackendClient
import de.justkile.jlberlin.datasource.TeamRemoteDataSource
import de.justkile.jlberlinmodel.Team
import kotlinx.coroutines.launch

class TeamRepository (
    private val teamRemoteDataSource: TeamRemoteDataSource
){

    suspend fun getTeams() = teamRemoteDataSource.getTeams()

    suspend fun createNewTeam(teamName: String) = teamRemoteDataSource.createNewTeam(Team(teamName))

}