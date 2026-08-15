package com.example.engine

import android.content.Context
import com.example.model.CodeFile
import com.example.model.CodeLanguage
import com.example.model.CodeProject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ProjectManager {

    fun createDefaultProject(): CodeProject {
        return createModernWebAppProject()
    }

    fun createModernWebAppProject(): CodeProject {
        val htmlContent = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>CodeCraft App</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div class="app-container">
    <header class="app-header">
      <div class="brand-badge">
        <span class="pulse-dot"></span>
        <span class="brand-name">CodeCraft Live</span>
      </div>
      <span class="fps-badge" id="fps-counter">120 FPS</span>
    </header>

    <main class="main-content">
      <section class="hero-section" id="hero-card">
        <h1 class="hero-title" id="main-title">Build at Light Speed</h1>
        <p class="hero-subtitle" id="main-desc">Pro code editor with live 120 FPS rendering and Figma-like visual element manipulation.</p>
        <div class="action-row">
          <button class="btn btn-primary" id="counter-btn" onclick="incrementCounter()">
            ⚡ Interacted <span id="click-count">0</span> times
          </button>
          <button class="btn btn-secondary" id="color-btn" onclick="randomizeTheme()">
            🎨 Shuffle Theme
          </button>
        </div>
      </section>

      <section class="stats-grid">
        <div class="stat-card" id="stat-fps">
          <div class="stat-value" id="fps-stat">120</div>
          <div class="stat-label">Target Frame Rate</div>
        </div>
        <div class="stat-card" id="stat-latency">
          <div class="stat-value">0.4ms</div>
          <div class="stat-label">DOM Render Time</div>
        </div>
      </section>

      <section class="canvas-section">
        <div class="canvas-header">
          <span class="canvas-title">Interactive Particle Canvas</span>
          <span class="canvas-status">Touch & Drag Active</span>
        </div>
        <canvas id="particle-canvas" width="340" height="180"></canvas>
      </section>
    </main>
  </div>
  <script src="script.js"></script>
</body>
</html>"""

        val cssContent = """/* CodeCraft Modern Theme */
:root {
  --bg-primary: #0D0D0F;
  --bg-surface: #1E1D22;
  --bg-surface-elevated: #27262D;
  --accent-primary: #6366F1;
  --accent-cyan: #38BDF8;
  --accent-green: #34D399;
  --text-primary: #F1F1F4;
  --text-secondary: #A0A0AB;
  --border-subtle: #2E2D36;
}

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
  -webkit-tap-highlight-color: transparent;
}

body {
  background-color: var(--bg-primary);
  color: var(--text-primary);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  overflow-x: hidden;
  padding: 16px;
}

.app-container {
  max-width: 500px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: var(--bg-surface);
  border-radius: 12px;
  border: 1px solid var(--border-subtle);
}

.brand-badge {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pulse-dot {
  width: 8px;
  height: 8px;
  background: var(--accent-green);
  border-radius: 50%;
  box-shadow: 0 0 8px var(--accent-green);
}

.brand-name {
  font-weight: 700;
  font-size: 14px;
  letter-spacing: 0.5px;
}

.fps-badge {
  background: rgba(56, 189, 248, 0.15);
  color: var(--accent-cyan);
  font-size: 11px;
  font-weight: 700;
  font-family: monospace;
  padding: 4px 8px;
  border-radius: 6px;
  border: 1px solid rgba(56, 189, 248, 0.3);
}

.hero-section {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: 16px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: relative;
  overflow: hidden;
}

.hero-title {
  font-size: 24px;
  font-weight: 800;
  background: linear-gradient(135deg, #FFFFFF 0%, #A0A0AB 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  line-height: 1.2;
}

.hero-subtitle {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.action-row {
  display: flex;
  gap: 10px;
  margin-top: 8px;
  flex-wrap: wrap;
}

.btn {
  padding: 10px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: transform 0.1s ease, filter 0.2s ease;
}

.btn:active {
  transform: scale(0.96);
}

.btn-primary {
  background: var(--accent-primary);
  color: #FFFFFF;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.btn-secondary {
  background: var(--bg-surface-elevated);
  color: var(--text-primary);
  border: 1px solid var(--border-subtle);
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.stat-card {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: 12px;
  padding: 16px;
  text-align: center;
}

.stat-value {
  font-size: 22px;
  font-weight: 800;
  color: var(--accent-cyan);
  font-family: monospace;
}

.stat-label {
  font-size: 11px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.canvas-section {
  background: var(--bg-surface);
  border: 1px solid var(--border-subtle);
  border-radius: 14px;
  padding: 14px;
}

.canvas-header {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

#particle-canvas {
  width: 100%;
  height: 180px;
  background: #000000;
  border-radius: 8px;
  display: block;
}"""

        val jsContent = """// CodeCraft Dynamic Live Script
let count = 0;
function incrementCounter() {
  count++;
  const label = document.getElementById('click-count');
  if (label) label.textContent = count;
  console.log('User interaction logged! Total:', count);
}

function randomizeTheme() {
  const hues = [220, 260, 290, 160, 30, 340];
  const selectedHue = hues[Math.floor(Math.random() * hues.length)];
  document.documentElement.style.setProperty('--accent-primary', 'hsl(' + selectedHue + ', 85%, 60%)');
  console.log('Switched theme accent to HSL:', selectedHue);
}

// 120 FPS Interactive Particle Canvas Engine
const canvas = document.getElementById('particle-canvas');
if (canvas) {
  const ctx = canvas.getContext('2d');
  canvas.width = canvas.clientWidth * (window.devicePixelRatio || 1);
  canvas.height = canvas.clientHeight * (window.devicePixelRatio || 1);

  const particles = [];
  for (let i = 0; i < 40; i++) {
    particles.push({
      x: Math.random() * canvas.width,
      y: Math.random() * canvas.height,
      vx: (Math.random() - 0.5) * 2,
      vy: (Math.random() - 0.5) * 2,
      radius: Math.random() * 3 + 1,
      color: Math.random() > 0.5 ? '#6366F1' : '#38BDF8'
    });
  }

  function renderLoop() {
    ctx.fillStyle = 'rgba(13, 13, 15, 0.25)';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    for (let i = 0; i < particles.length; i++) {
      const p = particles[i];
      p.x += p.vx;
      p.y += p.vy;

      if (p.x < 0 || p.x > canvas.width) p.vx *= -1;
      if (p.y < 0 || p.y > canvas.height) p.vy *= -1;

      ctx.beginPath();
      ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
      ctx.fillStyle = p.color;
      ctx.fill();

      for (let j = i + 1; j < particles.length; j++) {
        const p2 = particles[j];
        const dx = p.x - p2.x;
        const dy = p.y - p2.y;
        const dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < 60) {
          ctx.strokeStyle = 'rgba(99, 102, 241, ' + (1 - dist / 60) * 0.3 + ')';
          ctx.lineWidth = 0.8;
          ctx.beginPath();
          ctx.moveTo(p.x, p.y);
          ctx.lineTo(p2.x, p2.y);
          ctx.stroke();
        }
      }
    }
    requestAnimationFrame(renderLoop);
  }
  requestAnimationFrame(renderLoop);
}"""

        val jsonConfig = """{
  "name": "CodeCraft Web Project",
  "version": "1.0.0",
  "targetFps": 120,
  "theme": "pro-dark",
  "dependencies": []
}"""

        return CodeProject(
            name = "Web App Starter",
            description = "Interactive 120 FPS Web App with responsive layout & particles",
            files = listOf(
                CodeFile(name = "index.html", path = "/index.html", content = htmlContent),
                CodeFile(name = "style.css", path = "/style.css", content = cssContent),
                CodeFile(name = "script.js", path = "/script.js", content = jsContent),
                CodeFile(name = "package.json", path = "/package.json", content = jsonConfig)
            )
        )
    }

    fun create120FpsGameProject(): CodeProject {
        val gameHtml = """<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
  <title>Neon Arcade 120FPS</title>
  <style>
    body { margin: 0; background: #070709; color: #FFF; font-family: monospace; overflow: hidden; touch-action: none; }
    #hud { position: absolute; top: 12px; left: 16px; font-size: 16px; font-weight: bold; z-index: 10; pointer-events: none; }
    #fps { color: #38BDF8; }
    #score { color: #34D399; margin-left: 12px; }
    canvas { display: block; width: 100vw; height: 100vh; }
  </style>
</head>
<body>
  <div id="hud">⚡ <span id="fps">120 FPS</span> | SCORE: <span id="score">0</span></div>
  <canvas id="gameCanvas"></canvas>
  <script>
    const canvas = document.getElementById('gameCanvas');
    const ctx = canvas.getContext('2d');
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;

    let score = 0;
    const player = { x: canvas.width / 2, y: canvas.height - 80, size: 24, color: '#6366F1' };
    const stars = Array.from({length: 60}, () => ({ x: Math.random()*canvas.width, y: Math.random()*canvas.height, speed: Math.random()*3+1 }));
    const targets = [];

    window.addEventListener('touchmove', (e) => {
      if (e.touches.length > 0) {
        player.x = e.touches[0].clientX;
        player.y = e.touches[0].clientY;
      }
    });

    setInterval(() => {
      targets.push({ x: Math.random() * (canvas.width - 40) + 20, y: -20, speed: Math.random()*2+2, size: 18, color: '#F43F5E' });
    }, 600);

    function loop() {
      ctx.fillStyle = '#070709';
      ctx.fillRect(0, 0, canvas.width, canvas.height);

      // Stars
      ctx.fillStyle = 'rgba(255,255,255,0.7)';
      stars.forEach(s => {
        s.y += s.speed;
        if (s.y > canvas.height) s.y = 0;
        ctx.fillRect(s.x, s.y, 2, 2);
      });

      // Player
      ctx.fillStyle = player.color;
      ctx.shadowBlur = 15;
      ctx.shadowColor = player.color;
      ctx.beginPath();
      ctx.arc(player.x, player.y, player.size, 0, Math.PI*2);
      ctx.fill();
      ctx.shadowBlur = 0;

      // Targets
      for (let i = targets.length - 1; i >= 0; i--) {
        const t = targets[i];
        t.y += t.speed;
        ctx.fillStyle = t.color;
        ctx.beginPath();
        ctx.arc(t.x, t.y, t.size, 0, Math.PI*2);
        ctx.fill();

        const dist = Math.hypot(t.x - player.x, t.y - player.y);
        if (dist < player.size + t.size) {
          score += 10;
          document.getElementById('score').innerText = score;
          targets.splice(i, 1);
        } else if (t.y > canvas.height + 40) {
          targets.splice(i, 1);
        }
      }
      requestAnimationFrame(loop);
    }
    requestAnimationFrame(loop);
  </script>
</body>
</html>"""

        return CodeProject(
            name = "120FPS Neon Game",
            description = "High refresh rate arcade game with touch physics",
            files = listOf(
                CodeFile(name = "index.html", path = "/index.html", content = gameHtml)
            )
        )
    }

    /**
     * Unpacks ZIP / archive byte stream into project file tree
     */
    fun unpackZipArchive(inputStream: InputStream, archiveName: String): CodeProject {
        val files = mutableListOf<CodeFile>()
        val zipIn = ZipInputStream(inputStream)
        var entry: ZipEntry? = zipIn.nextEntry

        while (entry != null) {
            if (!entry.isDirectory) {
                val entryName = entry.name.removePrefix("/")
                val byteOut = ByteArrayOutputStream()
                val buffer = ByteArray(4096)
                var len: Int
                while (zipIn.read(buffer).also { len = it } > 0) {
                    byteOut.write(buffer, 0, len)
                }
                val content = String(byteOut.toByteArray(), Charsets.UTF_8)
                files.add(
                    CodeFile(
                        name = entryName.substringAfterLast('/'),
                        path = "/$entryName",
                        content = content
                    )
                )
            }
            zipIn.closeEntry()
            entry = zipIn.nextEntry
        }
        zipIn.close()

        val projectName = archiveName.substringBeforeLast('.').ifBlank { "Imported Project" }
        return CodeProject(
            name = projectName,
            description = "Imported from $archiveName (${files.size} files)",
            files = files.ifEmpty { createDefaultProject().files }
        )
    }

    /**
     * Packages all project files into a downloadable ZIP archive
     */
    fun exportProjectAsZip(project: CodeProject): ByteArray {
        val byteOut = ByteArrayOutputStream()
        val zipOut = ZipOutputStream(byteOut)

        for (file in project.files) {
            if (!file.isDirectory) {
                val cleanPath = file.path.removePrefix("/")
                val entry = ZipEntry(cleanPath)
                zipOut.putNextEntry(entry)
                zipOut.write(file.content.toByteArray(Charsets.UTF_8))
                zipOut.closeEntry()
            }
        }
        zipOut.close()
        return byteOut.toByteArray()
    }
}
