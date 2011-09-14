package com.curiousattemptbunny.gradle.git.version

import org.gradle.api.Plugin
import org.gradle.api.Project

class GitVersionPlugin implements Plugin<Project> {
    void apply(Project project) {
        if (project.version == Project.DEFAULT_VERSION) {
            project.rootProject.version = new GitVersion(gitDir: new File(project.rootProject.projectDir, ".git"))
        }
    }
}
