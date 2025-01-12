package de.justkile.jlberlinserver

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

class RoutingTest {

    @Test
    fun testTeams() = testApplication {
        application {
            module()
        }
        val response = client.get("/teams")

        assertThat(HttpStatusCode.OK, equalTo(response.status))
        assertThat("[]", equalTo(response.bodyAsText()))
    }
}