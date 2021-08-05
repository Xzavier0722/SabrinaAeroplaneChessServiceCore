package com.xzavier0722.uon.sabrinaaeroplanechess.servicecore

import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.SessionManager
import com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.data.SQLiteSource
import com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.data.DataStorage
import com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.socket.GameServiceListener
import com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.socket.LoginServiceListener

private val SQLiteSource = SQLiteSource("data","SabrinaAeroplane.db")
val SessionManager = SessionManager()
val DataStorage = DataStorage(SQLiteSource)
lateinit var LoginServiceListener: LoginServiceListener
lateinit var GameServiceListener: GameServiceListener
@Volatile var PacketId = 0

fun main() {

    SQLiteSource.start()
    println("Starting login service at 7220")
    LoginServiceListener = LoginServiceListener()

    println("Starting game service at 7221")
    GameServiceListener = GameServiceListener()

    println("Services started. Use /end to stop")
    while (true) {
        val input = readLine()
        if (input == "/end") {
            println("Stopping services")
            LoginServiceListener.abort()
            GameServiceListener.abort()
            SQLiteSource.abort()
            return
        }
    }
}