package com.tangledwebgames.repo

/**
 * An interface for a database which holds user & score data.
 */
interface ScoreRepository {

    fun addUser(name: String): User

    fun getUser(userId: Long): User?

    fun hasUser(userId: Long): Boolean

    fun getName(userId: Long): String?

    fun setName(userId: Long, name: String)

    fun getScore(userId: Long): Long?

    fun setScore(userId: Long, score: Long)

    fun newScore(userId: Long, score: Long) {
        getScore(userId)?.let {
            if (it < score) setScore(userId, score)
        }
    }

    fun getTopScores(numScores: Int = 10): List<User>
}