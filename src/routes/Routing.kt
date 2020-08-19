package com.tangledwebgames.routes

import com.tangledwebgames.routes.ParamKeys.COUNT
import com.tangledwebgames.routes.ParamKeys.NAME
import com.tangledwebgames.routes.ParamKeys.SCORE
import com.tangledwebgames.routes.ParamKeys.USER_ID
import com.tangledwebgames.routes.Resolvers.getTopScorers
import com.tangledwebgames.routes.Resolvers.getUser
import com.tangledwebgames.routes.Resolvers.newUser
import com.tangledwebgames.routes.Resolvers.postUserScore
import com.tangledwebgames.routes.Resolvers.updateUser
import io.ktor.routing.*

fun Routing.routingMain() {
    route("/user/{$USER_ID}") {
        get {
            getUser()
        }
        post("/update") {
            updateUser()
        }
        route("/newScore") {
            param(SCORE) {
                post {
                    postUserScore()
                }
            }
        }
    }
    route("/newUser") {
        param(NAME) {
            post {
                newUser()
            }
        }
    }
    route("/topScores") {
        optionalParam(COUNT) {
            get {
                getTopScorers()
            }
        }
    }
}