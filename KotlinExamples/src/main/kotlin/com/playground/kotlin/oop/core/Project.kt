package com.playground.kotlin.oop.core

/**
 * Project class demonstrating:
 * - Nested data classes
 * - Inner classes (with access to outer class)
 */
class Project(val name: String) {

    data class Task(val description: String) : Printable {
        override fun printInfo() {
            println("Project with description: $description")
        }
    }

    inner class Member(val username: String) : Printable {
        override fun printInfo() {
            println("User $username is a member of $name project")
        }
    }
}
