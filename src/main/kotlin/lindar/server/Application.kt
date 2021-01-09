package lindar.server

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import lindar.server.connection.*
import lindar.server.gameLogic.database.*
import java.util.*
import kotlin.collections.LinkedHashSet

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    install(WebSockets)

    val databaseConnection = CreateDatabaseConnection()
    println("Database connected successfully")
    
    routing {

        val connections = Collections.synchronizedSet<ConnectionController?>(LinkedHashSet())
        
        webSocket("/DFG") {
            send("You are connected!")
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

            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
}
