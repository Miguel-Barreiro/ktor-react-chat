package lindar.server.gameLogic

import io.ktor.util.*
import lindar.server.connection.*
import lindar.server.connection.ConnectionController
import lindar.server.gameLogic.database.AddSafePick
import lindar.server.gameLogic.database.DatabaseConnection
import lindar.server.gameLogic.database.GetGameData
import java.lang.Exception
import java.sql.Date

class NotLogedException : Exception( "User had to be logged in to perform this action")

private const val TRIES_PER_DAY = 6


public fun HandleReceivedMessage(login: LoginBody, connection: ConnectionController, databaseConnection: DatabaseConnection){

    connection.PlayerId = login.PlayerId

    val (startingDate, safePicks) = GetGameData(login.PlayerId, databaseConnection)

    val message = CreateStartGameDataMessage(startingDate, safePicks)
    connection.SendMessage(message)
}


public fun HandleReceivedMessage(message: PickSafeBody, connection: ConnectionController, databaseConnection: DatabaseConnection){
    if(connection.PlayerId.isNullOrEmpty()) {
        throw NotLogedException() 
    }
    val (resultCode,  picksLeft, safePick) = AddSafePick(message.SafeId, connection.PlayerId.orEmpty(), databaseConnection)

    when(resultCode){
        0-> {
            /* picking sucessful*/
            var resultMessage = PickSafeResponseMessage(
                PickSafeResponseBody(
                    listOf( safePick),
                    picksLeft,
                )
            )
            connection.SendMessage(resultMessage)
        }
        else -> {
            /* 1: has picked the same one before*/
            /* 2: has reached max picks*/

            connection.SendMessage(
                PickSafeFailMessage(PickSafeFailBody(resultCode))
            )
        }
    }
}


private fun CreateStartGameDataMessage( startingDate: Date, safePicks: List<SafePick> ): GameDataMessage {
    
    val rewards = object {
        var CROWN = 0
        var EGG = 0
        var JEWEL = 0
        var AMULET = 0
        var DAGGER = 0
        var PAINTING = 0
    }
    val dayPicks = arrayOf(0, 0, 0, 0, 0, 0, 0)

    val now = System.currentTimeMillis()
    val dayOfYear = Date(now).toLocalDate().dayOfYear
    val startGameDay = startingDate.toLocalDate().dayOfYear
    val currentDayNumber = dayOfYear - startGameDay

    safePicks.forEach {
        when(it.Inside){
            "CROWN" -> rewards.CROWN++
            "EGG" -> rewards.EGG++
            "JEWEL" -> rewards.JEWEL++
            "DAGGER" -> rewards.DAGGER++
            "AMULET" -> rewards.AMULET++
            "PAINTING" -> rewards.PAINTING++
        }
       if(it.PickDate!= null) { 
           val pickDate : Date = it.PickDate as Date
           dayPicks[ pickDate.toLocalDate().dayOfYear - startGameDay]++
       }
    }

    //MISSED, PLAYED, CURRENT, FUTURE
    fun getDayResult(numberDay : Int, currentDayNumber : Int, dayPicks : Array<Int>) : String{
        return when {
            currentDayNumber == numberDay -> "CURRENT"
            currentDayNumber > numberDay -> if(dayPicks[numberDay] > 0)  "PLAYED" else "MISSED"
            else -> "FUTURE";
            
        }
    }
    
    val dayResults: Array<String> = arrayOf("", "", "", "", "", "", "")
    for (i in 0..6)
        dayResults[i] = getDayResult(i, currentDayNumber, dayPicks)
    
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
    
    return GameDataMessage(
        BODY = GameDataBody(
            days = Days(
                dayResults[0],
                dayResults[1],
                dayResults[2],
                dayResults[3],
                dayResults[4],
                dayResults[5],
                dayResults[6],
            ),
            triesPerDay = TRIES_PER_DAY,
            currentTriesLeft = TRIES_PER_DAY - dayPicks[currentDayNumber],
            prizes = prizes,
            safePicks = safePicks,
        )
    )


}

