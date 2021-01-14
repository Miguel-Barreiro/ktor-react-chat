package lindar.server.connection

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import lindar.server.gameLogic.HandleReceivedMessage
import lindar.server.gameLogic.database.DatabaseConnection
import java.sql.SQLException
import java.util.*


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

data class PickSafeResponseMessage(val BODY : PickSafeResponseBody) : BasicMessage( "PICK_SAFE_RESPONSE")
data class PickSafeResponseBody( 
    val SafePicks:List<SafePick>,
    val CurrentTriesLeft: Int
)

data class PickSafeFailMessage(val BODY : PickSafeFailBody) : BasicMessage( "PICK_SAFE_RESPONSE_ERROR")
data class PickSafeFailBody(
    val ReasonCode: Int
)


//client Messages
data class LoginMessage(val BODY: LoginBody) : BasicMessage("LOGIN")
data class LoginBody(val PlayerId:String)

data class PickSafeMessage(val BODY:PickSafeBody) : BasicMessage("PICK_SAFE")
data class PickSafeBody(val SafeId:Int)

//utils
data class SafePick(val SafeId: Int, val Inside: String, val PickDate: Date? = null)
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

    var PlayerId: String? = null
    private val gson = Gson()

    // would have been cool to see a language with this feature :(
    //compiler doesn't seem to try to pick the correct function from the T (maybe one day)
//    private inline fun <reified T : BasicMessage> handleMessage(receivedText: String, databaseConnection: DatabaseConnection){
//        HandleReceivedMessage(gson.fromJson(receivedText,T::class.java), this, databaseConnection)
//    }
 
    
    fun ProcessMessage(receivedText: String, databaseConnection: DatabaseConnection){
        try {
            val basic : BasicMessage = gson.fromJson(receivedText,BasicMessage::class.java)
            if( this.PlayerId != null){
                when(basic.Type){
                    "PICK_SAFE" ->  HandleReceivedMessage(
                        gson.fromJson(receivedText,PickSafeMessage::class.java).BODY,
                        this,
                        databaseConnection)
                    else -> {
                        println("Invalid message: $receivedText")
                        return
                    }
                }
            }else {

                when (basic.Type) {
                    "LOGIN" -> HandleReceivedMessage(
                        gson.fromJson(receivedText, LoginMessage::class.java).BODY,
                        this,
                        databaseConnection
                    )
                    else -> {
                        println("Invalid message: $receivedText")
                        return
                    }
                }
            }
        }catch ( e : JsonSyntaxException){
            println("Invalid message: ${e.message}")
        }catch( e : SQLException){
            println("Database error: ${e.message}")
            e.printStackTrace()
        }catch (e : Exception){
            println("Some error: ${e.message}")
            e.printStackTrace()
        }
    }



    fun SendMessage(message: BasicMessage) {
        val textToSend : String = gson.toJson(message)
        GlobalScope.launch { // launch a new coroutine in background and continue
            session.send(textToSend)
        }
    }


}