import java.util.UUID

// ----------------------
// Printable interface
// ----------------------
interface Printable {
    fun printInfo() {
        println(this::class.simpleName)
    }
}

// ----------------------
// User hierarchy
// ----------------------
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

// ----------------------
// Singleton object
// ----------------------
internal object IdGenerator {
    fun generateId() = UUID.randomUUID().toString()
}

object Logger {
    fun log(msg: String) {
        println("[LOG] $msg")
    }
}

// ----------------------
// Project
// ----------------------
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

// ----------------------
// Inline class: IssueId
// ----------------------
@JvmInline
value class IssueId(val value: String) {
    companion object {
        fun newId() = IssueId(IdGenerator.generateId())
    }
}

// ----------------------
// Sealed class hierarchy
// ----------------------
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

// ----------------------
// Helper function to handle Issue
// ----------------------
fun handleIssue(issue: Issue) {
    when (issue) {
        is Issue.Bug -> println("Bug [${issue.severity}] - ${issue.description} (id=${issue.id.value})")
        is Issue.FeatureRequest -> println("Feature requested by ${issue.requestedBy}: ${issue.title} (id=${issue.id.value})")
        is Issue.SupportTicket -> println("Support ticket from ${issue.customerName}: ${issue.description} (id=${issue.id.value})")
    }
}

// ----------------------
// Class delegation: AuditedList
// ----------------------
class AuditedList<T>(private val innerList: MutableList<T> = ArrayList()) : MutableList<T> by innerList {

    private fun log(action: String) = println("AuditedList modification: $action")

    override fun add(element: T): Boolean {
        log("add($element)")
        return innerList.add(element)
    }

    override fun remove(element: T): Boolean {
        log("remove($element)")
        return innerList.remove(element)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        log("addAll($elements)")
        return innerList.addAll(elements)
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        log("removeAll($elements)")
        return innerList.removeAll(elements)
    }

    override fun clear() {
        log("clear()")
        innerList.clear()
    }

    override fun set(index: Int, element: T): T {
        log("set($index, $element)")
        return innerList.set(index, element)
    }

    override fun add(index: Int, element: T) {
        log("add($index, $element)")
        innerList.add(index, element)
    }

    override fun removeAt(index: Int): T {
        log("removeAt($index)")
        return innerList.removeAt(index)
    }
}

// ----------------------
// Object expression: Runnable
// ----------------------
fun startBackgroundJob(runnable: Runnable) {
    Thread(runnable).start()
}

// ----------------------
// Main function: usage examples
// ----------------------
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
    Logger.log("This is a test log")

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
