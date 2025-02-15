package de.justkile.jlberlin.datasource

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import de.justkile.jlberlin.BackendClient
import de.justkile.jlberlinmodel.Team
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.tasks.await

private const val TEAMS_ENDPOINT = "${BackendClient.BASE_URL}/teams"

class TeamRemoteDataSource {

    fun addListener(
        onTeamsChanged: (List<Team>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        Firebase.firestore
            .collection("teams")
            .addSnapshotListener { value, error ->
                Log.i("TeamRemoteDataSource", "Teams changed")
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                value?.toObjects(Team::class.java)?.let { onTeamsChanged(it) }
            }
    }

    suspend fun getTeams(): List<Team> =
        Firebase.firestore
            .collection("teams")
            .get()
            .await()
            .toObjects(Team::class.java)

    fun createNewTeam(team: Team) =
        Firebase.firestore
            .collection("teams")
            .add(team)


}