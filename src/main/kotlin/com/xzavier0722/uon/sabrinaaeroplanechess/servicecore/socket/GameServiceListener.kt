package com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.socket

import com.xzavier0722.uon.sabrinaaeroplanechess.common.Utils
import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.InetPointInfo
import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.Packet
import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.Request
import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.Session
import com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.SessionManager
import com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.game.GameRoom
import java.util.concurrent.LinkedBlockingQueue
import kotlin.text.StringBuilder

class GameServiceListener : ServiceListener(7221){

    private val gameRooms = HashMap<String, GameRoom>()
    private val queue = LinkedBlockingQueue<Session>()

    override fun onReceive(packet: Packet, info: InetPointInfo) {

        // Get session
        val sessionOpt = SessionManager.getSession(packet.sessionId)

        if (!sessionOpt.isPresent) {
            send(info, PacketUtils.getErrorPacket(packet))
            return
        }

        val session = sessionOpt.get()
        val data = session.aes.decrypt(packet.data)

        // Check data
        if (Utils.getSign(data) != packet.sign) {
            send(info, PacketUtils.getErrorPacket(packet))
            return
        }

        when (packet.request) {
            Request.GAME_ROOM -> {
                val request = data.split(",")
                when (request[0]) {
                    "create" -> {
                        val id = createNewRoomId()
                        println("Player "+session.playerProfile.uuid+" created room "+id)
                        val room = GameRoom(id)
                        room.addPlayer(session)
                        gameRooms[id] = room
                        val reply = PacketUtils.getReplyPacketFor(packet)
                        reply.request = Request.GAME_ROOM
                        reply.data = session.aes.encrypt(id)
                        reply.sign = Utils.getSign(id)
                        send(info, reply)
                        return
                    }
                    "join" -> {
                        val room = gameRooms[request[1]]
                        if (room != null && room.addPlayer(session)) {
                            println("Player "+session.playerProfile.uuid+" joined room "+room.code)
                            return
                        }
                    }
                    "leave" -> {
                        val room = gameRooms[request[1]]
                        if (room != null) {
                            println("Player "+session.playerProfile.uuid+" leaved room "+room.code)
                            if (room.removePlayer(session)) {
                                // The host leave, destroy the room
                                room.kickAll()
                                gameRooms.remove(request[1])
                            }
                            return
                        }
                    }
                    "start" -> {
                        val room = gameRooms[request[1]]
                        if (room != null && session.id == room.owner) {
                            println("Room "+room.code+" started game")
                            room.sendToAll("start,"+request[2])
                            return
                        }
                    }
                    "kick" -> {
                        val room = gameRooms[request[1]]
                        if (room != null && session.id == room.owner) {
                            println("Player "+session.playerProfile.uuid+" kicked room "+room.code)
                            room.sendToAll("kick,"+request[2])
                            return
                        }
                    }
                }
                send(info, PacketUtils.getErrorPacket(packet))
            }
            Request.GAME_PROCESS -> {
                val request = data.split(",")
                val room = gameRooms[request[1]]
                if (room != null) {
                    when (request[0]) {
                        "turnStart" -> {
                            println("Room "+room.code+" turn start")
                            room.sendProcess(null, "turnStart,"+room.getDice())
                            return
                        }
                        "pieceSelected" -> {
                            println("Player "+session.playerProfile.uuid+" piece selected "+request[2])
                            room.sendProcess(session, "selectedPiece,"+request[2])
                            return
                        }
                        "gameEnd" -> {
                            println("Room "+room.code+" game ended")
                            gameRooms.remove(request[1])
                            return
                        }
                    }
                }
                send(info, PacketUtils.getErrorPacket(packet))
            }
            Request.QUICK_MATCH -> {
                queue.offer(session)
                println("Player "+session.playerProfile.uuid+" queued for quick match. Queue: "+queue.size)
                if (queue.size > 3) {
                    val id = createNewRoomId()
                    val room = GameRoom(id)
                    val sb = StringBuilder()
                    for (i in 1 .. 4) {
                        sb.append(",")
                        val p = queue.take()
                        room.addPlayer(p)
                        sb.append(p.playerProfile.uuid.toString())
                    }
                    println("Quick match success!")
                    room.sendToAll("start$sb")
                }
            }

            else -> {}
        }
    }

    private fun createNewRoomId() : String {
        val re = Utils.randomString(8)
        return if (gameRooms.containsKey(re)) createNewRoomId() else re
    }

}