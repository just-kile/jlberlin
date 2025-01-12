package de.justkile.jlberlinserver

import de.justkile.jlberlinmodel.DistrictClaim
import de.justkile.jlberlinmodel.Team
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.sse
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop

fun Application.configureRouting() {

    val teams = emptyList<Team>().toMutableList()
    val districtName2claim = mutableMapOf<String, DistrictClaim>()

    val newClaim = MutableStateFlow<DistrictClaim?>(null)

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        get("/teams") {
            call.respond(teams)
        }

        post("/teams") {
            val team = call.receive<Team>()
            teams.add(team)

            call.response.status(HttpStatusCode.OK)
        }

        post("/claims") {
            log.info("/claims: Posted new claim.")

            val claim = call.receive<DistrictClaim>()
            newClaim.value = claim

            if (claim.teamName == null) {
                districtName2claim.remove(claim.districtName)
                call.response.status(HttpStatusCode.OK)
            } else {
                val currentClaim = districtName2claim[claim.districtName]
                if (currentClaim != null && currentClaim.claimTimeInSeconds >= claim.claimTimeInSeconds) {
                    call.response.status(HttpStatusCode.BadRequest)
                } else {
                    districtName2claim[claim.districtName] = claim
                    call.response.status(HttpStatusCode.OK)
                }
            }
        }

        // curl -i -X POST -H 'Content-Type: application/json' -d '{"districtName": "Hellersdorf", "teamName": "foo", "claimTimeInSeconds":"60"}' http://192.168.0.87:8080/claims
        get("/claims") {
            call.respond(districtName2claim.values)
        }

        sse("/events") {

            send(ServerSentEvent(event = "connection opened"))

            newClaim.drop(1).collect { claim ->
                send(ServerSentEvent(event ="new claim"))
            }

        }
    }
}