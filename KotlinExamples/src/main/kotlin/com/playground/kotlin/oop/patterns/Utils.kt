package com.playground.kotlin.oop.patterns

import java.util.UUID

/**
 * Singleton objects demonstrating:
 * - Internal visibility modifier
 * - Object declarations (singleton pattern)
 */
internal object IdGenerator {
    fun generateId() = UUID.randomUUID().toString()
}

object Logger {
    fun log(msg: String) {
        println("[LOG] $msg")
    }
}

// Helper function for object expression demo
fun startBackgroundJob(runnable: Runnable) {
    Thread(runnable).start()
}
