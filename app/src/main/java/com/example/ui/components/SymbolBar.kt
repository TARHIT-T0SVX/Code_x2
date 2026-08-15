package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.FormatIndentIncrease
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SymbolBar(
    onInsertSymbol: (String) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onFormat: () -> Unit,
    onIndent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val symbols = listOf(
        "<", ">", "/", "=", "\"", "'", "{", "}", "[", "]",
        "(", ")", ";", ":", ".", ",", "!", "$", "#", "@",
        "%", "&", "|", "+", "-", "*", "_", "?", "\\", "~"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("symbol_bar"),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Action Buttons
            FilledTonalIconButton(
                onClick = onUndo,
                modifier = Modifier.size(34.dp).testTag("btn_undo"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = DarkSurfaceElevated,
                    contentColor = TextPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    modifier = Modifier.size(16.dp)
                )
            }

            FilledTonalIconButton(
                onClick = onRedo,
                modifier = Modifier.size(34.dp).testTag("btn_redo"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = DarkSurfaceElevated,
                    contentColor = TextPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo",
                    modifier = Modifier.size(16.dp)
                )
            }

            FilledTonalIconButton(
                onClick = onIndent,
                modifier = Modifier.size(34.dp).testTag("btn_indent"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = DarkSurfaceElevated,
                    contentColor = AccentCyan
                )
            ) {
                Text("TAB", fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            FilledTonalIconButton(
                onClick = onFormat,
                modifier = Modifier.size(34.dp).testTag("btn_format_quick"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = DarkSurfaceElevated,
                    contentColor = AccentGreen
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AutoFixHigh,
                    contentDescription = "Format",
                    modifier = Modifier.size(16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(1.dp)
                    .background(DarkBorder)
            )

            // Symbol keys
            symbols.forEach { symbol ->
                Surface(
                    onClick = { onInsertSymbol(symbol) },
                    shape = RoundedCornerShape(6.dp),
                    color = DarkSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .height(34.dp)
                        .widthIn(min = 32.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = symbol,
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
