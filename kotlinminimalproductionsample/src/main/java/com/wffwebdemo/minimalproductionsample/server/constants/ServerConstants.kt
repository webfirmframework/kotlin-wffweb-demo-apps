package com.wffwebdemo.minimalproductionsample.server.constants

object ServerConstants {

    var DOMAIN_URL: String

    val DOMAIN_WS_URL: String

    val ORIGIN_DOMAIN_URL: String

    val SESSION_TIMEOUT: Int

    var SESSION_TIMEOUT_SECONDS: Int

    var SESSION_TIMEOUT_MILLISECONDS: Int

    private val LOCAL_MACHINE_IP = "localhost"

    private val LOCAL_MACHINE_PORT: String

    const val INDEX_PAGE_WS_URI = "/ws-for-index-page"

    /**
     * must be null initially
     */
    var CONTEXT_PATH: String?  = null

    init {

        val domainUrlFromEnv = System.getenv("DOMAIN_URL")
        val domainWsUrlFromEnv = System.getenv("DOMAIN_WS_URL")
        val originDomainUrlFromEnv = System.getenv("ORIGIN_DOMAIN_URL")
        val sessionTimeoutFromEnv = System.getenv("SESSION_TIMEOUT")

        val webPort = System.getenv("PORT")
        LOCAL_MACHINE_PORT = if (webPort != null && !webPort.isEmpty())
            webPort
        else
            "8080"

        DOMAIN_URL = domainUrlFromEnv ?: "http://$LOCAL_MACHINE_IP:$LOCAL_MACHINE_PORT"

        DOMAIN_WS_URL = domainWsUrlFromEnv ?: "ws://$LOCAL_MACHINE_IP:$LOCAL_MACHINE_PORT"

        ORIGIN_DOMAIN_URL = originDomainUrlFromEnv ?: "http://$LOCAL_MACHINE_IP:$LOCAL_MACHINE_PORT"

        SESSION_TIMEOUT = if (sessionTimeoutFromEnv != null)
            Integer.parseInt(sessionTimeoutFromEnv)
        else
            5

        SESSION_TIMEOUT_MILLISECONDS = SESSION_TIMEOUT * 1000 * 60
        SESSION_TIMEOUT_SECONDS = SESSION_TIMEOUT * 60
    }

}
