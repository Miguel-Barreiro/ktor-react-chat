package lindar.server

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import lindar.server.connection.*
import lindar.server.gameLogic.database.*
import java.sql.SQLException
import java.util.*
import kotlin.collections.LinkedHashSet

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    install(WebSockets)

    //TODO: we need to make this connection thread safe ( maybe even a pool of connections)
    var databaseConnection = CreateDatabaseConnection()
    println("Database connected successfully")
    
    routing {

        val connections = Collections.synchronizedSet<ConnectionController>(LinkedHashSet())

        webSocket("/DFG") {
            println("New player connected")
            
            val connection = ConnectionController(this)
            connections += connection
            
            try {

                for(frame in incoming) {
                    frame as? Frame.Text ?: continue
                    val receivedText = frame.readText()
                    
                    println("Received Text: $receivedText")
                    
                    connection.ProcessMessage(receivedText, databaseConnection)
                }

            }catch ( e: SQLException) {
                CloseDatabaseConnection(databaseConnection)
                CreateDatabaseConnection().also { databaseConnection = it }
                
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
}
