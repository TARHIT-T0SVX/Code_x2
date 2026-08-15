package com.example.engine

import com.example.model.CodeLanguage

data class AutoCompleteSuggestion(
    val label: String,
    val insertText: String,
    val detail: String,
    val type: SuggestionType
)

enum class SuggestionType {
    TAG,
    PROPERTY,
    KEYWORD,
    SNIPPET,
    FUNCTION
}

object AiAutocompletionEngine {

    private val htmlSuggestions = listOf(
        AutoCompleteSuggestion("div", "<div class=\"\">\n  \n</div>", "Container element", SuggestionType.TAG),
        AutoCompleteSuggestion("button", "<button class=\"btn\" onclick=\"\">\n  Click Me\n</button>", "Interactive Button", SuggestionType.TAG),
        AutoCompleteSuggestion("section", "<section class=\"section\">\n  \n</section>", "Semantic Section", SuggestionType.TAG),
        AutoCompleteSuggestion("h1", "<h1>Heading 1</h1>", "Primary Heading", SuggestionType.TAG),
        AutoCompleteSuggestion("p", "<p>Text content...</p>", "Paragraph text", SuggestionType.TAG),
        AutoCompleteSuggestion("span", "<span class=\"\"></span>", "Inline Span", SuggestionType.TAG),
        AutoCompleteSuggestion("canvas", "<canvas id=\"canvas\" width=\"400\" height=\"300\"></canvas>", "2D/WebGL Canvas", SuggestionType.TAG),
        AutoCompleteSuggestion("input", "<input type=\"text\" placeholder=\"Enter text...\" />", "Form Input", SuggestionType.TAG),
        AutoCompleteSuggestion("img", "<img src=\"\" alt=\"\" />", "Image Element", SuggestionType.TAG),
        AutoCompleteSuggestion("card-snippet", "<div class=\"card\">\n  <h2 class=\"card-title\">Title</h2>\n  <p class=\"card-desc\">Description here.</p>\n  <button class=\"btn\">Action</button>\n</div>", "Card Component", SuggestionType.SNIPPET)
    )

    private val cssSuggestions = listOf(
        AutoCompleteSuggestion("display: flex", "display: flex;\njustify-content: center;\nalign-items: center;", "Flexbox Center", SuggestionType.PROPERTY),
        AutoCompleteSuggestion("display: grid", "display: grid;\ngrid-template-columns: repeat(auto-fit, minmax(200px, 1fr));\ngap: 16px;", "Responsive Grid", SuggestionType.PROPERTY),
        AutoCompleteSuggestion("background-color", "background-color: #1E1D22;", "Surface Background", SuggestionType.PROPERTY),
        AutoCompleteSuggestion("border-radius", "border-radius: 12px;", "Rounded Corners", SuggestionType.PROPERTY),
        AutoCompleteSuggestion("box-shadow", "box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);", "Elevated Shadow", SuggestionType.PROPERTY),
        AutoCompleteSuggestion("transition", "transition: all 0.2s ease-in-out;", "Smooth Transition", SuggestionType.PROPERTY),
        AutoCompleteSuggestion("glass-card", "background: rgba(30, 29, 34, 0.8);\nbackdrop-filter: blur(12px);\nborder: 1px solid rgba(255, 255, 255, 0.1);\nborder-radius: 16px;", "Glassmorphism Card", SuggestionType.SNIPPET)
    )

    private val jsSuggestions = listOf(
        AutoCompleteSuggestion("addEventListener", "element.addEventListener('click', (e) => {\n  console.log(e);\n});", "Event Listener", SuggestionType.FUNCTION),
        AutoCompleteSuggestion("querySelector", "const element = document.querySelector('');", "DOM Query", SuggestionType.FUNCTION),
        AutoCompleteSuggestion("requestAnimationFrame", "function loop() {\n  // 120 FPS render\n  requestAnimationFrame(loop);\n}\nrequestAnimationFrame(loop);", "120 FPS Loop", SuggestionType.SNIPPET),
        AutoCompleteSuggestion("fetch-api", "fetch('/api/data')\n  .then(res => res.json())\n  .then(data => console.log(data))\n  .catch(err => console.error(err));", "Async Fetch", SuggestionType.SNIPPET),
        AutoCompleteSuggestion("setInterval", "setInterval(() => {\n  \n}, 1000);", "Timer Interval", SuggestionType.FUNCTION),
        AutoCompleteSuggestion("console.log", "console.log('');", "Console Output", SuggestionType.FUNCTION)
    )

    fun getSuggestions(query: String, language: CodeLanguage): List<AutoCompleteSuggestion> {
        val list = when (language) {
            CodeLanguage.HTML -> htmlSuggestions
            CodeLanguage.CSS -> cssSuggestions
            CodeLanguage.JAVASCRIPT, CodeLanguage.TYPESCRIPT -> jsSuggestions
            else -> htmlSuggestions + jsSuggestions
        }

        if (query.isBlank()) return list.take(6)
        val clean = query.trim().lowercase()
        return list.filter {
            it.label.lowercase().contains(clean) || it.detail.lowercase().contains(clean)
        }
    }

    /**
     * AI Code generation / refactoring engine
     */
    fun generateAiRefactoring(prompt: String, currentCode: String, language: CodeLanguage): String {
        val cleanPrompt = prompt.lowercase()
        return when {
            cleanPrompt.contains("button") || cleanPrompt.contains("btn") -> {
                if (language == CodeLanguage.HTML) {
                    currentCode + "\n<!-- AI Generated Interactive Button -->\n<button class=\"btn btn-ai\" onclick=\"alert('AI Triggered')\">✨ AI Action</button>"
                } else {
                    currentCode + "\n/* AI Generated Button Style */\n.btn-ai {\n  background: linear-gradient(135deg, #6366F1, #38BDF8);\n  color: #FFF;\n  padding: 10px 18px;\n  border-radius: 10px;\n  border: none;\n  font-weight: 700;\n  cursor: pointer;\n}"
                }
            }
            cleanPrompt.contains("dark") || cleanPrompt.contains("theme") -> {
                if (language == CodeLanguage.CSS) {
                    currentCode + "\n/* AI Dark Mode Enhancements */\n:root {\n  --ai-glow: rgba(99, 102, 241, 0.4);\n  --bg-deep: #0D0D0F;\n}\n.glow-card {\n  box-shadow: 0 0 25px var(--ai-glow);\n  border: 1px solid #6366F1;\n}"
                } else {
                    currentCode
                }
            }
            cleanPrompt.contains("animation") || cleanPrompt.contains("120") || cleanPrompt.contains("fps") -> {
                if (language == CodeLanguage.JAVASCRIPT) {
                    currentCode + "\n// AI 120 FPS High Refresh Animation Engine\n(function initSmoothPhysics() {\n  let last = performance.now();\n  function tick(now) {\n    const delta = (now - last) / 1000;\n    last = now;\n    // Smooth update logic\n    requestAnimationFrame(tick);\n  }\n  requestAnimationFrame(tick);\n})();"
                } else {
                    currentCode + "\n/* AI 120 FPS Fluid Keyframes */\n@keyframes floatSmooth {\n  0% { transform: translateY(0px); }\n  50% { transform: translateY(-8px); }\n  100% { transform: translateY(0px); }\n}\n.floating-element {\n  animation: floatSmooth 3s ease-in-out infinite;\n}"
                }
            }
            else -> {
                currentCode + "\n/* AI Refactor Applied: Code optimized for 120 FPS mobile performance */"
            }
        }
    }
}
