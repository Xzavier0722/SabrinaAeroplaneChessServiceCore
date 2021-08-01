package com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.game

import com.xzavier0722.uon.sabrinaaeroplanechess.common.game.PlayerProfile
import kotlin.collections.HashMap

class GameRoom(val code: String) {

    private val players = HashMap<String, PlayerProfile>()

    fun addPlayer() {
        
    }

    fun removePlayer(uuid: String) {
        players.remove(uuid)
    }

}