package com.playground.kotlin.oop.core

import com.playground.kotlin.oop.patterns.Logger.log
import com.playground.kotlin.oop.patterns.startBackgroundJob

/**
 * OOP Concepts Playground - Main Demo
 *
 * This file demonstrates various Kotlin OOP concepts:
 * - Interfaces (Printable.kt)
 * - Inheritance and polymorphism (Users.kt)
 * - Companion objects and extensions (Users.kt)
 * - Singleton objects (Utils.kt)
 * - Nested and inner classes (Project.kt)
 * - Value classes (Issues.kt)
 * - Sealed classes (Issues.kt)
 * - Class delegation (AuditedList.kt)
 * - Object expressions
 */

/**
 * Main function demonstrating all OOP concepts
 */
fun main() {
    println("=== AuditedList Demo ===")
    val list = AuditedList<String>()
    list.add("Kotlin")
    list.add("Java")
    list.remove("Java")
    list.addAll(listOf("C#", "Python"))
    list[0] = "Kotlin 1.8"
    list.removeAt(1)
    list.clear()

    println("\n=== Logger Demo ===")
    log("This is a test log")

    println("\n=== User & Companion Demo ===")
    val user = User.create("Alice")
    user.printInfo()
    User.printInfo()
    val sysUser = User.systemUser()
    println("System user: ${sysUser.name}")

    println("\n=== User hierarchy Demo ===")
    val admin = Admin("Bob")
    val regular = RegularUser("Charlie")
    admin.printInfo()
    admin.printRole()
    regular.printInfo()
    regular.printRole()

    println("\n=== Background job Demo ===")

    // Anonymous object expression example
    startBackgroundJob(object : Runnable {
        override fun run() {
            println("${Thread.currentThread().name}: Anonymous object expression background job running...")
        }
    })

    // SAM-converted lambda example:
    startBackgroundJob { println("${Thread.currentThread().name}: SAM-converted lambda background job running...") }

    println("\n=== Project Demo ===")
    val project = Project("Kotlin Playground")
    val task = Project.Task("Implement OOP concepts")
    task.printInfo()
    val member = project.Member("Alice")
    member.printInfo()

    println("\n=== Issues Demo ===")
    val bug = Issue.Bug(IssueId.newId(), "App crashes on start", 5)
    val feature = Issue.FeatureRequest(IssueId.newId(), "Add dark mode", "Alice", "dark mode please")
    val ticket = Issue.SupportTicket(IssueId.newId(), "Login not working", "Bob")

    listOf(bug, feature, ticket).forEach { handleIssue(it) }

    println("\n=== AuditedList + Issues Demo ===")
    val issueTitles = AuditedList<String>()
    issueTitles.add(feature.title)
    issueTitles.add("Bug: ${bug.description}")
    issueTitles.add("Ticket: ${ticket.description}")
    println("Issue titles in AuditedList: $issueTitles")
}
