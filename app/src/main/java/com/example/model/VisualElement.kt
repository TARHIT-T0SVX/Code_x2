package com.example.model

import java.util.UUID

data class VisualElement(
    val id: String = UUID.randomUUID().toString(),
    val domId: String = "",
    val tagName: String = "div",
    val className: String = "",
    val textContent: String = "",
    val posX: Float = 0f, // percentage or px
    val posY: Float = 0f,
    val width: Float = 100f,
    val height: Float = 60f,
    val zIndex: Int = 1,
    val backgroundColor: String = "#1E1D22",
    val textColor: String = "#FFFFFF",
    val fontSize: Int = 14,
    val fontWeight: String = "normal", // normal, bold, 500, 600, 700
    val textAlign: String = "left", // left, center, right
    val borderRadius: Int = 8,
    val padding: Int = 12,
    val margin: Int = 0,
    val opacity: Float = 1.0f,
    val display: String = "block", // block, flex, grid, inline-block
    val flexDirection: String = "row", // row, column
    val justifyContent: String = "flex-start", // flex-start, center, space-between
    val alignItems: String = "center", // stretch, center, flex-start
    val border: String = "none", // e.g. "1px solid #38BDF8"
    val boxShadow: String = "none",
    val isSelected: Boolean = false,
    val isLocked: Boolean = false,
    val isHidden: Boolean = false,
    val children: List<VisualElement> = emptyList(),
    val sourceFile: String = "index.html"
) {
    fun toInlineCss(): String {
        val sb = StringBuilder()
        if (backgroundColor.isNotBlank()) sb.append("background-color: $backgroundColor; ")
        if (textColor.isNotBlank()) sb.append("color: $textColor; ")
        if (fontSize > 0) sb.append("font-size: ${fontSize}px; ")
        if (fontWeight.isNotBlank()) sb.append("font-weight: $fontWeight; ")
        if (textAlign.isNotBlank()) sb.append("text-align: $textAlign; ")
        if (borderRadius > 0) sb.append("border-radius: ${borderRadius}px; ")
        if (padding > 0) sb.append("padding: ${padding}px; ")
        if (margin > 0) sb.append("margin: ${margin}px; ")
        if (opacity < 1.0f) sb.append("opacity: $opacity; ")
        if (border != "none" && border.isNotBlank()) sb.append("border: $border; ")
        if (boxShadow != "none" && boxShadow.isNotBlank()) sb.append("box-shadow: $boxShadow; ")
        if (display == "flex") {
            sb.append("display: flex; flex-direction: $flexDirection; justify-content: $justifyContent; align-items: $alignItems; ")
        }
        return sb.toString().trim()
    }
}

enum class CanvasTool {
    SELECT,
    MOVE,
    RESIZE,
    TEXT_EDIT,
    STYLE_INSPECTOR,
    LAYERS_TREE
}
