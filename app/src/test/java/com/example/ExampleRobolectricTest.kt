package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.CodeTransformer
import com.example.engine.ProjectManager
import com.example.model.CodeLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CodeCraft", appName)
  }

  @Test
  fun `test code formatting`() {
    val rawHtml = "<div><h1>Title</h1><p>Desc</p></div>"
    val formatted = CodeTransformer.formatCode(rawHtml, CodeLanguage.HTML)
    assertTrue(formatted.contains("<div>"))
    assertTrue(formatted.contains("<h1>Title</h1>"))
  }

  @Test
  fun `test code embed and split`() {
    val html = "<!DOCTYPE html><html><head><title>Test</title></head><body><h1>Hello</h1></body></html>"
    val css = "h1 { color: red; }"
    val js = "console.log('hi');"

    val embedded = CodeTransformer.embedCode(html, css, js)
    assertTrue(embedded.contains("<style>"))
    assertTrue(embedded.contains("<script>"))

    val (splitHtml, splitCss, splitJs) = CodeTransformer.splitCode(embedded)
    assertTrue(splitHtml.contains("style.css"))
    assertTrue(splitHtml.contains("script.js"))
    assertTrue(splitCss.contains("color: red;"))
    assertTrue(splitJs.contains("console.log('hi');"))
  }

  @Test
  fun `test default project creation`() {
    val project = ProjectManager.createDefaultProject()
    assertTrue(project.files.isNotEmpty())
    assertTrue(project.files.any { it.name == "index.html" })
  }
}
