package com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.data

interface DataSource {

    // Only for those queries which not requiring result, such as UPDATE, CREATE, DROP etc.
    fun execute(sql: String)

    // Return the result
    fun request(sql: String): List<Map<String, String>>

}