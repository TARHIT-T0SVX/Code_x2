package com.example.engine

import com.example.model.VisualElement
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object VisualDomEngine {

    /**
     * JavaScript code injected into the WebView for Figma/Photoshop style visual inspection,
     * touch element selection, dragging bounding boxes, and computed style extraction.
     */
    fun getInspectorBridgeJs(): String {
        return """
        (function() {
            if (window.__codecraft_inspector_loaded) return;
            window.__codecraft_inspector_loaded = true;

            let selectedElement = null;
            let overlay = null;
            let isInspectMode = false;

            // Create inspector overlay container
            function ensureOverlay() {
                if (!overlay) {
                    overlay = document.createElement('div');
                    overlay.id = '__codecraft_overlay';
                    overlay.style.position = 'fixed';
                    overlay.style.pointerEvents = 'none';
                    overlay.style.border = '2px solid #6366F1';
                    overlay.style.backgroundColor = 'rgba(99, 102, 241, 0.15)';
                    overlay.style.zIndex = '999999';
                    overlay.style.display = 'none';
                    overlay.style.transition = 'all 0.05s ease-out';
                    
                    // Coordinates label badge
                    const badge = document.createElement('div');
                    badge.id = '__codecraft_badge';
                    badge.style.position = 'absolute';
                    badge.style.bottom = '-24px';
                    badge.style.left = '0';
                    badge.style.backgroundColor = '#6366F1';
                    badge.style.color = '#FFFFFF';
                    badge.style.fontSize = '10px';
                    badge.style.fontFamily = 'monospace';
                    badge.style.padding = '2px 6px';
                    badge.style.borderRadius = '4px';
                    badge.style.whiteSpace = 'nowrap';
                    overlay.appendChild(badge);

                    document.body.appendChild(overlay);
                }
            }

            function updateOverlay(el) {
                if (!el || !overlay) return;
                const rect = el.getBoundingClientRect();
                overlay.style.left = rect.left + 'px';
                overlay.style.top = rect.top + 'px';
                overlay.style.width = rect.width + 'px';
                overlay.style.height = rect.height + 'px';
                overlay.style.display = 'block';

                const badge = overlay.querySelector('#__codecraft_badge');
                if (badge) {
                    const tag = el.tagName.toLowerCase();
                    const id = el.id ? '#' + el.id : '';
                    const cls = el.className && typeof el.className === 'string' ? '.' + el.className.split(' ')[0] : '';
                    badge.textContent = tag + id + cls + ' (' + Math.round(rect.width) + 'x' + Math.round(rect.height) + 'px)';
                }
            }

            function getElementData(el) {
                if (!el) return null;
                const rect = el.getBoundingClientRect();
                const style = window.getComputedStyle(el);
                
                return {
                    id: el.getAttribute('data-cc-id') || (el.id || Math.random().toString(36).substr(2, 9)),
                    domId: el.id || '',
                    tagName: el.tagName.toLowerCase(),
                    className: typeof el.className === 'string' ? el.className : '',
                    textContent: el.childNodes.length === 1 && el.childNodes[0].nodeType === 3 ? el.textContent.trim() : '',
                    posX: rect.left,
                    posY: rect.top,
                    width: rect.width,
                    height: rect.height,
                    backgroundColor: style.backgroundColor,
                    textColor: style.color,
                    fontSize: parseInt(style.fontSize) || 14,
                    fontWeight: style.fontWeight,
                    textAlign: style.textAlign,
                    borderRadius: parseInt(style.borderRadius) || 0,
                    padding: parseInt(style.padding) || 0,
                    margin: parseInt(style.margin) || 0,
                    opacity: parseFloat(style.opacity) || 1.0,
                    display: style.display,
                    flexDirection: style.flexDirection,
                    justifyContent: style.justifyContent,
                    alignItems: style.alignItems,
                    border: style.border,
                    boxShadow: style.boxShadow,
                    zIndex: parseInt(style.zIndex) || 1
                };
            }

            document.addEventListener('click', function(e) {
                if (!isInspectMode) return;
                e.preventDefault();
                e.stopPropagation();

                let target = e.target;
                if (target === overlay || target.id === '__codecraft_overlay') return;

                selectedElement = target;
                ensureOverlay();
                updateOverlay(target);

                if (window.AndroidBridge && window.AndroidBridge.onElementSelected) {
                    const data = getElementData(target);
                    window.AndroidBridge.onElementSelected(JSON.stringify(data));
                }
            }, true);

            window.__enableInspectMode = function(enabled) {
                isInspectMode = enabled;
                ensureOverlay();
                if (!enabled && overlay) {
                    overlay.style.display = 'none';
                    selectedElement = null;
                }
            };

            window.__selectElementBySelector = function(selector) {
                const el = document.querySelector(selector);
                if (el) {
                    selectedElement = el;
                    ensureOverlay();
                    updateOverlay(el);
                    if (window.AndroidBridge && window.AndroidBridge.onElementSelected) {
                        window.AndroidBridge.onElementSelected(JSON.stringify(getElementData(el)));
                    }
                }
            };

            window.__updateSelectedStyle = function(prop, value) {
                if (selectedElement) {
                    selectedElement.style[prop] = value;
                    updateOverlay(selectedElement);
                }
            };

            // Intercept console logs to pipe to IDE terminal
            const originalLog = console.log;
            const originalWarn = console.warn;
            const originalError = console.error;

            console.log = function(...args) {
                originalLog.apply(console, args);
                if (window.AndroidBridge && window.AndroidBridge.onConsoleLog) {
                    window.AndroidBridge.onConsoleLog('INFO', args.map(a => typeof a === 'object' ? JSON.stringify(a) : String(a)).join(' '));
                }
            };
            console.warn = function(...args) {
                originalWarn.apply(console, args);
                if (window.AndroidBridge && window.AndroidBridge.onConsoleLog) {
                    window.AndroidBridge.onConsoleLog('WARN', args.map(a => typeof a === 'object' ? JSON.stringify(a) : String(a)).join(' '));
                }
            };
            console.error = function(...args) {
                originalError.apply(console, args);
                if (window.AndroidBridge && window.AndroidBridge.onConsoleLog) {
                    window.AndroidBridge.onConsoleLog('ERROR', args.map(a => typeof a === 'object' ? JSON.stringify(a) : String(a)).join(' '));
                }
            };

            window.onerror = function(msg, url, line) {
                if (window.AndroidBridge && window.AndroidBridge.onConsoleLog) {
                    window.AndroidBridge.onConsoleLog('ERROR', msg + ' at line ' + line);
                }
            };
        })();
        """.trimIndent()
    }

    /**
     * Parses JSON string received from WebView inspect bridge into VisualElement model
     */
    fun parseElementFromJson(jsonStr: String): VisualElement? {
        return try {
            val obj = JSONObject(jsonStr)
            VisualElement(
                id = obj.optString("id", UUID.randomUUID().toString()),
                domId = obj.optString("domId", ""),
                tagName = obj.optString("tagName", "div"),
                className = obj.optString("className", ""),
                textContent = obj.optString("textContent", ""),
                posX = obj.optDouble("posX", 0.0).toFloat(),
                posY = obj.optDouble("posY", 0.0).toFloat(),
                width = obj.optDouble("width", 100.0).toFloat(),
                height = obj.optDouble("height", 60.0).toFloat(),
                backgroundColor = obj.optString("backgroundColor", "#1E1D22"),
                textColor = obj.optString("textColor", "#FFFFFF"),
                fontSize = obj.optInt("fontSize", 14),
                fontWeight = obj.optString("fontWeight", "normal"),
                textAlign = obj.optString("textAlign", "left"),
                borderRadius = obj.optInt("borderRadius", 8),
                padding = obj.optInt("padding", 12),
                margin = obj.optInt("margin", 0),
                opacity = obj.optDouble("opacity", 1.0).toFloat(),
                display = obj.optString("display", "block"),
                flexDirection = obj.optString("flexDirection", "row"),
                justifyContent = obj.optString("justifyContent", "flex-start"),
                alignItems = obj.optString("alignItems", "center"),
                border = obj.optString("border", "none"),
                boxShadow = obj.optString("boxShadow", "none"),
                zIndex = obj.optInt("zIndex", 1),
                isSelected = true
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extracts a default hierarchical tree of visual elements from HTML structure
     */
    fun extractElementsFromHtml(html: String): List<VisualElement> {
        val list = mutableListOf<VisualElement>()
        val tagRegex = "<([a-zA-Z0-9]+)([^>]*)>([\\s\\S]*?)</\\1>|<([a-zA-Z0-9]+)([^>]*)/>".toRegex()
        
        var index = 0
        tagRegex.findAll(html).forEach { match ->
            val tagName = match.groupValues[1].ifBlank { match.groupValues[4] }.lowercase()
            if (tagName in listOf("html", "head", "body", "script", "style", "meta", "link", "title")) {
                return@forEach
            }
            val attrs = match.groupValues[2].ifBlank { match.groupValues[5] }
            val innerText = match.groupValues[3].replace("<[^>]*>".toRegex(), "").trim()

            val idMatch = "id=[\"']([^\"']+)[\"']".toRegex().find(attrs)
            val classMatch = "class=[\"']([^\"']+)[\"']".toRegex().find(attrs)

            val domId = idMatch?.groupValues?.get(1) ?: ""
            val className = classMatch?.groupValues?.get(1) ?: ""

            list.add(
                VisualElement(
                    id = "elem_$index",
                    domId = domId,
                    tagName = tagName,
                    className = className,
                    textContent = innerText.take(40),
                    posX = (20 + (index % 4) * 60).toFloat(),
                    posY = (30 + index * 40).toFloat(),
                    width = 180f,
                    height = 50f,
                    fontSize = when (tagName) {
                        "h1" -> 26
                        "h2" -> 22
                        "h3" -> 18
                        "button" -> 14
                        else -> 14
                    },
                    fontWeight = if (tagName.startsWith("h") || tagName == "button") "bold" else "normal",
                    backgroundColor = when (tagName) {
                        "button" -> "#6366F1"
                        "card", "section" -> "#1E1D22"
                        else -> "#27262D"
                    },
                    textColor = "#FFFFFF",
                    borderRadius = if (tagName == "button") 8 else 12,
                    padding = 12
                )
            )
            index++
        }

        if (list.isEmpty()) {
            // Default sample elements
            list.add(VisualElement(id = "hero", domId = "hero-title", tagName = "h1", textContent = "Welcome to CodeCraft", fontSize = 28, fontWeight = "bold", textColor = "#FFFFFF", backgroundColor = "transparent"))
            list.add(VisualElement(id = "card", domId = "main-card", tagName = "div", className = "card", textContent = "Interactive Element Card", borderRadius = 12, padding = 16, backgroundColor = "#1E1D22", textColor = "#F1F1F4"))
            list.add(VisualElement(id = "btn", domId = "cta-button", tagName = "button", className = "btn-primary", textContent = "Get Started", borderRadius = 8, padding = 12, backgroundColor = "#6366F1", textColor = "#FFFFFF"))
        }

        return list
    }

    /**
     * Applies visual element edits (position, styling, content) back into the HTML and CSS source strings.
     */
    fun applyElementToCode(
        element: VisualElement,
        htmlContent: String,
        cssContent: String
    ): Pair<String, String> {
        var updatedHtml = htmlContent
        var updatedCss = cssContent

        val selector = when {
            element.domId.isNotBlank() -> "#${element.domId}"
            element.className.isNotBlank() -> ".${element.className.split(" ").first()}"
            else -> element.tagName
        }

        // Check if selector exists in CSS
        val ruleRegex = "$selector\\s*\\{([\\s\\S]*?)\\}".toRegex()
        val newStyleBody = buildString {
            append("\n  background-color: ${element.backgroundColor};")
            append("\n  color: ${element.textColor};")
            append("\n  font-size: ${element.fontSize}px;")
            append("\n  font-weight: ${element.fontWeight};")
            append("\n  text-align: ${element.textAlign};")
            append("\n  border-radius: ${element.borderRadius}px;")
            append("\n  padding: ${element.padding}px;")
            if (element.margin > 0) append("\n  margin: ${element.margin}px;")
            if (element.opacity < 1.0f) append("\n  opacity: ${element.opacity};")
            if (element.border != "none") append("\n  border: ${element.border};")
            if (element.boxShadow != "none") append("\n  box-shadow: ${element.boxShadow};")
            if (element.display == "flex") {
                append("\n  display: flex;")
                append("\n  flex-direction: ${element.flexDirection};")
                append("\n  justify-content: ${element.justifyContent};")
                append("\n  align-items: ${element.alignItems};")
            }
            append("\n")
        }

        updatedCss = if (ruleRegex.containsMatchIn(updatedCss)) {
            updatedCss.replace(ruleRegex, "$selector {$newStyleBody}")
        } else {
            "$updatedCss\n\n$selector {$newStyleBody}"
        }

        return Pair(CodeTransformer.formatCode(updatedHtml, com.example.model.CodeLanguage.HTML), CodeTransformer.formatCode(updatedCss, com.example.model.CodeLanguage.CSS))
    }
}
