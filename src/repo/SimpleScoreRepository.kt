package com.tangledwebgames.repo

/**
 * Basic implementation of [ScoreRepository] interface which holds an array of users in memory.
 * Does not save data to disk, not optimized for large amounts of data.
 */
class SimpleScoreRepository: ScoreRepository {

    private val userList: MutableList<User> = mutableListOf()

    override suspend fun addUser(name: String): User {
        return synchronized(userList) {
            User(
                id = userList.size.toLong(),
                name = name,
                highScore = 0L
            ).also {
                userList.add(it)
            }
        }
    }

    override fun getUser(userId: Long): User? {
        return userId.toInt()
            .takeIf { it in userList.indices }
            ?.let {
                userList[it]
            }
    }

    override fun hasUser(userId: Long): Boolean {
        return getUser(userId) != null
    }

    override fun getName(userId: Long): String? {
        return getUser(userId)?.name
    }

    override suspend fun setName(userId: Long, name: String) {
        synchronized(userList) {
            getUser(userId)?.let {
                userList[it.id.toInt()] = it.copy(name = name)
            }
        }
    }

    override fun getScore(userId: Long): Long? {
        return getUser(userId)?.highScore
    }

    override suspend fun setScore(userId: Long, score: Long) {
        synchronized(userList) {
            getUser(userId)?.let {
                userList[it.id.toInt()] = it.copy(highScore = score)
            }
        }
    }

    override fun getTopScores(numScores: Int): List<User> {
        return userList.sortedByDescending { it.highScore }
            .take(numScores)
    }

    override suspend fun clear() {
        synchronized(userList) {
            userList.clear()
        }
    }
}