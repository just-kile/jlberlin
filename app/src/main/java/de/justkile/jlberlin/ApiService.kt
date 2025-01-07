package de.justkile.jlberlin

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json


object BackendClient {
    const val HOST = "5.230.38.228"
    const val PORT = 8080
    const val BASE_URL = "http://$HOST:$PORT"

    val client = HttpClient(Android) {
        // important: Install SSE before ContentNegotiation (due to bug: https://youtrack.jetbrains.com/issue/KTOR-7631/SerializationException-Serializer-for-class-ClientSSESession-is-not-found-when-server-responds-with-JSON
        install(SSE) {
            showRetryEvents()
        }
        install(ContentNegotiation) {
            json()
        }
        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }
    }

}