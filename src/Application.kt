package com.tangledwebgames

import com.fasterxml.jackson.databind.SerializationFeature
import com.tangledwebgames.repo.ScoreRepository
import com.tangledwebgames.repo.SimpleScoreRepository
import com.tangledwebgames.routes.routingMain
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.routing.routing

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

val repo: ScoreRepository = SimpleScoreRepository()

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    routing {
        routingMain()
    }
}

