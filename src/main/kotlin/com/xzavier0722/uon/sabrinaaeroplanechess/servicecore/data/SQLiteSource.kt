package com.xzavier0722.uon.sabrinaaeroplanechess.servicecore.data

import com.xzavier0722.uon.sabrinaaeroplanechess.common.threading.QueuedExecutionThread
import java.io.File
import java.sql.Connection
import java.sql.DriverManager

class SQLiteSource(path: String, name: String): QueuedExecutionThread(), DataSource{

    private var conn: Connection

    init {
        val dir = File(path)
        if (!dir.exists() || !dir.isDirectory) {
            if (!dir.mkdirs()) {
                throw IllegalStateException("Cannot create data folder!")
            }
        }
        Class.forName("org.sqlite.JDBC")
        this.conn = DriverManager.getConnection("jdbc:sqlite:" + dir.path + name)
    }

    override fun execute(sql: String) {
        schedule{
            conn.createStatement().use {
                it.execute(sql)
            }
        }
    }

    override fun request(sql: String): List<Map<String, String>> {
        val re = ArrayList<HashMap<String, String>>()
        conn.createStatement().use { stat ->
            stat.executeQuery(sql).use { resultSet ->
                // Traverse each row
                while (resultSet.next()) {
                    val row = HashMap<String, String>()
                    val meta = resultSet.metaData

                    // Traverse each column
                    for (i in 0 until meta.columnCount) {
                        row[meta.getColumnLabel(i)] = resultSet.getString(i)
                    }

                    re.add(row)
                }
            }
        }
        return re
    }

}