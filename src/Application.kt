package com.tangledwebgames

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import com.fasterxml.jackson.databind.*
import com.tangledwebgames.repo.ScoreRepository
import com.tangledwebgames.repo.SimpleScoreRepository
import io.ktor.jackson.*
import io.ktor.features.*

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
        get("/") {
            call.respondText("OK")
        }
        route("/user") {
            get {
                call.request.queryParameters["id"]?.toLongOrNull()?.let { id ->
                    repo.getUser(id)?.let {
                        call.respond(it)
                    } ?: call.respondText("No user found with ID $id")
                } ?: call.respondText("Invalid user query")
            }
            accept(ContentType.Application.FormUrlEncoded) {
                post {
                    call.receiveParameters()["name"]?.let { newUserName ->
                        call.respond(repo.addUser(newUserName))
                    } ?: call.respondText("Invalid user post request")
                }
                post("/update") {
                    val params = call.receiveParameters()
                    params["id"]?.toLongOrNull()?.let { id ->
                        if (!repo.hasUser(id)) {
                            call.respondText("No user found with ID $id")
                            return@let
                        }
                        params["name"]?.let { name ->
                            repo.setName(id, name)
                        }
                        params["score"]?.toLongOrNull()?.let { score ->
                            repo.setScore(id, score)
                        }
                        repo.getUser(id)?.let { call.respond(it) }
                    }
                }
            }
        }
        route("/score") {
            route("/user") {
                get {
                    call.request.queryParameters["id"]?.toLongOrNull()?.let { id ->
                        repo.getUser(id)?.let {
                            call.respond(it.highScore)
                        } ?: call.respondText("No user found with ID $id")
                    } ?: call.respondText("Invalid user query")
                }
                post {
                    val params = call.receiveParameters()
                    params["id"]?.toLongOrNull()?.let { id ->
                        if (!repo.hasUser(id)) {
                            call.respondText("No user found with ID $id")
                            return@let
                        }
                        params["score"]?.toLongOrNull()?.let { score ->
                            repo.newScore(id, score)
                            repo.getUser(id)?.let { call.respond(it) }
                        } ?: call.respondText("No new score provided")
                    } ?: call.respondText("No user provided")
                }
            }
            get("/top") {
                val topScorers = call.request.queryParameters["count"]?.toIntOrNull()?.let {
                    repo.getTopScores(it)
                } ?: repo.getTopScores()
                call.respond(topScorers)
            }
        }
    }
}

