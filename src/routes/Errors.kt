package com.tangledwebgames.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*

internal object Errors {

    suspend fun Context.textError(text: String) {
        call.respondText(
            text = text,
            status = HttpStatusCode.OK
        )
    }

    suspend inline fun Context.textError(textProvider: () -> String) {
        textError(textProvider.invoke())
    }

    suspend fun Context.userNotFound(id: Long? = null) {
        textError {
            id?.let {
                "No user found with ID $it"
            } ?: "No user found"
        }
    }

    suspend fun Context.invalidUserId() {
        textError("${ParamKeys.USER_ID} must be a valid long")
    }
}