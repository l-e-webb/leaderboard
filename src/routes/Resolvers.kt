package com.tangledwebgames.routes

import com.tangledwebgames.repo.RepositoryProvider
import com.tangledwebgames.routes.Errors.invalidUserId
import com.tangledwebgames.routes.Errors.textError
import com.tangledwebgames.routes.Errors.userNotFound
import com.tangledwebgames.routes.ParamKeys.COUNT
import com.tangledwebgames.routes.ParamKeys.NAME
import com.tangledwebgames.routes.ParamKeys.SCORE
import com.tangledwebgames.routes.ParamKeys.USER_ID
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

internal object Resolvers {

    private val repo = RepositoryProvider.scoreRepository

    val getUser: RequestResolver = {
        call.parameters[USER_ID]?.toLongOrNull()?.let { id ->
            repo.getUser(id)?.let {
                call.respond(HttpStatusCode.OK, it)
            } ?: userNotFound(id)
        }
    }

    val newUser: RequestResolver = {
        call.parameters[NAME]?.let { newUserName ->
            call.respond(HttpStatusCode.OK, repo.addUser(newUserName))
        }
    }

    val updateUser: RequestResolver = {
        call.parameters[USER_ID]?.toLongOrNull()?.let { id ->
            if (!repo.hasUser(id)) {
                userNotFound(id)
                return@let
            }
            var hasParams = false
            call.request.queryParameters[NAME]?.let { name ->
                repo.setName(id, name)
                hasParams = true
            }
            call.request.queryParameters[SCORE]?.toLongOrNull()?.let { score ->
                repo.setScore(id, score)
                hasParams = true
            }
            if (hasParams) {
                repo.getUser(id)?.let {
                    call.respond(HttpStatusCode.OK, it)
                }
            } else {
                textError("At least one of query parameters $NAME and $SCORE must be provided. $SCORE must be a valid long.")
            }
        } ?: invalidUserId()
    }

    val postUserScore: RequestResolver = {
        call.parameters[USER_ID]?.toLongOrNull()?.let { userId ->
            call.parameters[SCORE]?.toLongOrNull()?.let { score ->
                if (repo.hasUser(userId)) {
                    repo.newScore(userId, score)
                    call.respond(HttpStatusCode.OK, requireNotNull(repo.getUser(userId)))
                } else {
                    userNotFound(userId)
                }
            } ?: textError("Must include query parameter $SCORE with valid long value")
        } ?: invalidUserId()
    }

    val getTopScorers: RequestResolver = {
        val topScorers = call.parameters[COUNT]?.toIntOrNull()?.let {
            repo.getTopScores(it)
        } ?: repo.getTopScores()
        call.respond(HttpStatusCode.OK, topScorers)
    }

}