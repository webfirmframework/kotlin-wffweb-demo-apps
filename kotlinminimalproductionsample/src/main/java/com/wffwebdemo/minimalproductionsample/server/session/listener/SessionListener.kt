package com.wffwebdemo.minimalproductionsample.server.session.listener

import com.webfirmframework.wffweb.server.page.BrowserPageContext
import com.wffwebdemo.minimalproductionsample.page.IndexPage
import java.util.logging.Logger
import javax.servlet.annotation.WebListener
import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener

@WebListener
class SessionListener : HttpSessionListener {

    override fun sessionCreated(sessionEvent: HttpSessionEvent) {
        LOGGER.info("SessionListener.sessionCreated()")
        // NOP
    }

    override fun sessionDestroyed(sessionEvent: HttpSessionEvent) {
        LOGGER.info("SessionListener.sessionDestroyed()")

        val session = sessionEvent.session
        val attrValue = session.getAttribute("indexPageInstanceId")

        if (attrValue != null) {
            val indexPageInstanceId = attrValue.toString()
            val indexPage = BrowserPageContext.INSTANCE
                    .getBrowserPage(indexPageInstanceId) as IndexPage

        }

        BrowserPageContext.INSTANCE.httpSessionClosed(session.id)
    }

    companion object {

        private val LOGGER = Logger
                .getLogger(SessionListener::class.java.getName())
    }

}
