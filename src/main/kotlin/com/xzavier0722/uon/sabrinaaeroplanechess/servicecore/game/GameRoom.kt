package com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.game

import com.xzavier0722.uon.sabrinaaeroplanechess.common.Utils
import com.xzavier0722.uon.sabrinaaeroplanechess.common.game.PlayerProfile
import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.Packet
import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.Request
import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.Session
import com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.GameServiceListener
import com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.PacketId
import com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.socket.PacketUtils
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class GameRoom(val code: String) {

    private val players = HashMap<String, Session>()
    lateinit var owner: String
    private val random = Random()

    fun addPlayer(session: Session) : Boolean {
        if (players.isEmpty()) {
            owner = session.id
        }
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

    fun removePlayer(session: Session) : Boolean{
        if (session.id == owner) {
            players.remove(owner)
            return true
        }
        val removed = players.remove(session.id)
        if (removed != null && players.size > 0) {
            players.values.forEach {
                val packet = PacketUtils.getGameRoomUpdatePacket(removed, true)
                packet.sessionId = it.id
                GameServiceListener.send(it.inetPoint, packet)
            }
        }
        return false
    }

    fun getPlayerList() : List<PlayerProfile> {
        val re = ArrayList<PlayerProfile>()
        players.values.forEach {
            re.add(it.playerProfile)
        }
        return re
    }

    fun kickAll() = sendToAll("kick")

    fun sendToAll(data: String) {
        players.values.forEach {
            val packet = Packet()
            packet.sessionId = it.id
            packet.sign = Utils.getSign(data)
            packet.data = it.aes.encrypt(data)
            packet.request = Request.GAME_ROOM
            packet.id = PacketId++
            GameServiceListener.send(it.inetPoint, packet)
        }
    }

    fun sendProcess(session: Session?, data: String) {
        players.values.forEach {
            if (it.id != session?.id) {
                val packet = Packet()
                packet.sessionId = it.id
                packet.sign = Utils.getSign(data)
                packet.data = it.aes.encrypt(data)
                packet.request = Request.GAME_PROCESS
                packet.id = PacketId++
                GameServiceListener.send(it.inetPoint, packet)
            }
        }
    }

    fun getDice() : Int {
        return 1+random.nextInt(6)
    }

}