package com.example.lib_plugin

import org.objectweb.asm.AnnotationVisitor


class SamAnnotationVisitor(
    api: Int,
    av: AnnotationVisitor,
    val className: String?,
    val serviceMap: MutableMap<String, String>
) : AnnotationVisitor(api, av) {

    override fun visit(name: String?, value: Any?) {
        super.visit(name, value)
        className?.let {
            if (name == "register") {
                serviceMap[value as String] = it
            }
        }
    }

}