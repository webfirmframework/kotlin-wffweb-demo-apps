package com.wffwebdemo.minimalproductionsample.server.ws

import com.webfirmframework.wffweb.PushFailedException
import com.webfirmframework.wffweb.server.page.BrowserPage
import com.webfirmframework.wffweb.server.page.BrowserPageContext
import com.webfirmframework.wffweb.server.page.action.BrowserPageAction
import com.wffwebdemo.minimalproductionsample.server.constants.ServerConstants
import com.wffwebdemo.minimalproductionsample.server.constants.ServerConstants.INDEX_PAGE_WS_URI
import com.wffwebdemo.minimalproductionsample.server.util.HeartBeatUtil
import java.io.IOException
import java.util.logging.Logger
import javax.servlet.ServletRequestEvent
import javax.servlet.ServletRequestListener
import javax.servlet.annotation.WebListener
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import javax.websocket.*
import javax.websocket.server.HandshakeRequest
import javax.websocket.server.ServerEndpoint
import javax.websocket.server.ServerEndpointConfig
import javax.websocket.server.ServerEndpointConfig.Configurator

/**
 * @ServerEndpoint gives the relative name for the end point This will be
 * accessed via
 * ws://localhost:8080/wffwebdemoproject/ws-for-wffweb.
 */
@ServerEndpoint(value = INDEX_PAGE_WS_URI, configurator = WSServerForIndexPage::class)
@WebListener
class WSServerForIndexPage : Configurator(), ServletRequestListener {

    private var browserPage: BrowserPage? = null

    private var httpSession: HttpSession? = null

    private var lastHeartbeatTime: Long = 0

    override fun modifyHandshake(config: ServerEndpointConfig?,
                                 request: HandshakeRequest?, response: HandshakeResponse?) {

        var httpSession: HttpSession? = request!!.httpSession as HttpSession

        super.modifyHandshake(config, request, response)

        if (httpSession == null) {
            LOGGER.info("httpSession == null after modifyHandshake")
            httpSession = request.httpSession as HttpSession
        }

        if (httpSession == null) {
            LOGGER.info("httpSession == null")
            return
        }

        config!!.userProperties.put("httpSession", httpSession)

        httpSession = request.httpSession as HttpSession
        LOGGER.info("modifyHandshake " + httpSession.id)

    }

    /**
     * @OnOpen allows us to intercept the creation of a new session. The session
     * class allows us to send data to the user. In the method onOpen,
     * we'll let the user know that the handshake was successful.
     */
    @OnOpen
    fun onOpen(session: Session, config: EndpointConfig) {

        LOGGER.info("onOpen")

        session.maxIdleTimeout = ServerConstants.SESSION_TIMEOUT_MILLISECONDS.toLong()

        httpSession = config.userProperties["httpSession"] as HttpSession

        if (httpSession != null) {

            LOGGER.info("websocket session id " + session.id
                    + " has opened a connection for httpsession id "
                    + httpSession!!.id)

            val totalCons = httpSession!!.getAttribute("totalConnections")

            var totalConnections = 0

            if (totalCons != null) {
                totalConnections = totalCons as Int
            }

            totalConnections++
            httpSession!!.setAttribute("totalConnections", totalConnections)

            // never to close the session on inactivity
            httpSession!!.maxInactiveInterval = -1
            LOGGER.info("httpSession.setMaxInactiveInterval(-1)")
            HeartBeatUtil.ping(httpSession!!.id)
        }

        val wffInstanceIds = session.requestParameterMap["wffInstanceId"]

        val instanceId = wffInstanceIds?.get(0)

        browserPage = BrowserPageContext.INSTANCE.webSocketOpened(instanceId)

        if (browserPage == null) {

            try {
                // or refresh the browser
                session.basicRemote.sendBinary(
                        BrowserPageAction.RELOAD.actionByteBuffer)
                session.close()
                return
            } catch (e: IOException) {
                // NOP
            }

        }

        browserPage!!.addWebSocketPushListener(session.id
        ) { data ->
            try {
                session.basicRemote.sendBinary(data)
            } catch (e: Throwable) {
                throw PushFailedException(e.message, e)
            }
        }

    }

    /**
     * When a user sends a message to the server, this method will intercept the
     * message and allow us to react to it. For now the message is read as a
     * String.
     */
    @OnMessage
    fun onMessage(message: ByteArray, session: Session) {

        browserPage!!.webSocketMessaged(message)

        if (message.size == 0) {
            LOGGER.info("client ping message.length == 0")
            if (httpSession != null && HTTP_SESSION_HEARTBEAT_INVTERVAL < System
                    .currentTimeMillis() - lastHeartbeatTime) {
                LOGGER.info("going to start httpsession hearbeat")
                HeartBeatUtil.ping(httpSession!!.id)
                lastHeartbeatTime = System.currentTimeMillis()
            }
        }
    }

    /**
     * The user closes the connection.
     *
     * Note: you can't send messages to the client from this method
     */
    @OnClose
    @Throws(IOException::class)
    fun onClose(session: Session) {

        LOGGER.info("onClose")

        // how much time you want client for inactivity
        // may be it could be the same value given for
        // session timeout in web.xml file.
        // it's valid only when the browser is closed
        // because client will be trying to reconnect.
        // The value is in seconds.
        if (httpSession != null) {

            val totalCons = httpSession!!.getAttribute("totalConnections")

            var totalConnections = 0

            if (totalCons != null) {
                totalConnections = totalCons as Int
                totalConnections--
            }

            httpSession!!.setAttribute("totalConnections", totalConnections)

            if (totalConnections == 0) {
                httpSession!!.maxInactiveInterval = ServerConstants.SESSION_TIMEOUT_SECONDS
                HeartBeatUtil.ping(httpSession!!.id)
            }

            LOGGER.info("httpSession.setMaxInactiveInterval(60 * 30)")
        }

        LOGGER.info("Session " + session.id + " closed")
        val wffInstanceIds = session.requestParameterMap["wffInstanceId"]

        val instanceId = wffInstanceIds?.get(0)
        BrowserPageContext.INSTANCE.webSocketClosed(instanceId,
                session.id)
    }

    @OnError
    fun onError(session: Session, throwable: Throwable) {
        // NOP
        // log only if required
        // if (LOGGER.isLoggable(Level.WARNING)) {
        // LOGGER.log(Level.WARNING, throwable.getMessage());
        // }
    }

    override fun requestDestroyed(sre: ServletRequestEvent) {
        httpSession = (sre.servletRequest as HttpServletRequest)
                .session
        LOGGER.info("requestDestroyed httpSession " + httpSession!!)

    }

    override fun requestInitialized(sre: ServletRequestEvent) {
        httpSession = (sre.servletRequest as HttpServletRequest)
                .session
        LOGGER.info("requestInitialized httpSession " + httpSession!!)
    }

    companion object {

        private val LOGGER = Logger
                .getLogger(WSServerForIndexPage::class.java!!.getName())

        private val HTTP_SESSION_HEARTBEAT_INVTERVAL = (ServerConstants.SESSION_TIMEOUT_MILLISECONDS - 1000 * 60 * 2).toLong()
    }
}
