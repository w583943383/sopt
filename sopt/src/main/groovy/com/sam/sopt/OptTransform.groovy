package com.sam.sopt

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager

import javassist.ClassPool
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

class OptTransform extends Transform {


    private ClassPool mClassPool = new ClassPool()

    private Project mProject
    private AppExtension mAppExtension


    public OptTransform(Project project, AppExtension appExtension) {
        mProject = project
        mAppExtension = appExtension
    }

    /**
     * 返回当前 Transform 唯一的名称。
     */
    @Override
    String getName() {
        return getClass().getSimpleName()
    }

    /**
     * 返回当前 Transform 需要的输入的类型。
     * ContentType 常用的类型有：
     * CLASSES:  编译好的.class文件
     * RESOURCES:  原始的Java文件
     * NATIVE_LIBS:  C/C++库
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 返回当前 Transform 应用的范围。
     * Scope 常用的类型 PROJECT、SUB_PROJECT、EXTERNAL_LIBRARIES 等。
     * 通常返回常量集合 SCOPE_FULL_PROJECT 即可。
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }


    /**
     * 当前 Transform 是否支持增量编译。
     */
    @Override
    boolean isIncremental() {
        return false
    }

    /**
     * Tansform 的实现类最重要的方法，用于做具体的数据转换。
     * 可以通过参数 transformInvocation 得到所有的 .class 等输入。
     */
    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)

        Collection<TransformInput> inputs = transformInvocation.getInputs()

        mClassPool.appendSystemPath()


        TransformOutputProvider outputProvider = transformInvocation.outputProvider
        List<File> bootClasses = mAppExtension?.bootClasspath
        if (bootClasses != null) {
            for (File file : bootClasses) {
                mClassPool.appendClassPath(file.absolutePath)
            }
        }
        for (TransformInput transformInput : inputs) {
            for (JarInput jarInput : transformInput.jarInputs) {
                mClassPool.appendClassPath(jarInput.file.absolutePath)
            }
            for (DirectoryInput directoryInput : transformInput.directoryInputs) {
                mClassPool.appendClassPath(directoryInput.file.absolutePath)
            }
        }


        for (TransformInput transformInput : inputs) {

            for (DirectoryInput directoryInput : transformInput.directoryInputs) {
                handleDirectoryInput(directoryInput)
            }

            for (JarInput jarInput : transformInput.jarInputs) {
                File file = outputProvider.getContentLocation(jarInput.file.absolutePath,
                        jarInput.contentTypes,
                        jarInput.scopes,
                        Format.JAR)
                handleJarInputs(jarInput)

            }

        }


    }


    void handleDirectoryInput(DirectoryInput directoryInput){

        if(directoryInput.file.isDirectory()){
            directoryInput.file.eachFileRecurse {File file->
                def name  = file.name
                println '-----------DirectoryInput class file name  <' + name + '> -----------'
                if(name.endsWith(".class")
                        && !name.startsWith("R\$")
                        && "R.class" != name
                        && "BuildConfig.class" != name){
                    ClassReader classReader = new ClassReader(file.bytes)
                    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                    ClassVisitor cv = new CrashOptClassVisitor(classWriter)

                }
            }
        }
    }


    void handleJarInputs(JarInput jarInput){

    }
}