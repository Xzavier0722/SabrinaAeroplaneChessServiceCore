package com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.socket

import com.xzavier0722.uon.sabrinaaeroplanechess.common.Utils
import com.xzavier0722.uon.sabrinaaeroplanechess.common.game.PlayerProfile
import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.InetPointInfo
import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.Packet
import com.xzavier0722.uon.sabrinaaeroplanechess.common.networking.Request
import com.xzavier0722.uon.sabrinaaeroplanechess.common.security.AES
import com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.DataStorage
import com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.SessionManager

class LoginServiceListener: ServiceListener(7220) {

    override fun onReceive(packet: Packet, info: InetPointInfo) {

        when (packet.request) {
            Request.LOGIN -> {
                /**
                 * Login process:
                 * 1. Client send a packet using AES sha256(password) encrypt "Login" in data and base64(Name) in sessionId
                 * 2. Server use stored user key (which is sha256(password)) to decrypt data and check if "Login"
                 * 3. If success, server create session, and reply packet included session key
                 *       encrypted by AES sha256(password) and real session id in sessionId
                 * 4. Future will use this session key to en/decrypt data
                 */
                // Get user key
                val name = String(Utils.debase64(packet.sessionId))
                val key = DataStorage.getUserKey(name)
                if (key.isPresent) {
                    val aes = AES(Utils.debase64(key.get()))
                    val data = aes.decrypt(packet.data)
                    if (data == "Login") {
                        // Login successful, create session
                        val session = SessionManager.createSession()
                        session.playerProfile = DataStorage.getPlayerProfileByName(name).get()
                        session.inetPoint = info

                        println("Player "+session.playerProfile.uuid+" login successful")

                        // Create reply packet
                        val replyPacket = PacketUtils.getReplyPacketFor(packet)
                        replyPacket.sign = Utils.getSign(session.key)
                        replyPacket.data = aes.encrypt(session.key)
                        replyPacket.request = Request.LOGIN
                        replyPacket.sessionId = session.id
                        send(info, replyPacket)
                        return
                    }
                }
                send(info, PacketUtils.getErrorPacket(packet))
            }
            Request.REGISTER -> {
                val key = Utils.base64(Utils.sha256(packet.timestamp.toString()+packet.sessionId))
                val aes = AES(key)
                val data = aes.decrypt(packet.data)
                // Check data
                if (Utils.getSign(data) == packet.sign) {
                    val userInfo = data.split(",")
                    // Check if exists
                    if (!DataStorage.isNameExists(userInfo[0])) {
                        val profile = PlayerProfile(userInfo[0])
                        DataStorage.register(profile, userInfo[1])

                        println("Player "+profile.uuid+" register successful")
                        // Create reply packet
                        val replyPacket = PacketUtils.getReplyPacketFor(packet)
                        replyPacket.data = profile.uuid.toString()
                        replyPacket.request = Request.REGISTER
                        replyPacket.sign = "NULL"

                        send(info, replyPacket)
                        return
                    }
                }
                send(info, PacketUtils.getErrorPacket(packet))
            }
            else -> {}
        }

    }

}