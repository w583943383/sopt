package com.example.lib_plugin

import com.example.lib_annotation.ModuleService
import org.objectweb.asm.*
import sun.rmi.runtime.Log

class SamClassVisitor(v: Int, cv: ClassVisitor) : ClassVisitor(v, cv) {

    val serviceMap = mutableMapOf<String,String>()
    private var className: String? = null


    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        className = name
    }


    override fun visitField(
        access: Int,
        name: String?,
        desc: String?,
        signature: String?,
        value: Any?
    ): FieldVisitor {
        return super.visitField(access, name, desc, signature, value)
    }


    override fun visitMethod(
        access: Int,
        name: String?,
        desc: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        return super.visitMethod(access, name, desc, signature, exceptions)
    }

    override fun visitTypeAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        desc: String?,
        visible: Boolean
    ): AnnotationVisitor {
        val annotationVisitor = super.visitTypeAnnotation(typeRef, typePath, desc, visible)
        if (Type.getDescriptor(ModuleService::class.java) == desc) {
            return SamAnnotationVisitor(Opcodes.ASM5,annotationVisitor,className,serviceMap)
        }
        return annotationVisitor
    }

    override fun visitEnd() {
        super.visitEnd()
    }


}