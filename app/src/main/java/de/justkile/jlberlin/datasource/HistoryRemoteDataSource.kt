package de.justkile.jlberlin.datasource

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import de.justkile.jlberlinmodel.Team
import kotlinx.coroutines.tasks.await

private const val COLLECTION_NAME = "histories"

data class HistoryData(
    val teamName: String = "",
    val districtName: String = "",
    val claimTimeInSeconds: Int = 0,
    val claimedAt: Long = 0
)

class HistoryRemoteDataSource {

    fun addListener(
        onHistoriesChanged: (List<HistoryData>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        Firebase.firestore
            .collection(COLLECTION_NAME)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                value?.toObjects(HistoryData::class.java)?.let { onHistoriesChanged(it) }
            }
    }

    suspend fun getHistory(): List<HistoryData> =
        Firebase.firestore
            .collection(COLLECTION_NAME)
            .get()
            .await()
            .toObjects(HistoryData::class.java)

    fun createNewHistoryEntry(entry: HistoryData) =
        Firebase.firestore
            .collection(COLLECTION_NAME)
            .add(entry)


}