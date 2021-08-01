package com.xzavier0722.uon.sabrinaaeroplanechess.servicecore

import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.SessionManager
import com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.data.SQLiteSource
import com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.data.DataStorage

val SessionManager = SessionManager()
val DataStorage = DataStorage(SQLiteSource("data","SabrinaAeroplane.db"))

fun main() {

}

class Startup {



}