package com.tangledwebgames.repo

object RepositoryProvider {
    var isTest: Boolean = false

    val scoreRepository: ScoreRepository
        get() = if (isTest) testRepository else mainRepository

    private val testRepository: ScoreRepository by lazy {
        SimpleScoreRepository()
    }

    // TODO: update to use SQL database or similar
    private val mainRepository: ScoreRepository by lazy {
        SimpleScoreRepository()
    }
}