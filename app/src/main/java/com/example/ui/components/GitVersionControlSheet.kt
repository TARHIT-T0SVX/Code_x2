package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GitBranch
import com.example.model.GitCommit
import com.example.model.WorkingFileChange
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GitVersionControlSheet(
    branches: List<GitBranch>,
    currentBranch: String,
    commits: List<GitCommit>,
    workingChanges: List<WorkingFileChange>,
    onSwitchBranch: (String) -> Unit,
    onCreateBranch: (String) -> Unit,
    onCommit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var commitMessage by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0: History, 1: Changes, 2: Diff/Conflicts
    var showNewBranchDialog by remember { mutableStateOf(false) }
    var newBranchName by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("git_version_control_sheet"),
        color = DarkBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Git Header & Branch Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.AccountTree, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(18.dp))
                    Text(
                        text = "GIT VERSION CONTROL",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }

                Surface(
                    onClick = { showNewBranchDialog = true },
                    shape = RoundedCornerShape(6.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(currentBranch, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.Add, contentDescription = "New Branch", tint = TextSecondary, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // Tab bar (Commits / Changes / Diff)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = AccentPrimary,
                divider = { HorizontalDivider(color = DarkBorderSubtle) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Commits (${commits.size})", fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Working Tree", fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Visual Diff", fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // Commit History Timeline
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(commits, key = { it.id }) { commit ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(AccentPrimary, CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(40.dp)
                                            .background(DarkBorder)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = commit.message,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = commit.hash,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = AccentCyan
                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "by ${commit.author} on ${commit.branch}",
                                            fontSize = 11.sp,
                                            color = TextTertiary
                                        )
                                        Text(
                                            text = dateFormat.format(Date(commit.timestamp)),
                                            fontSize = 10.sp,
                                            color = TextTertiary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Working Tree & Staged Changes
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Staged / Unstaged Files", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(workingChanges) { change ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = DarkSurfaceElevated,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = change.file.name,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "M",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentAmber
                                        )
                                    }
                                }
                            }
                        }

                        // Commit Input
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = commitMessage,
                            onValueChange = { commitMessage = it },
                            placeholder = { Text("Commit message (e.g. feat: add interactive UI elements)", fontSize = 12.sp, color = TextTertiary) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentPrimary,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (commitMessage.isNotBlank()) {
                                    onCommit(commitMessage.trim())
                                    commitMessage = ""
                                    selectedTab = 0
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Commit Changes")
                        }
                    }
                }
                2 -> {
                    // Visual Diff Viewer
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Visual Line Diff (Working vs HEAD)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = DarkSurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            LazyColumn(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                item {
                                    Text("@@ -14,7 +14,12 @@ // index.html", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = AccentCyan)
                                }
                                item {
                                    Text("- <h1 class=\"title\">Old App</h1>", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = AccentRose, modifier = Modifier.background(AccentRose.copy(alpha = 0.15f)).fillMaxWidth())
                                }
                                item {
                                    Text("+ <h1 class=\"title\">Build at Light Speed</h1>", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = AccentGreen, modifier = Modifier.background(AccentGreen.copy(alpha = 0.15f)).fillMaxWidth())
                                }
                                item {
                                    Text("+ <button class=\"btn btn-primary\">⚡ Interacted</button>", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = AccentGreen, modifier = Modifier.background(AccentGreen.copy(alpha = 0.15f)).fillMaxWidth())
                                }
                                item {
                                    Text("  <script src=\"script.js\"></script>", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = TextTertiary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewBranchDialog) {
        AlertDialog(
            onDismissRequest = { showNewBranchDialog = false },
            title = { Text("Create & Checkout Branch", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = newBranchName,
                    onValueChange = { newBranchName = it },
                    placeholder = { Text("e.g. feature/canvas-physics", color = TextTertiary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newBranchName.isNotBlank()) {
                            onCreateBranch(newBranchName.trim())
                            newBranchName = ""
                            showNewBranchDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
                ) {
                    Text("Checkout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewBranchDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}
