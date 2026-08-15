package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Speed
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
import com.example.model.NodeHealth
import com.example.model.ServerNodeInfo
import com.example.ui.theme.*

@Composable
fun DiagnosticsSheet(
    modifier: Modifier = Modifier
) {
    val serverNodes = remember {
        listOf(
            ServerNodeInfo("Local Zero-Latency Node", "localhost:3000", 0, NodeHealth.HEALTHY, 12),
            ServerNodeInfo("Public Edge Node US-East", "us-east.codecraft-live.io", 14, NodeHealth.HEALTHY, 28),
            ServerNodeInfo("Public Edge Node EU-Central", "eu-central.codecraft-live.io", 26, NodeHealth.HEALTHY, 34),
            ServerNodeInfo("Public Edge Node AP-South", "ap-south.codecraft-live.io", 38, NodeHealth.HEALTHY, 41)
        )
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("diagnostics_sheet"),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Speed, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                Text(
                    text = "PERFORMANCE & SERVER DIAGNOSTICS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextPrimary
                )
            }

            // 120 FPS HUD Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TARGET FRAME RATE", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextTertiary)
                        Text("120 FPS", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = AccentCyan, fontFamily = FontFamily.Monospace)
                        Text("Hardware Acceleration: Enabled", fontSize = 11.sp, color = AccentGreen)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("FRAME TIME", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextTertiary)
                        Text("8.33 ms", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary, fontFamily = FontFamily.Monospace)
                        Text("Zero-Latency Pipeline", fontSize = 11.sp, color = AccentPrimary)
                    }
                }
            }

            // Node Latencies
            Text("Active Edge Nodes & Latency", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(serverNodes) { node ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = DarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.Dns, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                                Column {
                                    Text(node.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextPrimary)
                                    Text(node.region, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = TextTertiary)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(8.dp).background(AccentGreen, shape = RoundedCornerShape(4.dp)))
                                Text(
                                    text = "${node.latencyMs} ms",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (node.latencyMs < 20) AccentGreen else AccentCyan
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
