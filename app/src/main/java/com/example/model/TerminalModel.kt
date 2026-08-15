package com.example.model

enum class LogLevel {
    INFO,
    WARN,
    ERROR,
    CMD,
    SUCCESS,
    SYSTEM
}

data class TerminalLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel = LogLevel.INFO,
    val message: String,
    val source: String = "system"
)

data class ServerNodeInfo(
    val name: String,
    val region: String,
    val latencyMs: Int,
    val status: NodeHealth,
    val loadPercentage: Int
)

enum class NodeHealth {
    HEALTHY,
    DEGRADED,
    OPTIMIZING
}
