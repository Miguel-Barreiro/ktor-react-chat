package lindar.server.gameLogic.database

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet


private const val GAME_DATA_SQL ="CALL GetGameData(?)"



private const val dbName = "dfg"
private const val dbUserName = "dfgUser"
private const val dbPassword = "dxwvwxjZqtm9gyUm"

//private const val hostName = "node92002-dfgserver.mircloud.host"
private const val hostName = "localhost"


data class DatabaseConnection(internal val connection: Connection){
    internal val GameDataStatement = connection.prepareStatement(GAME_DATA_SQL)

}

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



fun GetGameData(playerName: String, databaseConnection: DatabaseConnection): ResultSet? {
    databaseConnection.GameDataStatement.setString(1,playerName)
    return databaseConnection.GameDataStatement.executeQuery()
}