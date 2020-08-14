package com.tangledwebgames.routes

import com.tangledwebgames.routes.ScoreRoute.getTopScorers
import com.tangledwebgames.routes.ScoreRoute.getUserScore
import com.tangledwebgames.routes.ScoreRoute.postUserScore
import com.tangledwebgames.routes.UserRoute.getUser
import com.tangledwebgames.routes.UserRoute.newUser
import com.tangledwebgames.routes.UserRoute.updateUser
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.routing.*
import io.ktor.util.pipeline.PipelineContext

typealias Context = PipelineContext<Unit, ApplicationCall>
typealias RequestResolver = suspend Context.() -> Unit

fun Routing.routingMain() {
    route("/user") {
        get {
            getUser()
        }
        accept(ContentType.Application.FormUrlEncoded) {
            post {
                newUser()
            }
            post("/update") {
                updateUser()
            }
        }
    }
    route("/score") {
        route("/user") {
            get {
                getUserScore()
            }
            post {
                postUserScore()
            }
        }
        get("/top") {
            getTopScorers()
        }
    }
}