package com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.data

import com.xzavier0722.uon.sabrinaaeroplanechess.common.Utils
import com.xzavier0722.uon.sabrinaaeroplanechess.common.game.PlayerProfile
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.HashMap

class DataStorage(private val dataSource: DataSource) {

    private val profileCache = HashMap<String, PlayerProfile>()
    private val keyCache = HashMap<String, String>()
    private val uuidCache = HashMap<String, String>()

    init {
        dataSource.execute(
            "CREATE TABLE IF NOT EXISTS PlayerProfile (" +
                    "UUID CHAR(36) NOT NULL," +
                    "Name TEXT NOT NULL," +
                    "PlayCount INT NOT NULL," +
                    "Wins INT NOT NULL," +
                    "Key TEXT NOT NULL);"
        )
    }

    fun getUserKey(name: String) : Optional<String> {
        loadPlayerProfileByName(name)
        return Optional.ofNullable(keyCache[uuidCache[name]])
    }

    fun getPlayerProfileByName(name: String) : Optional<PlayerProfile> {
        loadPlayerProfileByName(name)
        return Optional.ofNullable(profileCache[uuidCache[name]])
    }

    fun getPlayerProfileByUUID(uuid: String) : Optional<PlayerProfile> {
        loadPlayerProfileByUUID(uuid)
        return Optional.ofNullable(profileCache[uuid])
    }

    fun addWins(player: PlayerProfile) {
        player.addWins()
        updateInfo(player.uuid.toString(), "Wins", player.wins.toString())
    }

    fun addPlayCount(player: PlayerProfile) {
        player.addCount()
        updateInfo(player.uuid.toString(), "PlayCount", player.playCount.toString())
    }

    fun isNameExists(name: String) : Boolean {
        if (uuidCache.containsKey(name)) {
            return true
        }
        return loadPlayerProfileByName(name)
    }

    fun register(player: PlayerProfile, key: String) {
        if (isNameExists(player.name)) {
            throw IllegalArgumentException("The name already exists!")
        }
        val uuid = player.uuid.toString()
        dataSource.execute(
            "INSERT INTO PlayerProfile VALUES (" +
                "'$uuid'," +
                "'${Utils.base64(player.name)}'," +
                "${player.playCount}," +
                "${player.wins},'" + key + "');"
        )
        profileCache[uuid] = player
        uuidCache[player.name] = uuid
        keyCache[uuid] = key
    }

    private fun updateInfo(uuid: String, key: String, value: String) {
        dataSource.execute("UPDATE PlayerProfile SET $key = '$value' WHERE UUID = '$uuid';")
    }

    private fun loadPlayerProfileByName(name: String) : Boolean {
        if (uuidCache.containsKey(name)) {
            return true
        }
        val result = dataSource.request("SELECT * FROM PlayerProfile WHERE Name = '${Utils.base64(name)}';")
        if (result.isNotEmpty()) {
            val data = result[0]
            val uuid = data["UUID"]!!
            val profile = PlayerProfile(UUID.fromString(uuid), String(Utils.debase64(data["Name"])), data["PlayCount"]!!.toInt(), data["Wins"]!!.toInt())
            profileCache[uuid] = profile
            keyCache[uuid] = data["Key"]!!
            uuidCache[name] = uuid
            return true
        }
        return false
    }

    private fun loadPlayerProfileByUUID(uuid: String) : Boolean {
        if (profileCache.containsKey(uuid)) {
            return false
        }
        val result = dataSource.request("SELECT * FROM PlayerProfile WHERE UUID = '$uuid';")
        if (result.isNotEmpty()) {
            val data = result[0]
            val uuid = data["UUID"]!!
            val profile = PlayerProfile(UUID.fromString(uuid), String(Utils.debase64(data["Name"])), data["PlayCount"]!!.toInt(), data["Wins"]!!.toInt())
            profileCache[uuid] = profile
            keyCache[uuid] = data["Key"]!!
            uuidCache[profile.name] = uuid
            return true
        }
        return false
    }

}