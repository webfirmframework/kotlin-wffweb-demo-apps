package com.wffwebdemo.minimalproductionsample.page.template

import com.webfirmframework.wffweb.tag.html.Br
import com.webfirmframework.wffweb.tag.html.attribute.event.ServerAsyncMethod
import com.webfirmframework.wffweb.tag.html.attribute.event.mouse.OnClick
import com.webfirmframework.wffweb.tag.html.formsandinputs.Button
import com.webfirmframework.wffweb.tag.html.stylesandsemantics.Div
import com.webfirmframework.wffweb.tag.htmlwff.NoTag
import com.webfirmframework.wffweb.wffbm.data.WffBMObject
import com.wffwebdemo.minimalproductionsample.page.model.DocumentModel

class SampleTemplate1(private val documentModel: DocumentModel) : Div(null), ServerAsyncMethod {

    init {
        develop()
    }

    private fun develop() {
        changeTitle()

        NoTag(this, "This is SampleTemplate1 ")

        Br(this)

        Button(this, OnClick(this@SampleTemplate1)).run {
            NoTag(this, "Click Me to change to SampleTemplate2")
        }
        Br(this)
        Br(this)
    }

    private fun changeTitle() {
        // getTagRepository() will give object only if the browserPage.render is returned
        val tagRepository = documentModel.browserPage!!.tagRepository
        if (tagRepository != null) {
            val title = tagRepository.findTagById("windowTitleId")
            title?.addInnerHtml(NoTag(null, "SampleTemplate1"))
        }
    }

    override fun asyncMethod(wffBMObject: WffBMObject?, event: ServerAsyncMethod.Event): WffBMObject? {

        this.insertBefore(SampleTemplate2(documentModel))
        this.parent.removeChild(this)



        return null
    }
}
