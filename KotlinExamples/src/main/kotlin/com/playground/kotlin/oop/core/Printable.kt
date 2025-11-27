package com.playground.kotlin.oop.core

/**
 * Printable interface demonstrating:
 * - Default method implementations in interfaces
 */
interface Printable {
    fun printInfo() {
        println(this::class.simpleName)
    }
}
