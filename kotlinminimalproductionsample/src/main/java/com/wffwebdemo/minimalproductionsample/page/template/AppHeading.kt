package com.wffwebdemo.minimalproductionsample.page.template

import com.webfirmframework.wffweb.tag.html.H1
import com.webfirmframework.wffweb.tag.html.attribute.global.Style
import com.webfirmframework.wffweb.tag.html.stylesandsemantics.Div
import com.webfirmframework.wffweb.tag.htmlwff.NoTag


class AppHeading : Div {

    constructor() : super(null, Style("background: green")) {

        H1(this).run {
            NoTag(this, "Kotlin + Java wffweb sample project")
        }

    }

}
