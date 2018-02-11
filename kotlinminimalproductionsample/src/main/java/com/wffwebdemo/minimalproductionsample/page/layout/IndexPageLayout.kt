package com.wffwebdemo.minimalproductionsample.page.layout

import com.webfirmframework.wffweb.tag.html.Body
import com.webfirmframework.wffweb.tag.html.Br
import com.webfirmframework.wffweb.tag.html.Html
import com.webfirmframework.wffweb.tag.html.TitleTag
import com.webfirmframework.wffweb.tag.html.attribute.*
import com.webfirmframework.wffweb.tag.html.attribute.event.ServerAsyncMethod
import com.webfirmframework.wffweb.tag.html.attribute.event.mouse.OnClick
import com.webfirmframework.wffweb.tag.html.attribute.global.Id
import com.webfirmframework.wffweb.tag.html.formsandinputs.Button
import com.webfirmframework.wffweb.tag.html.html5.attribute.Content
import com.webfirmframework.wffweb.tag.html.links.Link
import com.webfirmframework.wffweb.tag.html.metainfo.Head
import com.webfirmframework.wffweb.tag.html.metainfo.Meta
import com.webfirmframework.wffweb.tag.html.programming.Script
import com.webfirmframework.wffweb.tag.html.stylesandsemantics.Div
import com.webfirmframework.wffweb.tag.htmlwff.NoTag
import com.webfirmframework.wffweb.wffbm.data.WffBMObject
import com.wffwebdemo.minimalproductionsample.page.model.DocumentModel
import com.wffwebdemo.minimalproductionsample.page.template.ComponentUtil
import com.wffwebdemo.minimalproductionsample.page.template.SampleTemplate1
import java.util.logging.Logger

class IndexPageLayout(private val documentModel: DocumentModel) : Html(null), ServerAsyncMethod {

    init {
        super.setPrependDocType(true)
        super.setSharedData(documentModel)
        develop()
    }

    private fun develop() {

        Head(this).apply {
            TitleTag(this).apply {
                NoTag(this, "Bootstrap Example")
            }
            Meta(this,
                    Charset("utf-8"))
            Meta(this,
                    Name("viewport"),
                    Content("width=device-width, initial-scale=1"))
            Link(this,
                    Rel("stylesheet"),
                    Href("https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"))
            Script(this,
                    Src("https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js"))
            Script(this,
                    Src("https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"))
        }

        Body(this).apply {

            //Calling Java from Kotlin
            ComponentUtil.buildAppHeading(this);

            Div(this, Id("mainDivId")).apply {
                NoTag(this, "The content of the document...... ")
                Button(this, OnClick(this@IndexPageLayout)).apply {
                    NoTag(this, "Insert SampleTemplate1")
                }
                Br(this)
                Br(this)
            }
        }

    }

    override fun asyncMethod(wffBMObject: WffBMObject?, event: ServerAsyncMethod.Event): WffBMObject? {

        val tagRepository = documentModel.browserPage!!.tagRepository

        val mainDiv = tagRepository.findTagById("mainDivId")

        if (mainDiv != null) {
            LOGGER.info("SampleTemplate1 appended")
            mainDiv.appendChild(SampleTemplate1(documentModel))
            val titleTag = tagRepository.findOneTagAssignableToTag<TitleTag>(TitleTag::class.java!!)
            titleTag.addInnerHtml(NoTag(null, "Bootstrap Example | SampleTemplate1"))
        }

        return null
    }

    companion object {

        private val LOGGER = Logger.getLogger(IndexPageLayout::class.java!!.getName())
    }

}
