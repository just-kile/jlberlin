package de.justkile.jlberlin.datasource

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import de.justkile.jlberlinmodel.DistrictClaim
import kotlinx.coroutines.tasks.await


private const val COLLECTION_NAME = "claims"

class ClaimRemoteDataSource {

    suspend fun getDistrictClaims(): List<DistrictClaim> =
        Firebase.firestore
            .collection(COLLECTION_NAME)
            .get()
            .await()
            .toObjects(DistrictClaim::class.java)

    suspend fun createOrUpdate(claim: DistrictClaim) {

        val result = Firebase.firestore
            .collection(COLLECTION_NAME)
            .whereEqualTo("districtName", claim.districtName)
            .get()
            .await()
        if (result.isEmpty) {
            Firebase.firestore
                .collection(COLLECTION_NAME)
                .add(claim)
        } else {
            val docId = result.documents[0].id
            Firebase.firestore
                .collection(COLLECTION_NAME)
                .document(docId)
                .set(claim)
        }
    }


    fun addListener(
        onClaimsChanged: (List<DistrictClaim>) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        Firebase.firestore
            .collection(COLLECTION_NAME)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                value?.toObjects(DistrictClaim::class.java)?.let { onClaimsChanged(it) }
            }
    }

}