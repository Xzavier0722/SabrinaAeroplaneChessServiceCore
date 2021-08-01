package com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.socket

import com.xzavier0722.uon.sabrinaaeroplanechess.common.Utils
import com.xzavier0722.uon.sabrinaaeroplanechess.common.game.PlayerProfile
import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.Packet
import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.Request
import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.Session
import com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.PacketId

object PacketUtils {

    fun getReplyPacketFor(packet: Packet) : Packet {
        val re = Packet()
        re.id = packet.id
        re.sessionId = packet.sessionId
        return re
    }

    fun getConfirmPacket(packet: Packet) : Packet {
        val re = getReplyPacketFor(packet)
        re.data = "CONFIRM"
        re.request = Request.CONFIRM
        re.sign = "NULL"
        return re
    }

    fun getErrorPacket(packet: Packet) : Packet {
        val re = getReplyPacketFor(packet)
        re.request = Request.ERROR
        re.data = "ERROR"
        re.sign = "NULL"
        return re
    }

    fun getGameRoomUpdatePacket(session: Session, isRemove: Boolean) : Packet {
        val data = (if (isRemove) "remove," else "add,") + Utils.base64(Utils.getGson().toJson(session.playerProfile))

        val re = Packet()
        re.request = Request.GAME_ROOM
        re.sign = Utils.getSign(data)
        re.data = session.aes.encrypt(data)
        re.id = PacketId++

        return re
    }

}