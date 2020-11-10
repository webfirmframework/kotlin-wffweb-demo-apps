package com.wffwebdemo.minimalproductionsample.page.template

import com.webfirmframework.wffweb.tag.html.H2
import com.webfirmframework.wffweb.tag.html.attribute.For
import com.webfirmframework.wffweb.tag.html.attribute.Name
import com.webfirmframework.wffweb.tag.html.attribute.Type
import com.webfirmframework.wffweb.tag.html.attribute.event.ServerAsyncMethod
import com.webfirmframework.wffweb.tag.html.attribute.event.form.OnSubmit
import com.webfirmframework.wffweb.tag.html.attribute.global.ClassAttribute
import com.webfirmframework.wffweb.tag.html.attribute.global.Id
import com.webfirmframework.wffweb.tag.html.formsandinputs.Button
import com.webfirmframework.wffweb.tag.html.formsandinputs.Form
import com.webfirmframework.wffweb.tag.html.formsandinputs.Input
import com.webfirmframework.wffweb.tag.html.formsandinputs.Label
import com.webfirmframework.wffweb.tag.html.html5.attribute.AutoComplete
import com.webfirmframework.wffweb.tag.html.html5.attribute.Placeholder
import com.webfirmframework.wffweb.tag.html.stylesandsemantics.Div
import com.webfirmframework.wffweb.tag.htmlwff.NoTag
import com.webfirmframework.wffweb.wffbm.data.BMValueType
import com.webfirmframework.wffweb.wffbm.data.WffBMObject
import com.wffwebdemo.minimalproductionsample.page.model.DocumentModel

class SampleTemplate2(private val documentModel: DocumentModel) : Div(null), ServerAsyncMethod {

    init {
        develop()
    }

    private fun develop() {
        changeTitle()

        val classAttribute10 = ClassAttribute("form-group")
        val classAttribute13 = ClassAttribute("form-control")

        H2(this).apply {
            NoTag(this, "Vertical (basic) form")
        }
        Form(this,
                OnSubmit("event.preventDefault(); return true;",
                        { bm, _ ->

                            val email = bm.getValue("email") as String

                            val pwd = bm.getValue("pwd") as String

                            println("email: " + email + ", pwd: " + pwd)

                            val result = WffBMObject();
                            result.put("msg", BMValueType.STRING,
                                    "message from server")

                            result;
                        }, "return {email: email.value, pwd: pwd.value}",
                        "if (jsObject && jsObject.msg) {alert(jsObject.msg);}")).apply {

            Div(this,
                    classAttribute10).apply {
                Label(this,
                        For("email")).apply {
                    NoTag(this, "Email:")
                }
                Input(this,
                        Type(Type.EMAIL),
                        classAttribute13,
                        Id("email"),
                        Placeholder("Enter email"),
                        Name("email"),
                        AutoComplete(AutoComplete.EMAIL))
            }
            Div(this,
                    classAttribute10).apply {
                Label(this,
                        For("pwd")).apply {
                    NoTag(this, "Password:")
                }
                Input(this,
                        Type(Type.PASSWORD),
                        classAttribute13,
                        Id("pwd"),
                        Placeholder("Enter password"),
                        Name("pwd"))
            }
            Div(this,
                    ClassAttribute("checkbox")).apply {
                Label(this).apply {
                    Input(this,
                            Type("checkbox"),
                            Name("remember"))
                    NoTag(this, " Remember me")
                }
            }
            Button(this,
                    Type("submit"),
                    ClassAttribute("btn btn-default")).apply {
                NoTag(this, "Submit")
            }
        }
    }

    private fun changeTitle() {
        // getTagRepository() will give object only if the browserPage.render is returned
        val tagRepository = documentModel.browserPage!!.tagRepository
        if (tagRepository != null) {
            val title = tagRepository.findTagById("windowTitleId")
            title?.addInnerHtml(NoTag(null, "SampleTemplate2"))
        }
    }

    override fun asyncMethod(wffBMObject: WffBMObject?, event: ServerAsyncMethod.Event): WffBMObject? {
        this.insertBefore(SampleTemplate1(documentModel))
        this.parent.removeChild(this)

        return null
    }
}
