package com.example.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AiAutocompletionEngine
import com.example.engine.SyntaxHighlighter
import com.example.model.CodeFile
import com.example.ui.components.SymbolBar
import com.example.ui.theme.*

@Composable
fun EditorScreen(
    activeFile: CodeFile?,
    openFiles: List<CodeFile>,
    onSelectTab: (CodeFile) -> Unit,
    onCloseTab: (CodeFile) -> Unit,
    onContentChange: (String) -> Unit,
    onFormat: () -> Unit,
    onEmbed: () -> Unit,
    onSplit: () -> Unit,
    onToggleExplorer: () -> Unit,
    onToggleTerminal: () -> Unit,
    onToggleGit: () -> Unit,
    onToggleAi: () -> Unit,
    onToggleExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember(activeFile?.id) {
        mutableStateOf(TextFieldValue(activeFile?.content ?: ""))
    }

    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }

    val undoStack = remember { mutableStateListOf<String>() }
    val redoStack = remember { mutableStateListOf<String>() }

    // Keep textFieldValue in sync if content changed externally
    LaunchedEffect(activeFile?.content) {
        val current = activeFile?.content ?: ""
        if (textFieldValue.text != current) {
            textFieldValue = TextFieldValue(current, TextRange(current.length))
        }
    }

    val lines = remember(textFieldValue.text) {
        textFieldValue.text.split("\n")
    }

    val highlightedText = remember(textFieldValue.text, activeFile?.language) {
        if (activeFile != null) {
            SyntaxHighlighter.highlight(textFieldValue.text, activeFile.language)
        } else {
            androidx.compose.ui.text.AnnotatedString(textFieldValue.text)
        }
    }

    val suggestions = remember(textFieldValue.text, activeFile?.language) {
        val wordAtCursor = textFieldValue.text.take(textFieldValue.selection.start).substringAfterLast(" ").substringAfterLast("\n")
        if (activeFile != null) {
            AiAutocompletionEngine.getSuggestions(wordAtCursor, activeFile.language)
        } else {
            emptyList()
        }
    }

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("editor_screen"),
        color = DarkBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // VS Code Action Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Quick Toggles
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onToggleExplorer, modifier = Modifier.size(32.dp).testTag("btn_explorer_toggle")) {
                        Icon(Icons.Default.Folder, contentDescription = "Explorer", tint = AccentCyan, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onToggleGit, modifier = Modifier.size(32.dp).testTag("btn_git_toggle")) {
                        Icon(Icons.Default.AccountTree, contentDescription = "Git", tint = AccentPurple, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onToggleTerminal, modifier = Modifier.size(32.dp).testTag("btn_terminal_toggle")) {
                        Icon(Icons.Default.Terminal, contentDescription = "Terminal", tint = AccentGreen, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onToggleAi, modifier = Modifier.size(32.dp).testTag("btn_ai_toggle")) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Copilot", tint = AccentCyan, modifier = Modifier.size(18.dp))
                    }
                }

                // Center Action Buttons (Format, Embed, Split)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Format Button
                    Surface(
                        onClick = onFormat,
                        shape = RoundedCornerShape(6.dp),
                        color = DarkSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier.testTag("btn_format")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(14.dp))
                            Text("Format", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }

                    // Embed Button
                    Surface(
                        onClick = onEmbed,
                        shape = RoundedCornerShape(6.dp),
                        color = DarkSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier.testTag("btn_embed")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.MergeType, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(14.dp))
                            Text("Embed", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }

                    // Split Button
                    Surface(
                        onClick = onSplit,
                        shape = RoundedCornerShape(6.dp),
                        color = DarkSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier.testTag("btn_split")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.CallSplit, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(14.dp))
                            Text("Split", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }

                    IconButton(onClick = onToggleExport, modifier = Modifier.size(32.dp).testTag("btn_export_toggle")) {
                        Icon(Icons.Default.CloudDownload, contentDescription = "Export & Download", tint = AccentPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Tabs Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceElevated)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(openFiles, key = { it.id }) { file ->
                    val isActive = activeFile?.id == file.id
                    Surface(
                        onClick = { onSelectTab(file) },
                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                        color = if (isActive) DarkBackground else DarkSurfaceElevated,
                        border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, DarkBorder) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = file.name,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (isActive) TextPrimary else TextSecondary
                            )
                            if (openFiles.size > 1) {
                                IconButton(
                                    onClick = { onCloseTab(file) },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close Tab",
                                        tint = TextTertiary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search & Replace Bar (collapsible)
            if (showSearchBar) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Find...", fontSize = 11.sp, color = TextTertiary) },
                        singleLine = true,
                        modifier = Modifier.weight(1f).height(38.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = replaceQuery,
                        onValueChange = { replaceQuery = it },
                        placeholder = { Text("Replace...", fontSize = 11.sp, color = TextTertiary) },
                        singleLine = true,
                        modifier = Modifier.weight(1f).height(38.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Button(
                        onClick = {
                            if (searchQuery.isNotBlank()) {
                                val replaced = textFieldValue.text.replace(searchQuery, replaceQuery)
                                textFieldValue = TextFieldValue(replaced)
                                onContentChange(replaced)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("Replace All", fontSize = 10.sp)
                    }
                }
            }

            // Main Code Editor Area (Gutter + Text Field)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(DarkBackground)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScrollState)
                ) {
                    // Line numbers gutter
                    Column(
                        modifier = Modifier
                            .background(DarkSurface)
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                            .widthIn(min = 36.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        lines.indices.forEach { index ->
                            Text(
                                text = "${index + 1}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = TextMuted,
                                lineHeight = 19.sp
                            )
                        }
                    }

                    // Code Editor
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(horizontalScrollState)
                            .padding(8.dp)
                    ) {
                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = { newValue ->
                                if (newValue.text != textFieldValue.text) {
                                    undoStack.add(textFieldValue.text)
                                }
                                textFieldValue = newValue
                                onContentChange(newValue.text)
                            },
                            textStyle = CodeTextStyle,
                            cursorBrush = SolidColor(AccentCyan),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("code_text_field"),
                            decorationBox = { innerTextField ->
                                Box {
                                    innerTextField()
                                }
                            }
                        )
                    }
                }
            }

            // Autocomplete Suggestions Bar
            if (suggestions.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(suggestions) { item ->
                        Surface(
                            onClick = {
                                val currentText = textFieldValue.text
                                val cursor = textFieldValue.selection.start
                                val newText = currentText.substring(0, cursor) + item.insertText + currentText.substring(cursor)
                                textFieldValue = TextFieldValue(newText, TextRange(cursor + item.insertText.length))
                                onContentChange(newText)
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = DarkSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(item.label, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                                Text(item.detail, fontSize = 10.sp, color = TextTertiary)
                            }
                        }
                    }
                }
            }

            // Bottom Symbol Keyboard Bar
            SymbolBar(
                onInsertSymbol = { symbol ->
                    val currentText = textFieldValue.text
                    val cursor = textFieldValue.selection.start
                    val newText = currentText.substring(0, cursor) + symbol + currentText.substring(cursor)
                    textFieldValue = TextFieldValue(newText, TextRange(cursor + symbol.length))
                    onContentChange(newText)
                },
                onUndo = {
                    if (undoStack.isNotEmpty()) {
                        val prev = undoStack.removeLast()
                        redoStack.add(textFieldValue.text)
                        textFieldValue = TextFieldValue(prev)
                        onContentChange(prev)
                    }
                },
                onRedo = {
                    if (redoStack.isNotEmpty()) {
                        val next = redoStack.removeLast()
                        undoStack.add(textFieldValue.text)
                        textFieldValue = TextFieldValue(next)
                        onContentChange(next)
                    }
                },
                onFormat = onFormat,
                onIndent = {
                    val currentText = textFieldValue.text
                    val cursor = textFieldValue.selection.start
                    val newText = currentText.substring(0, cursor) + "  " + currentText.substring(cursor)
                    textFieldValue = TextFieldValue(newText, TextRange(cursor + 2))
                    onContentChange(newText)
                }
            )
        }
    }
}
