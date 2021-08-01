package com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.socket

import com.xzavier0722.uon.sabrinaaeroplanechess.common.Utils
import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.InetPointInfo
import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.Packet
import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.Request
import com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.SessionManager

class GameServiceListener : ServiceListener(7221){


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

            }
            Request.GAME_PROCESS -> {

            }
            Request.QUICK_MATCH -> {

            }

            else -> {}
        }
    }

}