package com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.game

import com.xzavier0722.uon.sabrinaaeroplanechess.common.game.PlayerProfile
import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.Session
import com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.GameServiceListener
import com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.socket.PacketUtils
import kotlin.collections.HashMap

class GameRoom(val code: String) {

    private val players = HashMap<String, Session>()

    fun addPlayer(session: Session) : Boolean {
        if (players.size > 3) {
            return false
        }
        players.values.forEach {
            val packet = PacketUtils.getGameRoomUpdatePacket(session, false)
            packet.sessionId = it.id
            GameServiceListener.send(it.inetPoint, packet)
        }
        players[session.id] = session
        return true
    }

    fun removePlayer(session: Session) {
        val removed = players.remove(session.id)
        if (removed != null && players.size > 0) {
            players.values.forEach {
                val packet = PacketUtils.getGameRoomUpdatePacket(removed, true)
                packet.sessionId = it.id
                GameServiceListener.send(it.inetPoint, packet)
            }
        }
    }

    fun getPlayerList() : List<PlayerProfile> {
        val re = ArrayList<PlayerProfile>()
        players.values.forEach {
            re.add(it.playerProfile)
        }
        return re
    }

}