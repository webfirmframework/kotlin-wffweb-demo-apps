package com.wffwebdemo.minimalproductionsample.page.template

import com.webfirmframework.wffweb.tag.html.AbstractHtml
import com.webfirmframework.wffweb.tag.html.attribute.AttributeNameConstants
import com.webfirmframework.wffweb.tag.html.attribute.Value
import com.webfirmframework.wffweb.tag.html.attribute.core.AbstractAttribute
import com.webfirmframework.wffweb.tag.html.attribute.event.form.OnChange
import com.webfirmframework.wffweb.tag.html.formsandinputs.Input

class StatefulInput(base: AbstractHtml?, vararg attributes: AbstractAttribute) : Input(base, *attributes) {

    private var valueAttr: Value? = null

    init {
        develop()
    }

    private fun develop() {

        if (super.getAttributeByName(AttributeNameConstants.ONCHANGE) != null) {
            throw IllegalArgumentException("OnChange attribute should not be explicitly given in this tag.")
        }

        valueAttr = super.getAttributeByName(AttributeNameConstants.VALUE) as Value?
        valueAttr = if (valueAttr != null) valueAttr else Value("")

        val onChange = OnChange("return true;",
                { bm, _ ->
                    val value = bm.getValue("attrValue") as String
                    //updateClient must be false to avoid synching the changes to the client browser page
                    valueAttr!!.setValue(false, value)
                    null
                },
                "return {attrValue: source.value};", null)


        super.addAttributes(valueAttr, onChange)
    }

    val value: String
        get() = valueAttr!!.value
}