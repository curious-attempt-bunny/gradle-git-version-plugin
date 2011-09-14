package com.curiousattemptbunny.gradle.git.version

import org.gradle.api.GradleException

class GitVersion {

    Closure format = { (branch != 'master' ? "$branch-" : "") + "${revisions}.${head}"}
    String branchEnvironmentKey = 'GIT_BRANCH'
    File gitDir

    GitVersion self = this

    @Lazy String stringRepresentation = {
        def f = format.clone()
        f.delegate = self
        f()
    }()

    @Lazy def branchParts = {
        checkGitDir()
        def branchLine = (["git", "--git-dir=${gitDir.absolutePath}", "branch", "-v", "--abbrev=40"] as String[]).execute().text.split("\n").find { it.startsWith("*") }
        def m = branchLine =~ /\*\s(.+?)\s+([0-9a-f]{40})\s.*/
        m[0]
    }()

    @Lazy def branch = {
        if (branchParts[1] == '(no branch)') {
            if (branchEnvironmentKey && System.env[branchEnvironmentKey]) {
                return System.env[branchEnvironmentKey]
            }
            return 'NO_BRANCH'
        }
        return branchParts[1]
    }()

    @Lazy def headLong = {
        branchParts[2]
    }()

    @Lazy def head = {
        branchParts[2][0..6]
    }()

    @Lazy def revisions = {
        checkGitDir()
        def revisionList = (["git", "--git-dir=${gitDir.absolutePath}", "rev-list", branch, "--"] as String[]).execute().text.split("\n")
        def search = true
        def laterCommits = revisionList.findAll {
                search = search && it != headLong ; search
        }
        revisionList.size() - laterCommits.size()
    }()

    private def checkGitDir() {
        if (!gitDir || !gitDir.exists() || !gitDir.isDirectory()) {
            throw new GradleException("You must specify the GitVersion.gitDir as this directory does not exist: "+gitDir?.absolutePath)
        }
    }

    String toString() {
        stringRepresentation
    }
}
