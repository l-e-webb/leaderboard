package com.tangledwebgames

import com.tangledwebgames.repo.RepositoryProvider
import com.tangledwebgames.repo.User
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonException
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ApplicationTest {
    companion object {
        private const val USER_NAME_0 = "Louis"
        private const val USER_NAME_1 = "Ashlen"
        private const val USER_NAME_2 = "Heather"
        private val USER_0 = User(0, USER_NAME_0, 0)
        private val USER_1 = User(1, USER_NAME_1, 0)
        private val USER_2 = User(2, USER_NAME_2, 0)
    }

    private val repo
        get() = RepositoryProvider.scoreRepository

    @After
    fun cleanup() {
        runBlocking {
            repo.clear()
        }
    }

    @Test
    fun getUser_single() = applicationTest {
        givenUsersAdded(USER_NAME_0)
        getValidUser(USER_0)
    }

    @Test
    fun getUser_multiple() = applicationTest {
        givenUsersAdded(USER_NAME_0, USER_NAME_1, USER_NAME_2)
        getValidUser(USER_0)
        getValidUser(USER_1)
        getValidUser(USER_2)
    }

    @Test
    fun getUser_nonexistent() = applicationTest {
        givenUsersAdded(USER_NAME_0)
        checkNoUsers(1)
        getNonexistentUser(1)
    }

    @Test
    fun getUser_invalidId() = applicationTest {
        givenUsersAdded(USER_NAME_0)
        getUserInvalidId(USER_NAME_0)
    }

    @Test
    fun newUser_single() = applicationTest {
        checkNoUsers(0)
        newUser(USER_NAME_0)
        checkUser(USER_0)
    }

    @Test
    fun newUser_multiple() = applicationTest {
        checkNoUsers(0, 1, 2)
        newUser(USER_NAME_0)
        newUser(USER_NAME_1)
        newUser(USER_NAME_2)
        checkUsers(USER_0, USER_1, USER_2)
        checkNoUsers(3)
    }

    @Test
    fun newUser_noName() = applicationTest {
        newUserNoName()
    }

    @Test
    fun updateUser_single_nameOnly() = applicationTest {
        givenUsersAdded(USER_NAME_0)
        updateValidUser(userId = 0, name = USER_NAME_1)
        checkUserName(0, USER_NAME_1)
    }

    @Test
    fun updateUser_single_scoreOnly() = applicationTest {
        givenUsersAdded(USER_NAME_0)
        updateValidUser(userId = 0, score = 5)
        checkUserScore(0, 5)
    }

    @Test
    fun updateUser_since_nameAndScore() = applicationTest {
        givenUsersAdded(USER_NAME_0)
        updateValidUser(userId = 0, name = USER_NAME_1, score = 5)
        checkUser(User(0, USER_NAME_1, 5))
    }

    @Test
    fun updateUser_multiple_nameAndScore() = applicationTest {
        givenUsersAdded(USER_NAME_0, USER_NAME_1, USER_NAME_2)
        updateValidUser(0, USER_NAME_1, 5)
        updateValidUser(1, USER_NAME_2, 6)
        updateValidUser(2, USER_NAME_0, 7)
        checkUsers(
            User(0, USER_NAME_1, 5),
            User(1, USER_NAME_2, 6),
            User(2, USER_NAME_0, 7)
        )
    }

    @Test
    fun updateUser_nonexistent() = applicationTest {
        givenUsersAdded(USER_NAME_0)
        updateNonExistentUser(1, name = "Any")
    }

    @Test
    fun updateUser_noParams() = applicationTest {
        givenUsersAdded(USER_NAME_0)
        updateUserNoParams(0)
    }

    @Test
    fun updateUser_invalidScore() = applicationTest {
        givenUsersAdded(USER_NAME_0)
        updateUserInvalidScore(0, name = USER_NAME_1, score="Twenty")
    }

    @Test
    fun updateUser_invalidId() = applicationTest {
        givenUsersAdded(USER_NAME_0)
        updateUserInvalidUserId(userId = USER_NAME_0, name = USER_NAME_1, score = 5)
    }

    private fun applicationTest(testBlock: TestApplicationEngine.() -> Unit) =
        withTestApplication({ module(testing = true) }, testBlock)

    private fun TestApplicationCall.assertStatus(code: HttpStatusCode) {
        assertEquals(code, response.status())
    }

    private fun TestApplicationEngine.getUser(userId: String, testBlock: TestApplicationCall.() -> Unit) {
        handleRequest(HttpMethod.Get, uri="/user/$userId").apply(testBlock)
    }

    private fun TestApplicationEngine.getValidUser(expectedUser: User) {
        getUser(expectedUser.id.toString()) {
            assertStatus(HttpStatusCode.OK)
            assertEquals(
                expectedUser,
                Json.decodeFromString<User>(response.content ?: "{}")
            )
        }
    }

    private fun TestApplicationEngine.getNonexistentUser(userId: Long) {
        getUser(userId.toString()) {
            userNotFound(userId.toString())
        }
    }

    private fun TestApplicationEngine.getUserInvalidId(userId: String) {
        getUser(userId) {
            invalidUserId()
        }
    }

    private fun TestApplicationEngine.newUser(name: String) {
        handleRequest(HttpMethod.Post, uri="/newUser?name=$name").apply {
            assertStatus(HttpStatusCode.OK)
            try {
                val newUser = Json.decodeFromString<User>(response.content ?: "{}")
                assertEquals(name, newUser.name)
                assertEquals(0, newUser.highScore)
            } catch (e: JsonException) {
                Assert.fail(e.message)
            }
        }
    }

    private fun TestApplicationEngine.newUserNoName() {
        handleRequest(HttpMethod.Post, uri="/newUser").apply {
            assertFalse(requestHandled)
        }
    }

    private fun TestApplicationEngine.updateUser(
        userId: String,
        name: String? = null,
        score: String? = null,
        testBlock: TestApplicationCall.() -> Unit
    ) {
        val uri = "/user/$userId/update" + if (name != null && score != null) {
            "?name=$name&score=$score"
        } else if (name != null) {
            "?name=$name"
        } else if (score != null) {
            "?score=$score"
        } else {
            ""
        }
        handleRequest(HttpMethod.Post, uri).apply(testBlock)
    }

    private fun TestApplicationEngine.updateValidUser(userId: Long, name: String? = null, score: Long? = null) {
        if (name == null && score == null) {
            throw(IllegalArgumentException("For valid update user test, at least one of name and score must be present."))
        }
        updateUser(userId.toString(), name, score?.toString()) {
            assertStatus(HttpStatusCode.OK)
            try {
                val responseUser = Json.decodeFromString<User>(response.content ?: "{}")
                assertEquals(userId, responseUser.id)
                name?.let {
                    assertEquals(it, responseUser.name)
                }
                score?.let {
                    assertEquals(it, responseUser.highScore)
                }
            } catch (e: JsonException) {
                Assert.fail(e.message)
            }
        }
    }

    private fun TestApplicationEngine.updateNonExistentUser(userId: Long, name: String? = null, score: Long? = null) {
        updateUser(userId.toString(), name, score?.toString()) {
            userNotFound(userId.toString())
        }
    }

    private fun TestApplicationEngine.updateUserNoParams(userId: Long) {
        updateUser(userId.toString()) {
            invalidUpdateQueryParameters()
        }
    }

    private fun TestApplicationEngine.updateUserInvalidScore(userId: Long, name: String? = null, score: String) {
        updateUser(userId.toString(), name, score) {
            invalidUpdateQueryParameters()
        }
    }

    private fun TestApplicationEngine.updateUserInvalidUserId(userId: String, name: String? = null, score: Long? = null) {
        updateUser(userId, name, score?.toString()) {
            assertStatus(HttpStatusCode.OK)
            assertEquals("userId must be a valid long", response.content)
        }
    }

    private fun TestApplicationCall.userNotFound(userId: String) {
        assertStatus(HttpStatusCode.OK)
        assertEquals("No user found with ID $userId", response.content)
    }

    private fun TestApplicationCall.invalidUserId() {
        assertStatus(HttpStatusCode.OK)
        assertEquals("userId must be a valid long", response.content)
    }

    private fun TestApplicationCall.invalidUpdateQueryParameters() {
        assertStatus(HttpStatusCode.OK)
        assertEquals(
            "At least one of query parameters name and score must be provided. score must be a valid long",
            response.content
        )
    }

    private fun givenUsersAdded(vararg names: String) {
        runBlocking {
            names.forEach {
                repo.addUser(it)
            }
        }
    }

    private fun checkUser(user: User) {
        assertEquals(user, repo.getUser(user.id))
    }

    private fun checkUsers(vararg users: User) {
        users.forEach {
            checkUser(it)
        }
    }

    private fun checkUserName(userId: Long, name: String) {
        assertEquals(name, repo.getName(userId))
    }

    private fun checkUserNames(vararg users: Pair<Long, String>) {
        users.forEach {
            checkUserName(it.first, it.second)
        }
    }

    private fun checkUserScore(userId: Long, score: Long) {
        assertEquals(score, repo.getScore(userId))
    }

    private fun checkUserScores(vararg users: Pair<Long, Long>) {
        users.forEach {
            checkUserScore(it.first, it.second)
        }
    }

    private fun checkNoUsers(vararg userIds: Long) {
        userIds.forEach {
            assertFalse(repo.hasUser(it))
        }
    }

}
