package com.playground.kotlin.oop.core

/**
 * User hierarchy demonstrating:
 * - Open classes and inheritance
 * - Companion objects
 * - Companion object extensions
 * - Method overriding
 */
open class User(val name: String) : Printable {
    fun printRole() = println("User's role is ${role()}")
    open fun role(): String = "Reader"

    // Companion object
    companion object : Printable {
        fun create(name: String): User = User(name)
        override fun printInfo() {
            println("User.Companion: factory for creating users")
        }
    }
}

// Companion object extension
fun User.Companion.systemUser(): User = User("system")

class Admin(name: String) : User(name) {
    override fun role(): String = "Admin"
    override fun printInfo() = println("Admin user $name with ${role()} role.")
}

class RegularUser(name: String) : User(name) {
    override fun printInfo() = println("Regular user $name with ${role()} role.")
}
