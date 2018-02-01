package com.wffwebdemo.minimalproductionsample.page

import com.webfirmframework.wffweb.server.page.BrowserPage
import com.webfirmframework.wffweb.tag.html.AbstractHtml
import com.webfirmframework.wffweb.tag.htmlwff.NoTag
import com.webfirmframework.wffweb.tag.repository.TagRepository
import com.wffwebdemo.minimalproductionsample.page.layout.IndexPageLayout
import com.wffwebdemo.minimalproductionsample.page.model.DocumentModel
import com.wffwebdemo.minimalproductionsample.server.constants.ServerConstants
import java.io.IOException
import java.io.OutputStream
import java.util.logging.Logger

class IndexPage(val documentModel: DocumentModel) : BrowserPage() {

    private var indexPageLayout: IndexPageLayout? = null

    private var mainDiv: AbstractHtml? = null

    private var mainDivChildren: List<AbstractHtml>? = null

    override fun webSocketUrl(): String {
        return ServerConstants.DOMAIN_WS_URL + (ServerConstants.CONTEXT_PATH + ServerConstants.INDEX_PAGE_WS_URI)
    }

    override fun render(): AbstractHtml {
        super.setWebSocketHeartbeatInterval(HEARTBEAT_TIME_MILLISECONDS)
        super.setWebSocketReconnectInterval(WS_RECONNECT_TIME)

        documentModel.browserPage = this

        indexPageLayout = IndexPageLayout(documentModel)

        mainDiv = TagRepository.findTagById("mainDivId", indexPageLayout!!)

        // to remove main div and to insert "Loading..." before rendering
        if (mainDiv != null) {
            mainDivChildren = mainDiv!!.children
            mainDiv!!.addInnerHtml(NoTag(null, "Loading..."))
        }

        return indexPageLayout as IndexPageLayout
    }

    @Throws(IOException::class)
    override fun toOutputStream(os: OutputStream, charset: String): Int {
        val outputStream = super.toOutputStream(os, charset)
        // to restore main div in the body
        // this makes the main div to be inserted via websocket communication
        if (mainDiv != null && mainDivChildren != null) {
            mainDiv!!.addInnerHtmls(*mainDivChildren!!
                    .toTypedArray<AbstractHtml>())
        }
        return outputStream
    }

    companion object {

        private val serialVersionUID = 1L

        private val LOGGER = Logger
                .getLogger(IndexPage::class.java!!.getName())

        // this is a standard interval
        val HEARTBEAT_TIME_MILLISECONDS = 25000

        private val WS_RECONNECT_TIME = 1000
    }

}
