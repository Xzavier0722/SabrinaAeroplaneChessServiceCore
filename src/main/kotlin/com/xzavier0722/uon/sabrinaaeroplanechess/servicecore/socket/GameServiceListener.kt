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
        // Check if session information should update
        if (session.inetPoint.toString() != info.toString()) {
            session.inetPoint = info
        }

        val data = session.aes.decrypt(packet.data)

        // Check data
        if (Utils.getSign(data) != packet.sign) {
            send(info, PacketUtils.getErrorPacket(packet))
            return
        }

        when (packet.request) {
            Request.GAME_ROOM -> {
                /**
                 * Game room related:
                 * Data format: "create/join/leave/start/kick,other data (if have)"
                 */
                val request = data.split(",")
                when (request[0]) {
                    "create" -> {
                        /**
                         * Game room create request:
                         * 1. Client sends create request with data "create"
                         * 2. Server replies with the room code
                         */
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
                        /**
                         * Join room request:
                         * 1. Client sends join request with data formatted by: "join,room code"
                         * 2. Server replies with data "0,base64(playerProfile1),base64(playerProfile2),..." (success) or "-1" (failed)
                         */
                        val room = gameRooms[request[1]]
                        val reply = PacketUtils.getReplyPacketFor(packet)
                        reply.request = Request.GAME_ROOM
                        if (room != null) {
                            val list = room.getPlayerList()
                            if (room.addPlayer(session)) {
                                println("Player "+session.playerProfile.uuid+" joined room "+room.code)
                                val sb = StringBuilder()
                                sb.append("0")
                                for (each in list) {
                                    sb.append(",")
                                    sb.append(Utils.base64(Utils.getGson().toJson(each)))
                                }
                                val reData = sb.toString()
                                reply.data = session.aes.encrypt(reData)
                                reply.sign = Utils.getSign(reData)
                            }
                        } else {
                            val msg = "-1"
                            reply.data = session.aes.encrypt(msg)
                            reply.sign = Utils.getSign(msg)
                        }
                        send(info, reply)
                        return
                    }
                    "leave" -> {
                        /**
                         * Leave room request:
                         * 1. Client sends leave request with data formatted by: "leave"
                         * 2. Server sends a game room update packet to all room members
                         */
                        val roomId = GameRoom.getRoomCode(session.id)
                        val room = gameRooms[roomId?:"NULL"]
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
                        /**
                         * Start game request:
                         * 1. Room owner send start request with data formatted: "start,uuids order by flag"
                         * 2. Server send game start packet to all room members
                         */
                        val roomId = GameRoom.getRoomCode(session.id)
                        val room = gameRooms[roomId?:"NULL"]
                        if (room != null && session.id == room.owner) {
                            println("Room "+room.code+" started game")
                            room.sendToAll("start,"+request[1])
                            return
                        }
                    }
                    "kick" -> {
                        /**
                         * Kick player request:
                         * 1. Room owner send kick request with data formatted: "kick,kicked uuid"
                         * 2. Server send kick packet with data "kick,uuid" to all room members
                         */
                        val roomId = GameRoom.getRoomCode(session.id)
                        val room = gameRooms[roomId?:"NULL"]
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
                /**
                 * Game process related:
                 * The turn operator send request to server "turnStart/pieceSelected/gameEnd"
                 * TODO: Timer and timeout
                 */
                val request = data.split(",")
                val roomId = GameRoom.getRoomCode(session.id)
                val room = gameRooms[roomId?:"NULL"]
                if (room != null) {
                    when (request[0]) {
                        "turnStart" -> {
                            /**
                             * Turn start request:
                             * 1. Client sends turn start request with data "turnStart"
                             * 2. Server sends "turnStart,dice number" to all room members
                             */
                            println("Room "+room.code+" turn start")
                            room.sendProcess(null, "turnStart,"+room.getDice())
                            return
                        }
                        "pieceSelected" -> {
                            /**
                             * Piece selected request:
                             * 1. Client sends piece selected request with data "pieceSelected,piece id"
                             * 2. Server sends "turnStart,dice number" to all room members
                             */
                            println("Player "+session.playerProfile.uuid+" piece selected "+request[1])
                            room.sendProcess(session, "selectedPiece,"+request[1])
                            return
                        }
                        "gameEnd" -> {
                            /**
                             * Game end request:
                             * 1. Client sends game end request with data "ganeEnd"
                             * 2. Server will not reply anything
                             */
                            println("Room "+room.code+" game ended")
                            room.destroy()
                            gameRooms.remove(roomId)
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