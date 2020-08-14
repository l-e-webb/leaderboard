package com.tangledwebgames.repo

class SimpleScoreRepository: ScoreRepository {

    private val userList: MutableList<User> = mutableListOf()

    override fun addUser(name: String): User {
        return User(
            id = userList.size.toLong(),
            name = name,
            highScore = 0L
        ).also {
            userList.add(it)
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

    override fun setName(userId: Long, name: String) {
        getUser(userId)?.let {
            userList[it.id.toInt()] = it.copy(name = name)
        }
    }

    override fun getScore(userId: Long): Long? {
        return getUser(userId)?.highScore
    }

    override fun setScore(userId: Long, score: Long) {
        getUser(userId)?.let {
            userList[it.id.toInt()] = it.copy(highScore = score)
        }
    }

    override fun getTopScores(numScores: Int): List<User> {
        return userList.sortedByDescending { it.highScore }
            .take(numScores)
    }
}