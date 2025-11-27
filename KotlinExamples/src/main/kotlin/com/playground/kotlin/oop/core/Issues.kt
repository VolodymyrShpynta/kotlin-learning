package com.playground.kotlin.oop.core

import com.playground.kotlin.oop.patterns.IdGenerator

/**
 * Issue tracking system demonstrating:
 * - Value classes (inline classes)
 * - Sealed class hierarchies
 * - Custom setters
 * - Init blocks with validation
 * - When expressions for exhaustive type checking
 */

@JvmInline
value class IssueId(val value: String) {
    companion object {
        fun newId() = IssueId(IdGenerator.generateId())
    }
}

sealed class Issue(val id: IssueId, val description: String) {

    // Bug
    class Bug(
        id: IssueId,
        description: String,
        val severity: Int
    ) : Issue(id, description) {
        init {
            require(severity in 1..5) { "Severity must be between 1 and 5, but was $severity" }
        }

        constructor(id: IssueId, description: String) : this(id, description, 3)
    }

    // FeatureRequest
    class FeatureRequest(
        id: IssueId,
        description: String,
        val requestedBy: String,
        title: String
    ) : Issue(id, description) {

        var title: String = title
            set(value) {
                field = value.replaceFirstChar { it.uppercase() }
            }

        init {
            this.title = title // trigger setter for capitalization
        }
    }

    // SupportTicket
    class SupportTicket(
        id: IssueId,
        description: String,
        val customerName: String
    ) : Issue(id, description)
}

// Helper function to handle Issue
fun handleIssue(issue: Issue) {
    when (issue) {
        is Issue.Bug -> println("Bug [${issue.severity}] - ${issue.description} (id=${issue.id.value})")
        is Issue.FeatureRequest -> println("Feature requested by ${issue.requestedBy}: ${issue.title} (id=${issue.id.value})")
        is Issue.SupportTicket -> println("Support ticket from ${issue.customerName}: ${issue.description} (id=${issue.id.value})")
    }
}

