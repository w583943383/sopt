package com.example.lib_plugin

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.HashMap
import java.util.jar.JarEntry
import java.util.jar.JarFile


class SamTransform : Transform() {

    private var serviceMap = mutableMapOf<String, String>()


    override fun getName(): String {
        return SamTransform::class.java.name
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return true
    }

    override fun transform(transformInvocation: TransformInvocation?) {
        super.transform(transformInvocation)
        transformInvocation?.inputs?.forEach { transformInput ->
            //directoryInputs为文件夹中的class文件，而jarInputs为jar包中的class文件
            transformInput.directoryInputs.forEach { directoryInput ->
                if (directoryInput.file.isDirectory) {
                    handlerDirectoryInputs(directoryInput.file)
                }
                //处理完输入文件之后，要把输出给下一个任务
                val dest = transformInvocation?.outputProvider.getContentLocation(
                    directoryInput.name, directoryInput.contentTypes, directoryInput.scopes,
                    Format.DIRECTORY
                )
                FileUtils.copyDirectory(directoryInput.file, dest)
            }

            transformInput.jarInputs.forEach {
                val filePath = it.file.absolutePath
                if (filePath.endsWith(".jar")) {
                    val jarFile = JarFile(filePath)
                    val enumeration = jarFile.entries()
                    while (enumeration.hasMoreElements()) {
                        val jarEntry = enumeration.nextElement() as JarEntry
                        val entryName = jarEntry.name
                        if (entryName.endsWith(".class")) {
                            handleClassInputStream(jarFile.getInputStream(jarEntry))
                        }
                    }
                    jarFile.close()
                }
                var jarName = it.name
                val md5Name = DigestUtils.md5Hex(it.file.absolutePath)
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length - 4)
                }
                val dest = transformInvocation?.outputProvider.getContentLocation(jarName+md5Name,it.contentTypes,it.scopes,Format.JAR)
                FileUtils.copyDirectory(it.file,dest)
            }





        }

    }

    private fun handlerDirectoryInputs(file: File) {
        if (file.isDirectory) {
            file.listFiles().forEach {
                handlerDirectoryInputs(file)
            }
        } else {
            if (file.name.endsWith(".class")) {
                handleClassInputStream(FileInputStream(file))
            }
        }

    }


    private fun handleClassInputStream(inputStream: InputStream) {
        val classReader = ClassReader(inputStream)
        //COMPUTE_MAXS 告诉 ASM 自动计算栈的最大值以及最大数量的方法的本地变量。COMPUTE_FRAMES 标识让 ASM 自动计算方法的栈桢
        val classWriter = ClassWriter(classReader,ClassWriter.COMPUTE_MAXS)
        val classVisitor = SamClassVisitor(Opcodes.ASM5,classWriter)
        classReader.accept(classVisitor,ClassReader.EXPAND_FRAMES)
        serviceMap.putAll(classVisitor.serviceMap)
        inputStream.close()
    }



}
