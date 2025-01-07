package de.justkile.jlberlin.datasource

import android.util.Log
import de.justkile.jlberlin.BackendClient
import de.justkile.jlberlin.viewmodel.ClaimState
import de.justkile.jlberlinmodel.DistrictClaim
import de.justkile.jlberlinmodel.Team
import io.ktor.client.call.body
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private const val CLAIMS_ENDPOINT = "${BackendClient.BASE_URL}/claims"

class ClaimRemoteDataSource {

    private val client = BackendClient.client

    suspend fun getDistrictClaims(): List<DistrictClaim> = client.get(CLAIMS_ENDPOINT).body()

    suspend fun createOrUpdate(claim: DistrictClaim) = client.post(CLAIMS_ENDPOINT) {
        setBody(claim)
    }

    suspend fun listenForNewClaims(onNewClaim: (List<DistrictClaim>) -> Unit) {

        while (true) {
            try {
                client.sse(
                    host = BackendClient.HOST,
                    port = BackendClient.PORT,
                    path = "/events"
                ) {
                    while (true) {
                        incoming.collect {
                            Log.i("ClaimRemoteDataSource", "Event: ${it.event}")
                            if (it.event == "new claim") {
                                onNewClaim(getDistrictClaims())
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ClaimRemoteDataSource", "Error while listening for new claims", e)
            }
            delay(1000)
        }
    }


}