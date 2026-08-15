package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.model.CodeProject
import com.example.ui.theme.*

@Composable
fun ExportDownloadDialog(
    project: CodeProject,
    onExportZip: () -> Unit,
    onExportSingleFile: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val publicUrl = remember { "https://codecraft-live.io/p/${project.name.lowercase().replace(" ", "-")}-${project.id.take(5)}" }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = AccentCyan)
                Text("Export & Download Project", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Export as ZIP Button
                Surface(
                    onClick = onExportZip,
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.FolderZip, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(24.dp))
                        Column {
                            Text("Download Project as ZIP Archive", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                            Text("Full package (.zip) with HTML, CSS, JS and config", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }

                // Download single file
                Surface(
                    onClick = onExportSingleFile,
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(24.dp))
                        Column {
                            Text("Download Active Code File", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                            Text("Save currently open file directly to device", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }

                // Public Host Shareable Link
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Public, contentDescription = null, tint = AccentPrimary, modifier = Modifier.size(18.dp))
                            Text("Public Host Preview URL", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextPrimary)
                        }
                        Text(
                            text = publicUrl,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = AccentCyan,
                            modifier = Modifier
                                .background(DarkBackground, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("CodeCraft Public URL", publicUrl)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Public Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Public Link", fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        },
        containerColor = DarkSurface
    )
}
