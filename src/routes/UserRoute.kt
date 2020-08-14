package com.tangledwebgames.routes

import com.tangledwebgames.repo.RepositoryProvider
import com.tangledwebgames.routes.Errors.missingParameter
import com.tangledwebgames.routes.Errors.userNotFound
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond

internal object UserRoute {

    private val repo = RepositoryProvider.scoreRepository

    val getUser: RequestResolver = {
        call.request.queryParameters["id"]?.toLongOrNull()?.let { id ->
            repo.getUser(id)?.let {
                call.respond(HttpStatusCode.OK, it)
            } ?: userNotFound(id)
        } ?: missingParameter("id")
    }

    val newUser: RequestResolver = {
        call.receiveParameters()["name"]?.let { newUserName ->
            call.respond(HttpStatusCode.OK, repo.addUser(newUserName))
        } ?: missingParameter("name")
    }

    val updateUser: RequestResolver = {
        val params = call.receiveParameters()
        params["id"]?.toLongOrNull()?.let { id ->
            if (!repo.hasUser(id)) {
                userNotFound(id)
                return@let
            }
            params["name"]?.let { name ->
                repo.setName(id, name)
            }
            params["score"]?.toLongOrNull()?.let { score ->
                repo.setScore(id, score)
            }
            repo.getUser(id)?.let {
                call.respond(HttpStatusCode.OK, it)
            }
        }
    }

}