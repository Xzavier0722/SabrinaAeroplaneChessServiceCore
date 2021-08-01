package com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.socket

import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.Packet
import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.Request

class LoginServiceListener: ServiceListener(7220) {

    override fun onReceive(packet: Packet) {

        when (packet.request) {
            Request.LOGIN -> {

            }
            Request.REGISTER -> {

            }
            else -> {}
        }

    }

}