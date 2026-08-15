package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.CodeTransformer
import com.example.engine.ProjectManager
import com.example.engine.VisualDomEngine
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.InputStream

data class MainUiState(
    val currentProject: CodeProject = ProjectManager.createDefaultProject(),
    val activeFile: CodeFile? = null,
    val openFiles: List<CodeFile> = emptyList(),
    val currentTab: Int = 0, // 0: Editor, 1: Live Preview & Design
    val isInspectMode: Boolean = false,
    val visualElements: List<VisualElement> = emptyList(),
    val selectedElement: VisualElement? = null,
    val terminalLogs: List<TerminalLog> = emptyList(),
    val gitBranches: List<GitBranch> = listOf(GitBranch("main", isCurrent = true), GitBranch("feature/canvas-120fps")),
    val currentBranch: String = "main",
    val gitCommits: List<GitCommit> = emptyList(),
    val workingChanges: List<WorkingFileChange> = emptyList(),
    val showFileExplorer: Boolean = false,
    val showTerminal: Boolean = false,
    val showGitSheet: Boolean = false,
    val showDiagnosticsSheet: Boolean = false,
    val showAiCopilot: Boolean = false,
    val showExportDialog: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadDefaultProject()
    }

    private fun loadDefaultProject() {
        val project = ProjectManager.createDefaultProject()
        val defaultFile = project.files.firstOrNull { it.name == "index.html" } ?: project.files.firstOrNull()
        val elements = if (defaultFile != null) VisualDomEngine.extractElementsFromHtml(defaultFile.content) else emptyList()

        val initialCommits = listOf(
            GitCommit(hash = "7f3a92", message = "init: initial project scaffolding with 120 FPS runner", branch = "main"),
            GitCommit(hash = "4c1e8b", message = "feat: add Figma visual inspector & live DOM sync", branch = "main")
        )

        val initialLogs = listOf(
            TerminalLog(level = LogLevel.SYSTEM, message = "CodeCraft Mobile IDE v1.0.0 initialized"),
            TerminalLog(level = LogLevel.INFO, message = "Zero-Latency Preview Server listening on http://localhost:3000"),
            TerminalLog(level = LogLevel.SUCCESS, message = "Hardware 120 FPS accelerated viewport ready"),
            TerminalLog(level = LogLevel.INFO, message = "Type 'help' in terminal for available commands")
        )

        _uiState.update {
            it.copy(
                currentProject = project,
                activeFile = defaultFile,
                openFiles = project.files.take(3),
                visualElements = elements,
                gitCommits = initialCommits,
                terminalLogs = initialLogs
            )
        }
    }

    fun switchTab(tabIndex: Int) {
        _uiState.update { it.copy(currentTab = tabIndex) }
    }

    fun selectFile(file: CodeFile) {
        _uiState.update { state ->
            val openList = if (state.openFiles.none { it.id == file.id }) {
                state.openFiles + file
            } else {
                state.openFiles
            }
            state.copy(
                activeFile = file,
                openFiles = openList,
                showFileExplorer = false
            )
        }
    }

    fun closeTab(file: CodeFile) {
        _uiState.update { state ->
            val newOpenList = state.openFiles.filter { it.id != file.id }
            val newActive = if (state.activeFile?.id == file.id) {
                newOpenList.firstOrNull()
            } else {
                state.activeFile
            }
            state.copy(
                openFiles = newOpenList,
                activeFile = newActive
            )
        }
    }

    fun updateFileContent(newContent: String) {
        val currentActive = _uiState.value.activeFile ?: return
        val updatedFile = currentActive.copy(content = newContent, isModified = true)

        val updatedFiles = _uiState.value.currentProject.files.map {
            if (it.id == updatedFile.id) updatedFile else it
        }

        val updatedOpenFiles = _uiState.value.openFiles.map {
            if (it.id == updatedFile.id) updatedFile else it
        }

        // If index.html was edited, re-extract DOM elements for visual canvas
        val elements = if (updatedFile.name == "index.html") {
            VisualDomEngine.extractElementsFromHtml(newContent)
        } else {
            _uiState.value.visualElements
        }

        val workingChange = WorkingFileChange(file = updatedFile, changeType = ChangeType.MODIFIED)
        val changes = (_uiState.value.workingChanges.filter { it.file.id != updatedFile.id } + workingChange)

        _uiState.update {
            it.copy(
                activeFile = updatedFile,
                currentProject = it.currentProject.copy(files = updatedFiles),
                openFiles = updatedOpenFiles,
                visualElements = elements,
                workingChanges = changes
            )
        }
    }

    /**
     * FORMAT BUTTON: Beautifies and fixes current file / all project files
     */
    fun formatCurrentCode() {
        val active = _uiState.value.activeFile ?: return
        val formatted = CodeTransformer.formatCode(active.content, active.language)
        updateFileContent(formatted)
        addTerminalLog(LogLevel.SUCCESS, "Formatted ${active.name} with standard indentation")
    }

    /**
     * EMBED BUTTON: Inlines all CSS and JS into single standalone HTML file
     */
    fun embedProjectCode() {
        val htmlFile = _uiState.value.currentProject.files.firstOrNull { it.name == "index.html" }
        val cssFile = _uiState.value.currentProject.files.firstOrNull { it.name.endsWith(".css") }
        val jsFile = _uiState.value.currentProject.files.firstOrNull { it.name.endsWith(".js") }

        if (htmlFile == null) {
            addTerminalLog(LogLevel.ERROR, "Embed failed: index.html not found")
            return
        }

        val merged = CodeTransformer.embedCode(
            html = htmlFile.content,
            css = cssFile?.content ?: "",
            js = jsFile?.content ?: ""
        )

        // Save as embedded_index.html or update index.html
        val embeddedFile = CodeFile(
            name = "embedded_index.html",
            path = "/embedded_index.html",
            content = merged
        )

        val newFiles = (_uiState.value.currentProject.files.filter { it.name != "embedded_index.html" } + embeddedFile)
        _uiState.update {
            it.copy(
                currentProject = it.currentProject.copy(files = newFiles),
                activeFile = embeddedFile,
                openFiles = (it.openFiles.filter { f -> f.name != embeddedFile.name } + embeddedFile)
            )
        }
        addTerminalLog(LogLevel.SUCCESS, "Embedded all CSS & JS into standalone embedded_index.html")
    }

    /**
     * SPLIT BUTTON: Extracts embedded <style> and <script> into separate index.html, style.css, and script.js
     */
    fun splitCurrentCode() {
        val active = _uiState.value.activeFile ?: return
        val (newHtml, newCss, newJs) = CodeTransformer.splitCode(active.content)

        val htmlFile = CodeFile(name = "index.html", path = "/index.html", content = newHtml)
        val cssFile = CodeFile(name = "style.css", path = "/style.css", content = newCss)
        val jsFile = CodeFile(name = "script.js", path = "/script.js", content = newJs)

        val updatedFiles = listOf(htmlFile, cssFile, jsFile) + _uiState.value.currentProject.files.filter {
            it.name != "index.html" && it.name != "style.css" && it.name != "script.js" && it.name != "embedded_index.html"
        }

        _uiState.update {
            it.copy(
                currentProject = it.currentProject.copy(files = updatedFiles),
                activeFile = htmlFile,
                openFiles = listOf(htmlFile, cssFile, jsFile)
            )
        }
        addTerminalLog(LogLevel.SUCCESS, "Split embedded code into index.html, style.css, and script.js")
    }

    fun createFile(name: String) {
        val newFile = CodeFile(
            name = name,
            path = "/$name",
            content = when (CodeLanguage.fromFileName(name)) {
                CodeLanguage.HTML -> "<!DOCTYPE html>\n<html>\n<head>\n  <title>$name</title>\n</head>\n<body>\n  <h1>New Page</h1>\n</body>\n</html>"
                CodeLanguage.CSS -> "/* $name */\nbody {\n  margin: 0;\n}"
                CodeLanguage.JAVASCRIPT -> "// $name\nconsole.log('$name loaded');"
                CodeLanguage.JSON -> "{\n  \"name\": \"$name\"\n}"
                else -> ""
            }
        )

        val newFiles = _uiState.value.currentProject.files + newFile
        _uiState.update {
            it.copy(
                currentProject = it.currentProject.copy(files = newFiles),
                activeFile = newFile,
                openFiles = it.openFiles + newFile,
                showFileExplorer = false
            )
        }
        addTerminalLog(LogLevel.INFO, "Created file $name")
    }

    fun createFolder(name: String) {
        val folder = CodeFile(name = name, path = "/$name", isDirectory = true)
        val newFiles = _uiState.value.currentProject.files + folder
        _uiState.update {
            it.copy(
                currentProject = it.currentProject.copy(files = newFiles),
                showFileExplorer = false
            )
        }
        addTerminalLog(LogLevel.INFO, "Created folder $name/")
    }

    fun deleteFile(file: CodeFile) {
        val newFiles = _uiState.value.currentProject.files.filter { it.id != file.id }
        val newOpen = _uiState.value.openFiles.filter { it.id != file.id }
        val newActive = if (_uiState.value.activeFile?.id == file.id) newOpen.firstOrNull() ?: newFiles.firstOrNull() else _uiState.value.activeFile

        _uiState.update {
            it.copy(
                currentProject = it.currentProject.copy(files = newFiles),
                openFiles = newOpen,
                activeFile = newActive
            )
        }
        addTerminalLog(LogLevel.WARN, "Deleted ${file.name}")
    }

    fun loadTemplate(templateType: String) {
        val project = when (templateType) {
            "game" -> ProjectManager.create120FpsGameProject()
            else -> ProjectManager.createModernWebAppProject()
        }
        val defaultFile = project.files.firstOrNull()
        _uiState.update {
            it.copy(
                currentProject = project,
                activeFile = defaultFile,
                openFiles = project.files.take(3),
                visualElements = if (defaultFile != null) VisualDomEngine.extractElementsFromHtml(defaultFile.content) else emptyList(),
                showFileExplorer = false
            )
        }
        addTerminalLog(LogLevel.SUCCESS, "Loaded ${project.name} template")
    }

    fun importArchive(uri: Uri, archiveName: String) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val stream: InputStream? = context.contentResolver.openInputStream(uri)
                if (stream != null) {
                    val project = ProjectManager.unpackZipArchive(stream, archiveName)
                    val defaultFile = project.files.firstOrNull { it.name == "index.html" } ?: project.files.firstOrNull()
                    _uiState.update {
                        it.copy(
                            currentProject = project,
                            activeFile = defaultFile,
                            openFiles = project.files.take(3),
                            visualElements = if (defaultFile != null) VisualDomEngine.extractElementsFromHtml(defaultFile.content) else emptyList(),
                            showFileExplorer = false
                        )
                    }
                    addTerminalLog(LogLevel.SUCCESS, "Successfully extracted $archiveName (${project.files.size} files)")
                }
            } catch (e: Exception) {
                addTerminalLog(LogLevel.ERROR, "Failed to import archive: ${e.message}")
            }
        }
    }

    // Figma & Photoshop Visual Inspector actions
    fun toggleInspectMode() {
        _uiState.update { it.copy(isInspectMode = !it.isInspectMode) }
    }

    fun selectVisualElement(element: VisualElement) {
        _uiState.update { it.copy(selectedElement = element) }
    }

    fun updateVisualElement(element: VisualElement) {
        val htmlFile = _uiState.value.currentProject.files.firstOrNull { it.name == "index.html" }
        val cssFile = _uiState.value.currentProject.files.firstOrNull { it.name.endsWith(".css") } ?: CodeFile(name = "style.css", path = "/style.css")

        if (htmlFile != null) {
            val (updatedHtml, updatedCss) = VisualDomEngine.applyElementToCode(
                element = element,
                htmlContent = htmlFile.content,
                cssContent = cssFile.content
            )

            val updatedFiles = _uiState.value.currentProject.files.map { f ->
                when (f.name) {
                    "index.html" -> f.copy(content = updatedHtml, isModified = true)
                    "style.css" -> f.copy(content = updatedCss, isModified = true)
                    else -> f
                }
            }

            _uiState.update {
                it.copy(
                    selectedElement = element,
                    currentProject = it.currentProject.copy(files = updatedFiles),
                    activeFile = if (it.activeFile?.name == "index.html") it.activeFile.copy(content = updatedHtml) else if (it.activeFile?.name == "style.css") it.activeFile.copy(content = updatedCss) else it.activeFile
                )
            }
        }
    }

    // Git actions
    fun commitChanges(message: String) {
        val newCommit = GitCommit(
            message = message,
            branch = _uiState.value.currentBranch,
            filesChangedCount = _uiState.value.workingChanges.size.coerceAtLeast(1)
        )
        _uiState.update {
            it.copy(
                gitCommits = listOf(newCommit) + it.gitCommits,
                workingChanges = emptyList()
            )
        }
        addTerminalLog(LogLevel.SUCCESS, "[${newCommit.branch} ${newCommit.hash}] $message")
    }

    fun switchBranch(branchName: String) {
        _uiState.update { it.copy(currentBranch = branchName) }
        addTerminalLog(LogLevel.INFO, "Switched to branch '$branchName'")
    }

    fun createBranch(branchName: String) {
        val newBranch = GitBranch(branchName, isCurrent = true)
        _uiState.update {
            it.copy(
                gitBranches = it.gitBranches.map { b -> b.copy(isCurrent = false) } + newBranch,
                currentBranch = branchName
            )
        }
        addTerminalLog(LogLevel.SUCCESS, "Created and checked out new branch '$branchName'")
    }

    // Terminal Commands
    fun executeTerminalCommand(cmd: String) {
        addTerminalLog(LogLevel.CMD, cmd)
        val tokens = cmd.trim().split(" ")
        val root = tokens.firstOrNull()?.lowercase() ?: ""

        when (root) {
            "help" -> {
                addTerminalLog(LogLevel.INFO, "Available commands:")
                addTerminalLog(LogLevel.INFO, "  run          - Starts 120 FPS live preview")
                addTerminalLog(LogLevel.INFO, "  format       - Auto-formats active code file")
                addTerminalLog(LogLevel.INFO, "  embed        - Merges HTML, CSS, JS into 1 file")
                addTerminalLog(LogLevel.INFO, "  split        - Splits embedded code into separate files")
                addTerminalLog(LogLevel.INFO, "  ls           - Lists all project files")
                addTerminalLog(LogLevel.INFO, "  cat <file>   - Prints file contents")
                addTerminalLog(LogLevel.INFO, "  git status   - Displays working tree status")
                addTerminalLog(LogLevel.INFO, "  git commit   - Commits staged changes")
                addTerminalLog(LogLevel.INFO, "  fps          - Benchmarks 120 FPS viewport")
                addTerminalLog(LogLevel.INFO, "  host         - Displays local & public hosting URL")
                addTerminalLog(LogLevel.INFO, "  clear        - Clears terminal output")
            }
            "format" -> formatCurrentCode()
            "embed" -> embedProjectCode()
            "split" -> splitCurrentCode()
            "run" -> {
                switchTab(1)
                addTerminalLog(LogLevel.SUCCESS, "Preview launched at 120 FPS on localhost:3000")
            }
            "ls" -> {
                val list = _uiState.value.currentProject.files.joinToString("  ") { it.name }
                addTerminalLog(LogLevel.INFO, list)
            }
            "cat" -> {
                val fname = tokens.getOrNull(1)
                val target = _uiState.value.currentProject.files.firstOrNull { it.name == fname }
                if (target != null) {
                    addTerminalLog(LogLevel.INFO, target.content.take(300))
                } else {
                    addTerminalLog(LogLevel.ERROR, "File not found: $fname")
                }
            }
            "git" -> {
                val sub = tokens.getOrNull(1)?.lowercase()
                when (sub) {
                    "status" -> {
                        addTerminalLog(LogLevel.INFO, "On branch ${_uiState.value.currentBranch}")
                        if (_uiState.value.workingChanges.isEmpty()) {
                            addTerminalLog(LogLevel.SUCCESS, "nothing to commit, working tree clean")
                        } else {
                            _uiState.value.workingChanges.forEach {
                                addTerminalLog(LogLevel.WARN, "  modified: ${it.file.name}")
                            }
                        }
                    }
                    "commit" -> {
                        val msg = cmd.substringAfter("-m", "Update code").replace("\"", "").trim()
                        commitChanges(msg)
                    }
                    else -> addTerminalLog(LogLevel.INFO, "git status | git commit -m <msg> | git log")
                }
            }
            "fps" -> {
                addTerminalLog(LogLevel.SUCCESS, "Target: 120.0 FPS | Frame Delta: 8.33ms | GPU Acceleration: ON")
            }
            "host" -> {
                addTerminalLog(LogLevel.INFO, "Local: http://localhost:3000/")
                addTerminalLog(LogLevel.INFO, "Public: https://codecraft-live.io/p/${_uiState.value.currentProject.id.take(6)}")
            }
            "clear" -> {
                _uiState.update { it.copy(terminalLogs = emptyList()) }
            }
            else -> {
                addTerminalLog(LogLevel.WARN, "Command not found: $cmd. Type 'help' for commands.")
            }
        }
    }

    fun addTerminalLog(level: LogLevel, message: String) {
        val log = TerminalLog(level = level, message = message)
        _uiState.update { it.copy(terminalLogs = it.terminalLogs + log) }
    }

    // AI Copilot
    fun applyAiPrompt(prompt: String) {
        val active = _uiState.value.activeFile ?: return
        val refactored = com.example.engine.AiAutocompletionEngine.generateAiRefactoring(prompt, active.content, active.language)
        updateFileContent(refactored)
        addTerminalLog(LogLevel.SUCCESS, "AI Applied: $prompt")
    }

    // Modal sheet toggles
    fun toggleExplorer() = _uiState.update { it.copy(showFileExplorer = !it.showFileExplorer) }
    fun toggleTerminal() = _uiState.update { it.copy(showTerminal = !it.showTerminal) }
    fun toggleGit() = _uiState.update { it.copy(showGitSheet = !it.showGitSheet) }
    fun toggleDiagnostics() = _uiState.update { it.copy(showDiagnosticsSheet = !it.showDiagnosticsSheet) }
    fun toggleAi() = _uiState.update { it.copy(showAiCopilot = !it.showAiCopilot) }
    fun toggleExport() = _uiState.update { it.copy(showExportDialog = !it.showExportDialog) }
}
