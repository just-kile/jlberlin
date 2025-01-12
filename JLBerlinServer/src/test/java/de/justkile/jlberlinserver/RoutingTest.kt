package de.justkile.jlberlinserver

import de.justkile.jlberlinmodel.DistrictClaim
import de.justkile.jlberlinmodel.Team
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.testing.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import kotlin.test.Test



class RoutingTest {


    @Test
    fun testGetTeams_singleTeamCreated_returnsTeamWithSameName() = testApplication {
        val client = createClient()

        val teamName = "BestTeamEver"

        client.post("/teams") {
            contentType(ContentType.Application.Json)
            setBody(Team(teamName))
        }

        val response = client.get("/teams")

        val teams = response.body<List<Team>>()
        assertThat(response.status, equalTo(HttpStatusCode.OK))
        assertThat(teams.size, equalTo(1))
        assertThat(teams.get(0).name, equalTo(teamName))
    }

    @Test
    fun getClaims_singleClaimCreated_returnsSameClaim() = testApplication {
        val client = createClient()

        val districtClaim = DistrictClaim(
            districtName = "BestDistrictEver",
            claimTimeInSeconds = 120,
            teamName = "BestTeamEver"
        )

        client.post("/claims") {
            contentType(ContentType.Application.Json)
            setBody(districtClaim.copy())
        }

        val response = client.get("/claims")

        val claims = response.body<List<DistrictClaim>>()
        assertThat(response.status, equalTo(HttpStatusCode.OK))
        assertThat(claims.size, equalTo(1))
        assertThat(claims.get(0), equalTo(districtClaim))
    }

    @Test
    fun postClaim_districtClaimedWithSmallerTimeAlready_newClaimIsSaved() = testApplication {
        val client = createClient()

        val oldDistrictClaim = DistrictClaim(
            districtName = "BestDistrictEver",
            claimTimeInSeconds = 120,
            teamName = "BestTeamEver"
        )

        val newDistrictClaim = oldDistrictClaim.copy(
            claimTimeInSeconds = 180,
            teamName = "EvenBetterTeam"
        )

        client.post("/claims") {
            contentType(ContentType.Application.Json)
            setBody(oldDistrictClaim.copy())
        }

        val postResponse = client.post("/claims") {
            contentType(ContentType.Application.Json)
            setBody(newDistrictClaim.copy())
        }

        val response = client.get("/claims")

        val claims = response.body<List<DistrictClaim>>()
        assertThat(postResponse.status, equalTo(HttpStatusCode.OK))
        assertThat(claims.size, equalTo(1))
        assertThat(claims.get(0), equalTo(newDistrictClaim))
    }

    @Test
    fun postClaim_districtClaimedWithBiggerTimeAlready_newClaimIsNotSaved() = testApplication {
        val client = createClient()

        val oldDistrictClaim = DistrictClaim(
            districtName = "BestDistrictEver",
            claimTimeInSeconds = 180,
            teamName = "BestTeamEver"
        )

        val newDistrictClaim = oldDistrictClaim.copy(
            claimTimeInSeconds = 120,
            teamName = "EvenBetterTeam"
        )

        client.post("/claims") {
            contentType(ContentType.Application.Json)
            setBody(oldDistrictClaim.copy())
        }

        val postResponse = client.post("/claims") {
            contentType(ContentType.Application.Json)
            setBody(newDistrictClaim.copy())
        }

        val response = client.get("/claims")

        val claims = response.body<List<DistrictClaim>>()
        assertThat(postResponse.status, equalTo(HttpStatusCode.BadRequest))
        assertThat(claims.size, equalTo(1))
        assertThat(claims.get(0), equalTo(oldDistrictClaim))
    }

    private fun ApplicationTestBuilder.createClient(): HttpClient {
        application {
            module()
        }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        return client
    }
}