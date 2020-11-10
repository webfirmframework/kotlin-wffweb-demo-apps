package com.wffwebdemo.minimalproductionsample.server.servlet

import java.io.IOException
import java.util.logging.Logger
import javax.servlet.ServletException
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * heart beat servlet
 */
/**
 * @see HttpServlet.HttpServlet
 */
@WebServlet("/heart-beat")
class HeartBeatServlet : HttpServlet() {

    /**
     * @see HttpServlet.doGet
     */
    @Throws(ServletException::class, IOException::class)
    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {

        LOGGER.info("heat-beat request received")

        response.contentType = "text/html;charset=utf-8"

        response.outputStream.use { os ->

            request.getSession(false)
            os.write(ByteArray(0))
            os.flush()
        }

    }

    companion object {

        private val serialVersionUID = 1L

        private val LOGGER = Logger.getLogger(HeartBeatServlet::class.java.getName())
    }

}
