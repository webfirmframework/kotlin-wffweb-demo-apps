package com.wffwebdemo.minimalproductionsample.server.servlet

import com.webfirmframework.wffweb.server.page.BrowserPageContext
import com.webfirmframework.wffweb.tag.html.attribute.core.AttributeRegistry
import com.webfirmframework.wffweb.tag.html.core.TagRegistry
import com.wffwebdemo.minimalproductionsample.page.IndexPage
import com.wffwebdemo.minimalproductionsample.page.model.DocumentModel
import com.wffwebdemo.minimalproductionsample.server.constants.ServerConstants
import java.io.IOException
import java.util.logging.Logger
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Servlet implementation class HomePageServlet
 */
/**
 * @see HttpServlet.HttpServlet
 */
@WebServlet("/index")
class IndexPageServlet : HttpServlet() {

    @Throws(ServletException::class)
    override fun init() {
        super.init()
        // optional
        TagRegistry.loadAllTagClasses()
        AttributeRegistry.loadAllAttributeClasses()
        LOGGER.info("Loaded all wffweb classes")
        ServerConstants.CONTEXT_PATH = servletContext.contextPath
    }

    /**
     * @see HttpServlet.doGet
     */
    @Throws(ServletException::class, IOException::class)
    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {

        response.contentType = "text/html;charset=utf-8"

        try {
            response.outputStream.use { os ->

                val session = request.session

                var documentModel: DocumentModel? = session.getAttribute("DOCUMENT_MODEL") as DocumentModel?

                var indexPage: IndexPage?

                if (documentModel == null) {
                    documentModel = DocumentModel()
                    documentModel.httpSession = request.session
                    session.setAttribute("DOCUMENT_MODEL", documentModel)
                }

                indexPage = IndexPage(documentModel)

                BrowserPageContext.INSTANCE.addBrowserPage(session.id, indexPage)

                indexPage.toOutputStream(os, "UTF-8")
                os.flush()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    companion object {

        private val serialVersionUID = 1L

        private val LOGGER = Logger.getLogger(IndexPageServlet::class.java.getName())
    }

}
