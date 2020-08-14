package com.tangledwebgames.routes

import com.tangledwebgames.repo.RepositoryProvider
import com.tangledwebgames.routes.Errors.missingParameter
import com.tangledwebgames.routes.Errors.userNotFound
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond

internal object ScoreRoute {

    private val repo = RepositoryProvider.scoreRepository

    val getUserScore: RequestResolver = {
        call.request.queryParameters["id"]?.toLongOrNull()?.let { id ->
            repo.getUser(id)?.let {
                call.respond(HttpStatusCode.OK, it.highScore)
            } ?: userNotFound(id)
        } ?: missingParameter("id")
    }

    val postUserScore: RequestResolver = {
        val params = call.receiveParameters()
        params["id"]?.toLongOrNull()?.let { id ->
            if (!repo.hasUser(id)) {
                userNotFound(id)
                return@let
            }
            params["score"]?.toLongOrNull()?.let { score ->
                repo.newScore(id, score)
                repo.getUser(id)?.let {
                    call.respond(HttpStatusCode.OK, it)
                }
            } ?: missingParameter("score")
        } ?: missingParameter("id")
    }

    val getTopScorers: RequestResolver = {
        val topScorers = call.request.queryParameters["count"]?.toIntOrNull()?.let {
            repo.getTopScores(it)
        } ?: repo.getTopScores()
        call.respond(HttpStatusCode.OK, topScorers)
    }

}