package com.example.lib_plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project


class SamPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.all {
            when (it) {
                is AppPlugin -> {
                    val android = project.extensions.getByType(AppExtension::class.java)
                    android.registerTransform(SamTransform())
                }
            }
        }
    }
}
