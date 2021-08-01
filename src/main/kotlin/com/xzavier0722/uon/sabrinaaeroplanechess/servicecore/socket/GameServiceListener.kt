package com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.socket

import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.Packet
import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.Request

class GameServiceListener : ServiceListener(7221){


    override fun onReceive(packet: Packet) {
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