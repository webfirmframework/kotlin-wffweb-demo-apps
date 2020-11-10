package com.wffwebdemo.minimalproductionsample.server.ws

import com.webfirmframework.wffweb.server.page.HeartbeatManager
import com.wffwebdemo.minimalproductionsample.server.constants.ServerConstants
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

class HeartbeatRunnable(private val httpSessionId: String) : Runnable {
    override fun run() {

        if (ServerConstants.CONTEXT_PATH == null) {
            return
        }
        var con: HttpURLConnection? = null
        var `in`: BufferedReader? = null
        try {
            val url = ServerConstants.DOMAIN_URL + ServerConstants.CONTEXT_PATH + "/heart-beat"
            val obj = URL(url)
            con = obj.openConnection() as HttpURLConnection

            // optional default is GET
            con.requestMethod = "GET"
            con.setRequestProperty("Cookie", "JSESSIONID=$httpSessionId")
            con.connect()
            val responseCode = con.responseCode
            LOGGER.info("responseCode $responseCode")
            `in` = BufferedReader(
                    InputStreamReader(con.inputStream))
            var inputLine: String?
            val response = StringBuilder()
            while (`in`.readLine().also { inputLine = it } != null) {
                response.append(inputLine)
            }
            LOGGER.info("heartbeat response $response")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (`in` != null) {
                try {
                    `in`.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            con?.disconnect()
        }
    }

    companion object {
        private val LOGGER = Logger
                .getLogger(HeartbeatRunnable::class.java.name)

        @JvmField
        val HEARTBEAT_MANAGER_MAP: ConcurrentHashMap<String, HeartbeatManager> = ConcurrentHashMap()
    }
}