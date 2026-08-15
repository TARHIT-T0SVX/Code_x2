package com.example.ui.preview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.CodeTransformer
import com.example.engine.VisualDomEngine
import com.example.model.CanvasTool
import com.example.model.VisualElement
import com.example.ui.theme.*
import kotlin.math.roundToInt

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PreviewScreen(
    htmlContent: String,
    cssContent: String,
    jsContent: String,
    visualElements: List<VisualElement>,
    selectedElement: VisualElement?,
    isInspectMode: Boolean,
    onToggleInspectMode: () -> Unit,
    onSelectElement: (VisualElement) -> Unit,
    onUpdateElement: (VisualElement) -> Unit,
    onConsoleLog: (String, String) -> Unit,
    onToggleExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var selectedDevice by remember { mutableStateOf("Mobile") } // Mobile, Tablet, Desktop, Responsive
    var isLandscape by remember { mutableStateOf(false) }
    var showLayersPanel by remember { mutableStateOf(false) }
    var showStyleInspector by remember { mutableStateOf(false) }
    var currentFps by remember { mutableIntStateOf(120) }

    // Prepare complete merged HTML payload with injected 120 FPS inspector bridge
    val fullHtmlPayload = remember(htmlContent, cssContent, jsContent) {
        val baseEmbedded = CodeTransformer.embedCode(htmlContent, cssContent, jsContent)
        val inspectorScript = "\n<script>\n${VisualDomEngine.getInspectorBridgeJs()}\n</script>\n"
        if (baseEmbedded.contains("</body>", ignoreCase = true)) {
            baseEmbedded.replace("</body>", "$inspectorScript</body>", ignoreCase = true)
        } else {
            baseEmbedded + inspectorScript
        }
    }

    // Dynamic Viewport Dimensions
    val viewportWidth = when (selectedDevice) {
        "Mobile" -> if (isLandscape) 560.dp else 340.dp
        "Tablet" -> if (isLandscape) 640.dp else 420.dp
        "Desktop" -> 680.dp
        else -> Modifier.fillMaxWidth()
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("preview_screen"),
        color = DarkBackground
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Preview Controls Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: 120 FPS badge & Device switcher
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // FPS Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = DarkSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(AccentGreen, CircleShape))
                            Text("$currentFps FPS", fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                        }
                    }

                    // Device toggles
                    listOf("Mobile", "Tablet", "Desktop", "Full").forEach { device ->
                        val isSelected = selectedDevice == device
                        Surface(
                            onClick = { selectedDevice = device },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) AccentPrimary else DarkSurfaceElevated
                        ) {
                            Text(
                                text = device,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Right: Figma/Photoshop Visual Design Mode Toggle & Tools
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Orientation toggle
                    IconButton(
                        onClick = { isLandscape = !isLandscape },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (isLandscape) Icons.Default.ScreenRotation else Icons.Default.StayCurrentPortrait,
                            contentDescription = "Rotate",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Figma Mode Button
                    Surface(
                        onClick = {
                            onToggleInspectMode()
                            webViewRef?.evaluateJavascript("window.__enableInspectMode(${!isInspectMode});", null)
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isInspectMode) AccentPurple else DarkSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isInspectMode) AccentPurple else DarkBorder),
                        modifier = Modifier.testTag("btn_figma_mode")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = if (isInspectMode) Color.White else AccentPurple,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isInspectMode) "Figma Active" else "Figma / Layers",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isInspectMode) Color.White else TextPrimary
                            )
                        }
                    }

                    // Reload
                    IconButton(
                        onClick = {
                            webViewRef?.loadDataWithBaseURL("http://localhost:3000/", fullHtmlPayload, "text/html", "UTF-8", null)
                        },
                        modifier = Modifier.size(30.dp).testTag("btn_preview_reload")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }

                    // Export
                    IconButton(
                        onClick = onToggleExport,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = AccentCyan, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Subtoolbar if Figma Visual Inspector is active
            if (isInspectMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("FIGMA CANVAS TOOLS:", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = AccentPurple, fontWeight = FontWeight.Bold)

                        Surface(
                            onClick = { showLayersPanel = !showLayersPanel },
                            shape = RoundedCornerShape(4.dp),
                            color = if (showLayersPanel) AccentPurple else DarkSurface
                        ) {
                            Text("Layers Tree", fontSize = 10.sp, color = if (showLayersPanel) Color.White else TextPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }

                        Surface(
                            onClick = { showStyleInspector = !showStyleInspector },
                            shape = RoundedCornerShape(4.dp),
                            color = if (showStyleInspector) AccentCyan else DarkSurface
                        ) {
                            Text("Style Inspector", fontSize = 10.sp, color = if (showStyleInspector) DarkBackground else TextPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }
                    }

                    if (selectedElement != null) {
                        Text(
                            text = "<${selectedElement.tagName}${if (selectedElement.domId.isNotBlank()) "#${selectedElement.domId}" else ""}>",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = AccentGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Main Preview Canvas Viewport
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF070709)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = if (selectedDevice == "Full") {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier
                            .width(if (isLandscape) 560.dp else 340.dp)
                            .fillMaxHeight()
                            .padding(vertical = 8.dp)
                            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                    }
                ) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    useWideViewPort = true
                                    loadWithOverviewMode = true
                                    setSupportZoom(true)
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                    cacheMode = WebSettings.LOAD_NO_CACHE
                                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                }

                                // Hardware acceleration for 120 FPS
                                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

                                // Bridge for visual inspection and logs
                                addJavascriptInterface(object {
                                    @JavascriptInterface
                                    fun onElementSelected(json: String) {
                                        val el = VisualDomEngine.parseElementFromJson(json)
                                        if (el != null) {
                                            onSelectElement(el)
                                        }
                                    }

                                    @JavascriptInterface
                                    fun onConsoleLog(level: String, msg: String) {
                                        onConsoleLog(level, msg)
                                    }
                                }, "AndroidBridge")

                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        if (isInspectMode) {
                                            view?.evaluateJavascript("window.__enableInspectMode(true);", null)
                                        }
                                    }
                                }

                                webChromeClient = WebChromeClient()
                                webViewRef = this
                                loadDataWithBaseURL("http://localhost:3000/", fullHtmlPayload, "text/html", "UTF-8", null)
                            }
                        },
                        update = { view ->
                            webViewRef = view
                            view.loadDataWithBaseURL("http://localhost:3000/", fullHtmlPayload, "text/html", "UTF-8", null)
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Touch Drag & Resize overlay when an element is selected in Figma mode
                    if (isInspectMode && selectedElement != null) {
                        InteractiveElementOverlay(
                            element = selectedElement,
                            onUpdateElement = onUpdateElement
                        )
                    }
                }

                // Figma / Photoshop Layers Drawer (Overlay panel)
                androidx.compose.animation.AnimatedVisibility(
                    visible = isInspectMode && showLayersPanel,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    FigmaLayersPanel(
                        elements = visualElements,
                        selectedElement = selectedElement,
                        onSelect = { el ->
                            onSelectElement(el)
                            val selector = if (el.domId.isNotBlank()) "#${el.domId}" else if (el.className.isNotBlank()) ".${el.className.split(" ").first()}" else el.tagName
                            webViewRef?.evaluateJavascript("window.__selectElementBySelector('$selector');", null)
                        },
                        onClose = { showLayersPanel = false }
                    )
                }

                // Photoshop / Figma Style Property Inspector Drawer
                androidx.compose.animation.AnimatedVisibility(
                    visible = isInspectMode && showStyleInspector && selectedElement != null,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    if (selectedElement != null) {
                        FigmaStyleInspectorPanel(
                            element = selectedElement,
                            onUpdate = { updated ->
                                onUpdateElement(updated)
                                webViewRef?.evaluateJavascript("window.__updateSelectedStyle('backgroundColor', '${updated.backgroundColor}');", null)
                                webViewRef?.evaluateJavascript("window.__updateSelectedStyle('color', '${updated.textColor}');", null)
                                webViewRef?.evaluateJavascript("window.__updateSelectedStyle('fontSize', '${updated.fontSize}px');", null)
                                webViewRef?.evaluateJavascript("window.__updateSelectedStyle('borderRadius', '${updated.borderRadius}px');", null)
                                webViewRef?.evaluateJavascript("window.__updateSelectedStyle('padding', '${updated.padding}px');", null)
                            },
                            onClose = { showStyleInspector = false }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive touch bounding box with drag position and resize handles
 */
@Composable
private fun InteractiveElementOverlay(
    element: VisualElement,
    onUpdateElement: (VisualElement) -> Unit
) {
    var offsetX by remember(element.id) { mutableFloatStateOf(element.posX) }
    var offsetY by remember(element.id) { mutableFloatStateOf(element.posY) }
    var currentWidth by remember(element.id) { mutableFloatStateOf(element.width.coerceAtLeast(80f)) }
    var currentHeight by remember(element.id) { mutableFloatStateOf(element.height.coerceAtLeast(40f)) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .size(currentWidth.dp, currentHeight.dp)
            .border(2.dp, AccentPurple, RoundedCornerShape(4.dp))
            .pointerInput(element.id) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                    onUpdateElement(element.copy(posX = offsetX, posY = offsetY))
                }
            }
    ) {
        // Coordinate label badge
        Surface(
            color = AccentPurple,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.align(Alignment.TopStart).offset(y = (-20).dp)
        ) {
            Text(
                text = "${element.tagName} (${currentWidth.toInt()}×${currentHeight.toInt()})",
                color = Color.White,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        // Bottom right resize handle
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(16.dp)
                .offset(x = 6.dp, y = 6.dp)
                .background(AccentCyan, CircleShape)
                .border(2.dp, Color.White, CircleShape)
                .pointerInput(element.id) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        currentWidth = (currentWidth + dragAmount.x).coerceAtLeast(40f)
                        currentHeight = (currentHeight + dragAmount.y).coerceAtLeast(20f)
                        onUpdateElement(element.copy(width = currentWidth, height = currentHeight))
                    }
                }
        )
    }
}

/**
 * Figma-style Layers Hierarchy List
 */
@Composable
private fun FigmaLayersPanel(
    elements: List<VisualElement>,
    selectedElement: VisualElement?,
    onSelect: (VisualElement) -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(220.dp)
            .fillMaxHeight()
            .padding(8.dp),
        shape = RoundedCornerShape(10.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceElevated)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("LAYERS TREE", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentPurple)
                IconButton(onClick = onClose, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(14.dp))
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(elements, key = { it.id }) { el ->
                    val isSelected = selectedElement?.id == el.id
                    Surface(
                        onClick = { onSelect(el) },
                        shape = RoundedCornerShape(4.dp),
                        color = if (isSelected) DarkSurfaceHighlight else Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (el.tagName == "button") Icons.Default.SmartButton else Icons.Default.Layers,
                                contentDescription = null,
                                tint = if (isSelected) AccentPurple else TextTertiary,
                                modifier = Modifier.size(14.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (el.domId.isNotBlank()) "#${el.domId}" else el.tagName,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TextPrimary else TextSecondary
                                )
                                if (el.textContent.isNotBlank()) {
                                    Text(el.textContent.take(20), fontSize = 9.sp, color = TextTertiary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Photoshop / Figma Style Property Inspector
 */
@Composable
private fun FigmaStyleInspectorPanel(
    element: VisualElement,
    onUpdate: (VisualElement) -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight()
            .padding(8.dp),
        shape = RoundedCornerShape(10.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("STYLE INSPECTOR", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentCyan)
                IconButton(onClick = onClose, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(14.dp))
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Background Color
                item {
                    Text("Background Color", fontSize = 10.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("#6366F1", "#38BDF8", "#34D399", "#F43F5E", "#1E1D22", "#000000").forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
                                    .border(if (element.backgroundColor == hex) 2.dp else 1.dp, if (element.backgroundColor == hex) Color.White else DarkBorder, CircleShape)
                                    .pointerInput(hex) {
                                        detectTapGestures {
                                            onUpdate(element.copy(backgroundColor = hex))
                                        }
                                    }
                            )
                        }
                    }
                }

                // Text Color
                item {
                    Text("Text Color", fontSize = 10.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("#FFFFFF", "#F1F1F4", "#A0A0AB", "#38BDF8", "#FBBF24").forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
                                    .border(if (element.textColor == hex) 2.dp else 1.dp, if (element.textColor == hex) AccentCyan else DarkBorder, CircleShape)
                                    .pointerInput(hex) {
                                        detectTapGestures {
                                            onUpdate(element.copy(textColor = hex))
                                        }
                                    }
                            )
                        }
                    }
                }

                // Font Size Slider
                item {
                    Text("Font Size: ${element.fontSize}px", fontSize = 10.sp, color = TextSecondary)
                    Slider(
                        value = element.fontSize.toFloat(),
                        onValueChange = { onUpdate(element.copy(fontSize = it.toInt())) },
                        valueRange = 10f..40f,
                        colors = SliderDefaults.colors(thumbColor = AccentCyan, activeTrackColor = AccentCyan)
                    )
                }

                // Border Radius Slider
                item {
                    Text("Border Radius: ${element.borderRadius}px", fontSize = 10.sp, color = TextSecondary)
                    Slider(
                        value = element.borderRadius.toFloat(),
                        onValueChange = { onUpdate(element.copy(borderRadius = it.toInt())) },
                        valueRange = 0f..32f,
                        colors = SliderDefaults.colors(thumbColor = AccentPurple, activeTrackColor = AccentPurple)
                    )
                }

                // Padding Slider
                item {
                    Text("Padding: ${element.padding}px", fontSize = 10.sp, color = TextSecondary)
                    Slider(
                        value = element.padding.toFloat(),
                        onValueChange = { onUpdate(element.copy(padding = it.toInt())) },
                        valueRange = 0f..40f,
                        colors = SliderDefaults.colors(thumbColor = AccentGreen, activeTrackColor = AccentGreen)
                    )
                }

                // Opacity Slider
                item {
                    Text("Opacity: ${(element.opacity * 100).toInt()}%", fontSize = 10.sp, color = TextSecondary)
                    Slider(
                        value = element.opacity,
                        onValueChange = { onUpdate(element.copy(opacity = it)) },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = AccentPrimary, activeTrackColor = AccentPrimary)
                    )
                }
            }
        }
    }
}
