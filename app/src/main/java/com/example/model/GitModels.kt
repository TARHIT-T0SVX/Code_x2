package com.example.model

import java.util.UUID

data class GitCommit(
    val id: String = UUID.randomUUID().toString().take(7),
    val hash: String = UUID.randomUUID().toString().take(8),
    val message: String,
    val author: String = "Developer",
    val timestamp: Long = System.currentTimeMillis(),
    val branch: String = "main",
    val filesChangedCount: Int = 1
)

data class GitBranch(
    val name: String,
    val isCurrent: Boolean = false,
    val isRemote: Boolean = false
)

data class WorkingFileChange(
    val file: CodeFile,
    val changeType: ChangeType = ChangeType.MODIFIED,
    val isStaged: Boolean = false
)

enum class ChangeType {
    ADDED,
    MODIFIED,
    DELETED,
    CONFLICT
}

data class MergeConflict(
    val fileName: String,
    val currentContent: String,
    val incomingContent: String,
    val resolvedContent: String = ""
)
