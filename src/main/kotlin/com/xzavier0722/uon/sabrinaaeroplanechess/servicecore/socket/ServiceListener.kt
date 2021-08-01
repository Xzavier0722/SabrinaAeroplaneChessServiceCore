package com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.socket

import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.*
import java.net.DatagramPacket

abstract class ServiceListener(port: Int) {

    protected val point: SocketPoint = SocketPoint(port) { onReceive(it) }
    private val processingPackets = HashMap<String, HandlingDatagramPacket>()
    @Volatile private var seq = 0

    init {
        point.start()
    }

    private fun onReceive(packet: DatagramPacket) {
        val info = InetPointInfo.get(packet)
        val infoStr = info.toString()
        var handlingPacket = processingPackets[infoStr]
        val data = packet.data

        if (handlingPacket == null) {
            // New income,
            handlingPacket = HandlingDatagramPacket()
            handlingPacket.accept(data)
            val identifier = data[0]
            if (identifier == 0x48.toByte()) {
                val len = data[1].toInt()
                if (len == 1) {
                    onReceive(info ,handlingPacket.packet)
                    return
                }
            }
            processingPackets[infoStr] = handlingPacket
        } else {
            handlingPacket.accept(data)
            if (handlingPacket.isCompleted) {
                onReceive(info, handlingPacket.packet)
            }
        }

    }

    private fun onReceive(info: InetPointInfo, packet: Packet) {
        val request = packet.request
        when (request) {
            Request.CONFIRM -> {
                processingPackets.remove(info.toString())
                return
            }
            Request.RESEND -> {
                val handlingPacket = processingPackets[info.toString()]
                if (handlingPacket == null) {
                    send(info, PacketUtils.getErrorPacket(packet))
                } else {
                    val rePacket = handlingPacket.getDatagramPacket(packet.data.toInt(), info)
                    if (rePacket.isPresent) {
                        point.send(rePacket.get())
                    } else {
                        send(info, PacketUtils.getErrorPacket(packet))
                    }
                }
                return
            }
            else -> {}
        }

        onReceive(packet)
        // Send confirm
        if (request.requireConfirm()) {
            send(info, PacketUtils.getConfirmPacket(packet))
        }
    }

    fun send(info: InetPointInfo, packet: Packet) {
        packet.sequence = seq++
        packet.timestamp = System.currentTimeMillis()
        val handlingPacket = HandlingDatagramPacket.getFor(packet)

        for (i in 0 until handlingPacket.sliceCount) {
            point.send(handlingPacket.getDatagramPacket(i, info).get())
        }

        if (packet.request.requireConfirm()) {
            processingPackets[info.toString()] = handlingPacket
        }
    }

    abstract fun onReceive(packet: Packet)

    fun abort() = point.abort()

}