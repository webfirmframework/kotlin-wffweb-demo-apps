package com.wffwebdemo.minimalproductionsample.page.template;

import com.webfirmframework.wffweb.tag.html.AbstractHtml;

public final class ComponentUtil {

    private ComponentUtil() {
    }

    public static void buildAppHeading(AbstractHtml parent) {
        //calling Kotlin from Java
        //AppHeading is in Kotlin
        parent.appendChild(new AppHeading());
    }
}
