package de.justkile.jlberlin.datasource

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import de.justkile.jlberlinmodel.Team
import kotlinx.coroutines.tasks.await

private const val COLLECTION_NAME = "teams"

class TeamRemoteDataSource {

    fun addListener(
        onTeamsChanged: (List<Team>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        Firebase.firestore
            .collection(COLLECTION_NAME)
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
            .collection(COLLECTION_NAME)
            .get()
            .await()
            .toObjects(Team::class.java)

    fun createNewTeam(team: Team) =
        Firebase.firestore
            .collection(COLLECTION_NAME)
            .add(team)


}