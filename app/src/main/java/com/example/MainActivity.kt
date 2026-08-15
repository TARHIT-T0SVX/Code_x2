package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.ProjectManager
import com.example.model.LogLevel
import com.example.ui.components.*
import com.example.ui.editor.EditorScreen
import com.example.ui.preview.PreviewScreen
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                CodeCraftApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeCraftApp(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val htmlContent = remember(uiState.currentProject.files) {
        uiState.currentProject.files.firstOrNull { it.name == "index.html" || it.name.endsWith(".html") }?.content ?: ""
    }
    val cssContent = remember(uiState.currentProject.files) {
        uiState.currentProject.files.firstOrNull { it.name.endsWith(".css") }?.content ?: ""
    }
    val jsContent = remember(uiState.currentProject.files) {
        uiState.currentProject.files.firstOrNull { it.name.endsWith(".js") }?.content ?: ""
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("app_scaffold"),
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().testTag("top_app_bar"),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DarkSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Box(modifier = Modifier.padding(6.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = null,
                                    tint = AccentCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = uiState.currentProject.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Box(modifier = Modifier.size(6.dp).background(AccentGreen, CircleShape))
                            }
                            Text(
                                text = "localhost:3000 • 120 FPS Realtime",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = TextTertiary
                            )
                        }
                    }

                    // Top Action Icons
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = { viewModel.toggleDiagnostics() },
                            modifier = Modifier.size(34.dp).testTag("btn_diagnostics")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Performance & Server Diagnostics",
                                tint = AccentCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleExport() },
                            modifier = Modifier.size(34.dp).testTag("btn_export_main")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Download Project",
                                tint = AccentPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 0.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                // Editor Tab
                NavigationBarItem(
                    selected = uiState.currentTab == 0,
                    onClick = { viewModel.switchTab(0) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Code Editor",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Code Editor",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = if (uiState.currentTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentPrimary,
                        selectedTextColor = AccentPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = DarkSurfaceElevated
                    ),
                    modifier = Modifier.testTag("nav_tab_editor")
                )

                // Preview & Design Tab
                NavigationBarItem(
                    selected = uiState.currentTab == 1,
                    onClick = { viewModel.switchTab(1) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Live Preview & Design",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Live Preview & Design",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = if (uiState.currentTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentCyan,
                        selectedTextColor = AccentCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = DarkSurfaceElevated
                    ),
                    modifier = Modifier.testTag("nav_tab_preview")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentTab) {
                0 -> {
                    EditorScreen(
                        activeFile = uiState.activeFile,
                        openFiles = uiState.openFiles,
                        onSelectTab = { viewModel.selectFile(it) },
                        onCloseTab = { viewModel.closeTab(it) },
                        onContentChange = { viewModel.updateFileContent(it) },
                        onFormat = { viewModel.formatCurrentCode() },
                        onEmbed = { viewModel.embedProjectCode() },
                        onSplit = { viewModel.splitCurrentCode() },
                        onToggleExplorer = { viewModel.toggleExplorer() },
                        onToggleTerminal = { viewModel.toggleTerminal() },
                        onToggleGit = { viewModel.toggleGit() },
                        onToggleAi = { viewModel.toggleAi() },
                        onToggleExport = { viewModel.toggleExport() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                1 -> {
                    PreviewScreen(
                        htmlContent = htmlContent,
                        cssContent = cssContent,
                        jsContent = jsContent,
                        visualElements = uiState.visualElements,
                        selectedElement = uiState.selectedElement,
                        isInspectMode = uiState.isInspectMode,
                        onToggleInspectMode = { viewModel.toggleInspectMode() },
                        onSelectElement = { viewModel.selectVisualElement(it) },
                        onUpdateElement = { viewModel.updateVisualElement(it) },
                        onConsoleLog = { level, msg ->
                            val logLevel = when (level) {
                                "WARN" -> LogLevel.WARN
                                "ERROR" -> LogLevel.ERROR
                                else -> LogLevel.INFO
                            }
                            viewModel.addTerminalLog(logLevel, "[JS] $msg")
                        },
                        onToggleExport = { viewModel.toggleExport() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // File Explorer Drawer Modal Sheet
            if (uiState.showFileExplorer) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.toggleExplorer() },
                    containerColor = DarkSurface,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = DarkBorder) }
                ) {
                    Box(modifier = Modifier.height(480.dp)) {
                        FileExplorerDrawer(
                            files = uiState.currentProject.files,
                            activeFile = uiState.activeFile,
                            onSelectFile = { viewModel.selectFile(it) },
                            onCreateFile = { viewModel.createFile(it) },
                            onCreateFolder = { viewModel.createFolder(it) },
                            onDeleteFile = { viewModel.deleteFile(it) },
                            onUploadArchiveUri = { uri, name -> viewModel.importArchive(uri, name) },
                            onLoadTemplate = { viewModel.loadTemplate(it) }
                        )
                    }
                }
            }

            // Interactive Terminal Modal Sheet
            if (uiState.showTerminal) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.toggleTerminal() },
                    containerColor = DarkSurface,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = DarkBorder) }
                ) {
                    Box(modifier = Modifier.height(420.dp)) {
                        TerminalSheet(
                            logs = uiState.terminalLogs,
                            onExecuteCommand = { viewModel.executeTerminalCommand(it) },
                            onClearLogs = { viewModel.executeTerminalCommand("clear") }
                        )
                    }
                }
            }

            // Git Version Control Modal Sheet
            if (uiState.showGitSheet) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.toggleGit() },
                    containerColor = DarkSurface,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = DarkBorder) }
                ) {
                    Box(modifier = Modifier.height(500.dp)) {
                        GitVersionControlSheet(
                            branches = uiState.gitBranches,
                            currentBranch = uiState.currentBranch,
                            commits = uiState.gitCommits,
                            workingChanges = uiState.workingChanges,
                            onSwitchBranch = { viewModel.switchBranch(it) },
                            onCreateBranch = { viewModel.createBranch(it) },
                            onCommit = { viewModel.commitChanges(it) }
                        )
                    }
                }
            }

            // Performance & Server Diagnostics Modal Sheet
            if (uiState.showDiagnosticsSheet) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.toggleDiagnostics() },
                    containerColor = DarkSurface,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = DarkBorder) }
                ) {
                    Box(modifier = Modifier.height(460.dp)) {
                        DiagnosticsSheet()
                    }
                }
            }

            // AI Copilot Modal Sheet
            if (uiState.showAiCopilot) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.toggleAi() },
                    containerColor = DarkSurface,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = DarkBorder) }
                ) {
                    AiCopilotSheet(
                        onApplyAiPrompt = { prompt ->
                            viewModel.applyAiPrompt(prompt)
                            viewModel.toggleAi()
                        }
                    )
                }
            }

            // Download & Export Project Dialog
            if (uiState.showExportDialog) {
                ExportDownloadDialog(
                    project = uiState.currentProject,
                    onExportZip = {
                        try {
                            val zipBytes = ProjectManager.exportProjectAsZip(uiState.currentProject)
                            val cacheDir = context.cacheDir
                            val zipFile = File(cacheDir, "${uiState.currentProject.name.lowercase().replace(" ", "_")}.zip")
                            FileOutputStream(zipFile).use { it.write(zipBytes) }
                            Toast.makeText(context, "Project exported to ${zipFile.name} (${zipBytes.size} bytes)", Toast.LENGTH_LONG).show()
                            viewModel.addTerminalLog(LogLevel.SUCCESS, "Exported project ZIP (${zipBytes.size} bytes)")
                        } catch (e: Exception) {
                            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                        viewModel.toggleExport()
                    },
                    onExportSingleFile = {
                        val active = uiState.activeFile
                        if (active != null) {
                            try {
                                val file = File(context.cacheDir, active.name)
                                file.writeText(active.content)
                                Toast.makeText(context, "Saved ${active.name} (${active.content.length} chars)", Toast.LENGTH_SHORT).show()
                                viewModel.addTerminalLog(LogLevel.SUCCESS, "Downloaded single file ${active.name}")
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        viewModel.toggleExport()
                    },
                    onDismiss = { viewModel.toggleExport() }
                )
            }
        }
    }
}
