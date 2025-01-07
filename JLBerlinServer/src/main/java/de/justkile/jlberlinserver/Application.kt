package de.justkile.jlberlinserver

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.sse.SSE
import kotlinx.serialization.json.Json

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }

    install(SSE)

    configureRouting()
}