package com.tangledwebgames

import com.fasterxml.jackson.databind.SerializationFeature
import com.tangledwebgames.repo.RepositoryProvider
import com.tangledwebgames.routes.routingMain
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.response.*
import io.ktor.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    RepositoryProvider.isTest = testing
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
    install(StatusPages) {
        exception<Throwable> {
            call.respond(HttpStatusCode.InternalServerError)
        }

    }

    routing {
        routingMain()
    }
}

