package com.xzavier0722.uon.sabrinaaeroplanechess.servicecore

import com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.data.DataStorage
import com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.data.SQLiteSource

fun main() {
    DataStorage.dataSource = SQLiteSource("data","SabrinaAeroplane.db")
}

class Startup {



}