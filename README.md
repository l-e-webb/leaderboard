# Leaderboard

_Leaderboard_ is a demo project I made to learn the basics of server programming with [Ktor](https://ktor.io/), a Kotlin framework for web applications. In this case, the server provides a (very) basic web API for maintaining the leaderboard of on online game, providing methods to create, update, and retrieve score data for users.

## Running locally

Once cloned, execute `./gradle run` in the root directory (just `gradlew run` on Windows). Gradle should take care of the rest and the server will be accessible at `localhost:8000`. 

Notes:
* You can change the port from `resources/application.conf`.
* You will need a Java 8 JDK installed.
* If you're familiar with running Kotlin projects from IntelliJ, you can also run the `main` function in `Application.kt`.

## API

Once running, the server supports the following actions. (Note that the current implementation only keeps data in memory, so the user database will be empty each time the server is run.)

#### `GET: /user/{userId}`

Returns a `User` JSON for the user with the associated ID. `userId` must be a valid `Long` value.

#### `POST: /user/{userId}/update`

Update the name and/or score associated with the user of the given ID. Returns a `User` JSON for the updated user.

Parameters (at least one must be present):
* `score` (optional): New score to set for the given user. Must be a valid `Long`. Can be lower than the user's current high score.
* `name` (optional): New user name for the user.

Example: `/user/1245/update?name=Grace&score=1000000`

#### `POST: /user/{userId}/newScore`

Updates the high score for a given user if the new score is higher than their previous score. Returns the `User` JSON.

Parameters:
* `score` (required): new score for the user. Must be a valid `Long`. Only updates the user's high score if the new score is higher.

Example: `/user/12345/newScore?score=1500000`

#### `POST: /newUser`

Creates a new user with a specified name and a high score of 0. Returns the new user JSON (including their newly assigned ID).

Parameters:
* `name` (required): name for the new user.

Example: `/newUser?name=Grace`

#### `GET: /topScores`

Gets a list of the highest scoring users, in descending order by score.

Parameters:
* `count` (optional): number of top scorers to receive. If omitted, the top 10 are provided.

### Error handling

The server only responds with statuses 200, 404, and 500. If a request is recognized by the server but cannot be completed (for example, a user ID is not a valid `Long` or an update request is posted with neither parameter provided), the server will respond with status 200 and a message describing why the request could not be completed. URI's that do not conform to the routes above will result in 404, and any exception during execution will give a 500.

### User JSON

`User` objects have the following format:
```$xslt
{
    "id": [Long],
    "name": [String],
    "highScore": [Long]
}
```

Most routes respond with a single `User` object. The `topScores` route gives a list of these objects.