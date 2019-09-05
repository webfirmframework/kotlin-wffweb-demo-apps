package com.wffwebdemo.minimalproductionsample.server.util

import com.wffwebdemo.minimalproductionsample.server.constants.ServerConstants
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.logging.Logger

object HeartBeatUtil {

    private val LOGGER = Logger
            .getLogger(HeartBeatUtil::class.java.getName())

    fun ping(sessionId: String) {

        if (ServerConstants.CONTEXT_PATH == null) {
            return
        }

        val thread = Thread(Runnable {
            try {
                val url = ServerConstants.DOMAIN_URL + ServerConstants.CONTEXT_PATH + "/heart-beat"

                val obj = URL(url)
                val con = obj
                        .openConnection() as HttpURLConnection

                // optional default is GET
                con.requestMethod = "GET"
                con.setRequestProperty("Cookie", "JSESSIONID=" + sessionId)
                con.connect()

                val responseCode = con.responseCode

                LOGGER.info("responseCode " + responseCode)

                val `in` = BufferedReader(
                        InputStreamReader(con.inputStream))
                var inputLine: String?
                val response = StringBuilder()

                inputLine = `in`.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = `in`.readLine()
                }

                `in`.close()
                LOGGER.info("heartbeat response " + response)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
        thread.isDaemon = true
        thread.start()

    }

}
