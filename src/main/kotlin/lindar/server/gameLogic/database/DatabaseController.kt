package lindar.server.gameLogic.database

import com.sun.org.apache.xpath.internal.operations.Bool
import lindar.server.connection.SafePick
import java.sql.*


private const val GAME_DATA_SQL ="CALL GetGameData(?)"
private const val SAFE_PICK_SQL ="CALL AddSafePick(?,?)"



private const val dbName = "dfg"
private const val dbUserName = "dfgUser"
private const val dbPassword = "dxwvwxjZqtm9gyUm"

//private const val hostName = "node92002-dfgserver.mircloud.host"
private const val hostName = "localhost"


data class DatabaseConnection(internal val connection: Connection)

fun CreateDatabaseConnection(): DatabaseConnection {

    val connectionString = "jdbc:mysql://" + hostName + ":3306/" + 
                            dbName + "?" +
                           "useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false&maxReconnects=10"

    val connection = DriverManager.getConnection(
        connectionString,
        dbUserName,
        dbPassword
    )
    return DatabaseConnection(connection) 
}

fun CloseDatabaseConnection(databaseConnection: DatabaseConnection) {
    try {
        databaseConnection.connection.close()
    }catch ( e : SQLException) {
        println("Connection closing error: " + e.message)
    }
}




fun GetGameData(playerId: String, databaseConnection: DatabaseConnection): Pair<Date,List<SafePick>>{
    val gameDataStatement = databaseConnection.connection.prepareStatement(GAME_DATA_SQL)
    gameDataStatement.setString(1,playerId)

    var startingDate = Date(1)   
    val safePicks: MutableList<SafePick> = mutableListOf()
        
    executePreparedStatement(gameDataStatement,
        firstSetCallback = {
            it.next()
            startingDate = it.getDate(1)
        },
        secondSetCallback = {
            while (it.next()) {
                val safeId = it.getInt("SafePick")
                val reward = it.getString("Reward")
                val pickDate = it.getDate("Day")
                safePicks += SafePick(
                    safeId,
                    reward,
                    pickDate,
                )
            }
        }
    )

    gameDataStatement.close()

    return Pair(startingDate, safePicks)
}

fun AddSafePick(safeId: Int, playerId: String, databaseConnection: DatabaseConnection) : Triple<Int, Int, SafePick>{
    val addSafePickStatement = databaseConnection.connection.prepareStatement(SAFE_PICK_SQL)
    addSafePickStatement.setString(1, playerId)
    addSafePickStatement.setInt(2, safeId)

    var errorCode : Int = 0
    var picksLeft : Int = 0
    var reward : String = ""
    var safeId : Int = 0
    
    executePreparedStatement(addSafePickStatement,
        firstSetCallback = {
            it.next()
            errorCode = it.getInt("errorCode")
        }, 
        secondSetCallback = {
            it.next()
            picksLeft = it.getInt("picksLeft")
            reward = it.getString("reward")
            safeId = it.getInt("safeId")
        }
    )

    addSafePickStatement.close()
    return Triple( errorCode, picksLeft, SafePick( safeId,reward))
}


private fun executePreparedStatement( preparedStatement: PreparedStatement, 
                                      firstSetCallback: (ResultSet)-> Unit, 
                                      secondSetCallback: ((ResultSet)-> Unit)?, ){
    
    fun executeStatementUtil(preparedStatement: PreparedStatement){
        if (preparedStatement.execute()) {
            firstSetCallback(preparedStatement.resultSet)
            if(preparedStatement.getMoreResults() && secondSetCallback != null) {
                secondSetCallback(preparedStatement.resultSet)
            }
        }
    }

    try {
        executeStatementUtil(preparedStatement)
    }catch ( e : SQLTransientException){
        executeStatementUtil(preparedStatement)
    }
}

