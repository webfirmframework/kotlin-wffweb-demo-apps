package com.wffwebdemo.minimalproductionsample.server.ws

import com.webfirmframework.wffweb.PushFailedException
import com.webfirmframework.wffweb.server.page.*
import com.webfirmframework.wffweb.server.page.action.BrowserPageAction
import com.webfirmframework.wffweb.util.ByteBufferUtil
import com.wffwebdemo.minimalproductionsample.AppSettings
import com.wffwebdemo.minimalproductionsample.page.IndexPage
import com.wffwebdemo.minimalproductionsample.page.model.DocumentModel
import com.wffwebdemo.minimalproductionsample.server.constants.ServerConstants
import java.io.IOException
import java.nio.ByteBuffer
import java.util.function.Function
import java.util.logging.Level
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

/**
 * @ServerEndpoint gives the relative name for the end point This will be
 * accessed via
 * ws://localhost:8080/minimalproductionsample/ws-for-index-page
 */
@ServerEndpoint(value = ServerConstants.INDEX_PAGE_WS_URI, configurator = WSServerForIndexPage::class)
@WebListener
class WSServerForIndexPage : ServerEndpointConfig.Configurator(), ServletRequestListener {
    private var browserPage: BrowserPage? = null
    private var httpSession: HttpSession? = null
    private var payloadProcessor: PayloadProcessor? = null

    @Volatile
    private var heartbeatManager: HeartbeatManager? = null
    override fun modifyHandshake(config: ServerEndpointConfig,
                                 request: HandshakeRequest, response: HandshakeResponse) {
        val parameterMap = request
                .parameterMap
        var httpSession: HttpSession? = null
        val wffInstanceIds = parameterMap[BrowserPage.WFF_INSTANCE_ID]
        val instanceId = wffInstanceIds!![0]
        browserPage = BrowserPageContext.INSTANCE.webSocketOpened(instanceId)
        var documentModel: DocumentModel? = null
        if (browserPage is IndexPage) {
            val indexPage = browserPage as IndexPage
            documentModel = indexPage.documentModel
            httpSession = documentModel.httpSession
        }
        super.modifyHandshake(config, request, response)

        // in a worst case if the httpSession is null
        if (httpSession == null) {
            LOGGER.info(
                    "httpSession == null after modifyHandshake so httpSession = (HttpSession) request.getHttpSession()")
            httpSession = request.httpSession as HttpSession
        }
        if (httpSession == null) {
            LOGGER.info("httpSession == null")
            return
        }
        config.userProperties["httpSession"] = httpSession
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
        httpSession = config.userProperties["httpSession"] as HttpSession?
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
            val hbm: HeartbeatManager = HeartbeatRunnable.HEARTBEAT_MANAGER_MAP.computeIfAbsent(httpSession!!.id,
                    Function<String, HeartbeatManager> { k: String? ->
                        HeartbeatManager(AppSettings.CACHED_THREAD_POOL,
                                HTTP_SESSION_HEARTBEAT_INTERVAL, HeartbeatRunnable(httpSession!!.id))
                    })
            heartbeatManager = hbm
            hbm.runAsync()
        }
        val wffInstanceIds = session.requestParameterMap["wffInstanceId"]!!
        val instanceId = wffInstanceIds[0]
        browserPage = BrowserPageContext.INSTANCE.webSocketOpened(instanceId)
        if (browserPage == null) {
            try {
                // or refresh the browser
                session.basicRemote.sendBinary(
                        BrowserPageAction.RELOAD.actionByteBuffer)
                session.close()
            } catch (e: IOException) {
                // NOP
            }
            return
        }

        // Internally it may contain a volatile variable
        // so it's better to declare a dedicated variable before
        // addWebSocketPushListener.
        // If the maxBinaryMessageBufferSize is changed dynamically
        // then call getMaxBinaryMessageBufferSize method directly in
        // sliceIfRequired method as second argument.
        val maxBinaryMessageBufferSize = session
                .maxBinaryMessageBufferSize

        // NB: do not use browserPage.getPayloadProcessor it has bug
        // payloadProcessor = browserPage.getPayloadProcessor();
        payloadProcessor = PayloadProcessor(browserPage)
        browserPage!!.addWebSocketPushListener(session.id, WebSocketPushListener { data: ByteBuffer? ->
            ByteBufferUtil.sliceIfRequired(data, maxBinaryMessageBufferSize
            ) { part: ByteBuffer?, last: Boolean ->
                try {
                    session.basicRemote.sendBinary(part, last)
                } catch (e: IOException) {
                    LOGGER.log(Level.SEVERE,
                            "IOException while session.getBasicRemote().sendBinary(part, last)",
                            e)
                    try {
                        session.close()
                    } catch (e1: IOException) {
                        LOGGER.log(Level.SEVERE,
                                "IOException while session.close()",
                                e1)
                    }
                    throw PushFailedException(e.message, e)
                }
                !last
            }
        })
    }

    /**
     * When a user sends a message to the server, this method will intercept the
     * message and allow us to react to it. For now the message is read as a
     * String.
     */
    @OnMessage
    fun onMessage(message: ByteBuffer, last: Boolean, session: Session?) {
        payloadProcessor!!.webSocketMessaged(message, last)
        if (last && message.capacity() == 0) {
            LOGGER.info("client ping message.length == 0")
            if (httpSession != null) {
                LOGGER.info("going to start httpsession hearbeat")
                val hbm = heartbeatManager
                hbm?.runAsync()
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
                val hbm = heartbeatManager
                hbm?.runAsync()
            }
            LOGGER.info("httpSession.setMaxInactiveInterval(60 * 30)")
        }
        LOGGER.info("Session " + session.id + " closed")
        val wffInstanceIds = session.requestParameterMap["wffInstanceId"]!!
        val instanceId = wffInstanceIds[0]
        BrowserPageContext.INSTANCE.webSocketClosed(instanceId,
                session.id)
        payloadProcessor = null
    }

    @OnError
    fun onError(session: Session?, throwable: Throwable?) {
        // NOP
        // log only if required
        // if (LOGGER.isLoggable(Level.WARNING)) {
        // LOGGER.log(Level.WARNING, throwable.getMessage());
        // }
    }

    override fun requestDestroyed(sre: ServletRequestEvent) {
        httpSession = (sre.servletRequest as HttpServletRequest)
                .session
        LOGGER.info("requestDestroyed httpSession $httpSession")
    }

    override fun requestInitialized(sre: ServletRequestEvent) {
        httpSession = (sre.servletRequest as HttpServletRequest)
                .session
        LOGGER.info("requestInitialized httpSession $httpSession")
    }

    companion object {
        private val LOGGER = Logger
                .getLogger(WSServerForIndexPage::class.java.name)
        private val HTTP_SESSION_HEARTBEAT_INTERVAL = (ServerConstants.SESSION_TIMEOUT_MILLISECONDS
                - 1000 * 60 * 2).toLong()
    }
}