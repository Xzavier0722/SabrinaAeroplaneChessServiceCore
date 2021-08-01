package com.xzavier0722.uon.sabrinaaeroplanechess.servicecore

import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.SessionManager
import com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.data.SQLiteSource
import com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.data.DataStorage
import com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.socket.GameServiceListener
import com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.socket.LoginServiceListener

val SessionManager = SessionManager()
val DataStorage = DataStorage(SQLiteSource("data","SabrinaAeroplane.db"))
val LoginServiceListener = LoginServiceListener()
val GameServiceListener = GameServiceListener()
@Volatile var PacketId = 0

fun main() {

}

class Startup {



}