package de.justkile.jlberlin.datasource

import de.justkile.jlberlin.BackendClient
import de.justkile.jlberlinmodel.Team
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

private const val TEAMS_ENDPOINT = "${BackendClient.BASE_URL}/teams"

class TeamRemoteDataSource {

    private val client = BackendClient.client

    suspend fun getTeams(): List<Team> = client.get(TEAMS_ENDPOINT).body()

    suspend fun createNewTeam(team: Team) = client.post(TEAMS_ENDPOINT) {
        setBody(team)
    }

}