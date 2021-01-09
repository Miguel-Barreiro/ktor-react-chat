package lindar.server.gameLogic

import lindar.server.connection.*
import lindar.server.connection.ConnectionController
import lindar.server.gameLogic.database.DatabaseConnection
import lindar.server.gameLogic.database.GetGameData
import java.sql.ResultSet


private const val TRIES_PER_DAY = 6


public fun HandleReceivedMessage(login: LoginMessage, connection: ConnectionController, databaseConnection: DatabaseConnection){

    val gameData = GetGameData(login.BODY.PlayerId, databaseConnection)

    val message = CreateStartGameDataMessage(gameData)
    connection.SendMessage(message)
}


public fun HandleReceivedMessage(message: PickSafeMessage, connection: ConnectionController, databaseConnection: DatabaseConnection){

//    val gameData = GetGameData(login.name, databaseConnection)
//
//    val message = CreateStartGameDataMessage(gameData)
//    connection.SendMessage(message)
//
//    send("You said: $receivedText")

}


fun CreateStartGameDataMessage(resultSet: ResultSet?): GameDataMessage {

    //MISSED, PLAYED, CURRENT, FUTURE
    fun getDayResult(numberDay : Int, currentDayNumber : Int, dayPicks : Array<Int>) : String{
        return when {
            currentDayNumber == numberDay -> "CURRENT"
            currentDayNumber > numberDay -> if(dayPicks[numberDay] > 0)  "PLAYED" else "MISSED"
            else -> "FUTURE";
            
        }
    }
    
    val rewards = object {
        var CROWN = 0
        var EGG = 0
        var JEWEL = 0
        var AMULET = 0
        var DAGGER = 0
        var PAINTING = 0
    }

    val dayPicks = arrayOf(0, 0, 0, 0, 0, 0, 0)
    val currentDayNumber = 0

    val dayResults: Array<String> = arrayOf("", "", "", "", "", "", "")
    for (i in 0..6)
        dayResults[i] = getDayResult(i, currentDayNumber, dayPicks)


    val days = Days(
        dayResults[0],
        dayResults[1],
        dayResults[2],
        dayResults[3],
        dayResults[4],
        dayResults[5],
        dayResults[6],
    )

    val prizes: List<Prize> = listOf(
        Prize(
            objectType = "CROWN",
            currentNumber = rewards.CROWN,
            target = 9,
            reward = Reward(
                "Money",
                500
            )
        ),
        Prize(
            objectType = "EGG",
            currentNumber = rewards.EGG,
            target = 5,
            reward = Reward(
                "Money",
                200
            )
        ),
        Prize(
            objectType = "JEWEL",
            currentNumber = rewards.JEWEL,
            target = 5,
            reward = Reward(
                "FREE_SPINS",
                5
            )
        ),
        Prize(
            objectType = "AMULET",
            currentNumber = rewards.AMULET,
            target = 5,
            reward = Reward(
                "Money",
                300
            )
        ),
        Prize(
            objectType = "DAGGER",
            currentNumber = rewards.DAGGER,
            target = 5,
            reward = Reward(
                "Money",
                150
            )
        ),
        Prize(
            objectType = "PAINTING",
            currentNumber = rewards.PAINTING,
            target = 5,
            reward = Reward(
                "FREE_SPINS",
                5
            )
        ),
    )
    val safePicks: List<SafePick> = emptyList()




    if (resultSet != null) {
        while (resultSet.next()) {
//            val result = resultSet.getString("result")
            println(resultSet.getArray(1))
        }
    }
    
    
    
    
    return GameDataMessage(
        BODY = GameDataBody(
            days = days,
            triesPerDay = TRIES_PER_DAY,
            currentTriesLeft = TRIES_PER_DAY - dayPicks[currentDayNumber],
            prizes = prizes,
            safePicks = safePicks,
        )
    )






}


