package lindar.server.connection

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import lindar.server.gameLogic.HandleReceivedMessage
import lindar.server.gameLogic.database.DatabaseConnection


open class BasicMessage(val Type:String)

//Server Messages
data class GameDataMessage( val BODY: GameDataBody) : BasicMessage("GAME_DATA")
data class GameDataBody(
    val days : Days,
    val triesPerDay : Int,
    val currentTriesLeft : Int,
    val prizes : List<Prize>,
    val safePicks : List<SafePick>
)

//client Messages
data class LoginMessage(val BODY: LoginBody) : BasicMessage("LOGIN")
data class LoginBody(val PlayerId:String)

data class PickSafeMessage(val BODY:PickSafeBody) : BasicMessage("PICK_SAFE")
data class PickSafeBody(val name:String)

//utils
data class SafePick(val SafeId: Int, val Inside: String)
data class Reward (val type : String, val ammount : Int)
data class Prize (
    val objectType : String,
    val currentNumber : Int,
    val target : Int,
    val reward : Reward,
)

data class Days (

    val monday : String,
    val tuesday : String,
    val wednesday : String,
    val thursday : String,
    val friday : String,
    val saturday : String,
    val sunday : String
)

class ConnectionController(private val session: DefaultWebSocketSession){

    private val gson = Gson()

    // would have been cool to see a language with this feature :(
    //compiler doesn't seem to try to pick the correct function from the T (maybe one day)
//    private inline fun <reified T : BasicMessage> handleMessage(receivedText: String, databaseConnection: DatabaseConnection){
//        HandleReceivedMessage(gson.fromJson(receivedText,T::class.java), this, databaseConnection)
//    }
    
    fun ProcessMessage(receivedText: String, databaseConnection: DatabaseConnection){
        try {
            val basic : BasicMessage = gson.fromJson(receivedText,BasicMessage::class.java)
            when(basic.Type){
                "LOGIN" ->  HandleReceivedMessage(gson.fromJson(receivedText,LoginMessage::class.java), this, databaseConnection)
                "PICK_SAFE" ->  HandleReceivedMessage(gson.fromJson(receivedText,PickSafeMessage::class.java), this, databaseConnection)
                else -> {
                    println("Invalid message: $receivedText")
                    return
                }
            }
        }catch ( e : JsonSyntaxException){
            println("Invalid message: $receivedText")
        }
    }



    fun SendMessage(message: BasicMessage) {
        val textToSend : String = gson.toJson(message)
        GlobalScope.launch { // launch a new coroutine in background and continue
            session.send(textToSend)
        }
    }


}