package com.sam.sopt

import com.android.build.gradle.AppExtension
import org.gradle.api.Project
import org.gradle.api.Plugin


class OptPlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {

        AppExtension appExtension = project.extensions.findByType(AppExtension.class)
        appExtension.registerTransform(new OptTransform())

    }


}