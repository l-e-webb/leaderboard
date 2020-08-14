package com.tangledwebgames.routes

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText

internal object Errors {

    suspend fun Context.userNotFound(id: Long? = null) {
        call.respondText(status = HttpStatusCode.OK) {
            id?.let {
                "No user found with ID $it"
            } ?: "No user found"
        }
    }

    suspend fun Context.missingParameter(missingParam: String? = null) {
        call.respondText(status = HttpStatusCode.OK) {
            missingParam?.let {
                "Missing required parameter \'$it\'"
            } ?: "Missing required parameter(s)"
        }
    }
}