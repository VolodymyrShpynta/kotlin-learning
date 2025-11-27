package com.playground.kotlin.basics.receivers

import java.util.concurrent.locks.ReentrantLock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests demonstrating Kotlin receiver-style functions (lambdas with receivers).
 *
 * Key concepts:
 * - Extension lambdas (T.() -> R)
 * - run, with, apply scope functions
 * - Builder pattern with receivers
 * - Resource management with receivers
 * - Type-safe builders
 */
class ReceiverStyleFunctionsPlaygroundTests {

    // ==================== run() Tests ====================

    @Test
    fun testRunExtensionFunctionModifiesReceiver() {
        val list = mutableListOf(1, 2, 3)

        list.run {
            add(4)
            add(5)
            remove(1)
        }

        assertEquals(listOf(2, 3, 4, 5), list)
    }

    @Test
    fun testRunExtensionFunctionReturnsResult() {
        val list = mutableListOf(1, 2, 3)

        val size = list.run {
            add(4)
            add(5)
            size // returns the size
        }

        assertEquals(5, size)
    }

    @Test
    fun testRunWithReceiverAsThis() {
        val result = "Kotlin".run {
            // 'this' refers to the String "Kotlin"
            uppercase() + " IS AWESOME"
        }

        assertEquals("KOTLIN IS AWESOME", result)
    }

    // ==================== with() Tests ====================

    @Test
    fun testWithFunctionModifiesReceiver() {
        val list = mutableListOf(1, 2, 3)

        with(list) {
            add(6)
            add(7)
            remove(2)
        }

        assertEquals(listOf(1, 3, 6, 7), list)
    }

    @Test
    fun testWithFunctionReturnsLastExpression() {
        val list = mutableListOf("A", "B", "C")

        val result = with(list) {
            add("D")
            "Added D to list of size $size"
        }

        assertEquals("Added D to list of size 4", result)
    }

    @Test
    fun testWithVsRunDifference() {
        val list = mutableListOf(1, 2, 3)

        // with() is a regular function (not an extension)
        val withResult = with(list) { size }

        // run() is an extension function
        val runResult = list.run { size }

        assertEquals(withResult, runResult)
    }

    // ==================== apply() Tests ====================

    @Test
    fun testApplyModifiesAndReturnsReceiver() {
        val list = mutableListOf(1, 2, 3)

        val result = list.apply {
            add(8)
            add(9)
            remove(3)
        }

        // apply returns the receiver itself
        assertSame(list, result)
        assertEquals(listOf(1, 2, 8, 9), list)
    }

    @Test
    fun testApplyChainingForFluentAPI() {
        val list = mutableListOf<Int>()

        list.apply {
            add(1)
            add(2)
        }.apply {
            add(3)
            add(4)
        }.apply {
            removeFirst()
        }

        assertEquals(listOf(2, 3, 4), list)
    }

    @Test
    fun testApplyForObjectConfiguration() {
        data class Person(var name: String = "", var age: Int = 0)

        val person = Person().apply {
            name = "Alice"
            age = 30
        }

        assertEquals("Alice", person.name)
        assertEquals(30, person.age)
    }

    // ==================== buildString() Tests ====================

    @Test
    fun testBuildStringCreatesStringFromBuilder() {
        val result = buildString {
            append("Hello")
            append(" ")
            append("Kotlin")
        }

        assertEquals("Hello Kotlin", result)
    }

    @Test
    fun testBuildStringWithConditionalLogic() {
        val result = buildString {
            append("Start")
            if (true) {
                append(" - Condition met")
            }
            append(" - End")
        }

        assertEquals("Start - Condition met - End", result)
    }

    @Test
    fun testBuildStringWithLoops() {
        val result = buildString {
            repeat(3) { i ->
                append("Item $i")
                if (i < 2) append(", ")
            }
        }

        assertEquals("Item 0, Item 1, Item 2", result)
    }

    // ==================== buildList() Tests ====================

    @Test
    fun testBuildListCreatesImmutableList() {
        val list = buildList {
            add(1)
            add(2)
            add(3)
        }

        assertEquals(listOf(1, 2, 3), list)
    }

    @Test
    fun testBuildListWithConditionalElements() {
        val list = buildList {
            add(1)
            if (true) add(2)
            if (false) add(3)
            add(4)
        }

        assertEquals(listOf(1, 2, 4), list)
    }

    @Test
    fun testBuildListWithLoop() {
        val list = buildList {
            for (i in 1..5) {
                if (i % 2 == 0) add(i)
            }
        }

        assertEquals(listOf(2, 4), list)
    }

    @Test
    fun testBuildListWithAddAll() {
        val list = buildList {
            addAll(listOf(1, 2, 3))
            add(4)
            addAll(listOf(5, 6))
        }

        assertEquals(listOf(1, 2, 3, 4, 5, 6), list)
    }

    // ==================== buildSet() Tests ====================

    @Test
    fun testBuildSetCreatesImmutableSet() {
        val set = buildSet {
            add(11)
            add(22)
            add(33)
        }

        assertEquals(setOf(11, 22, 33), set)
    }

    @Test
    fun testBuildSetRemovesDuplicates() {
        val set = buildSet {
            add(1)
            add(2)
            add(1) // duplicate
            add(3)
            add(2) // duplicate
        }

        assertEquals(setOf(1, 2, 3), set)
        assertEquals(3, set.size)
    }

    @Test
    fun testBuildSetWithAddAll() {
        val set = buildSet {
            addAll(listOf(1, 2, 2, 3))
            add(4)
        }

        assertEquals(setOf(1, 2, 3, 4), set)
    }

    // ==================== buildMap() Tests ====================

    @Test
    fun testBuildMapCreatesImmutableMap() {
        val map = buildMap {
            put("one", 1)
            put("two", 2)
            put("three", 3)
        }

        assertEquals(mapOf("one" to 1, "two" to 2, "three" to 3), map)
    }

    @Test
    fun testBuildMapOverwritesDuplicateKeys() {
        val map = buildMap {
            put("key", 1)
            put("key", 2) // overwrites
        }

        assertEquals(mapOf("key" to 2), map)
        assertEquals(1, map.size)
    }

    @Test
    fun testBuildMapWithIndexedOperator() {
        val map = buildMap<String, Int> {
            this["a"] = 1
            this["b"] = 2
            this["c"] = 3
        }

        assertEquals(mapOf("a" to 1, "b" to 2, "c" to 3), map)
    }

    @Test
    fun testBuildMapWithPutAll() {
        val map = buildMap {
            putAll(mapOf("x" to 10, "y" to 20))
            put("z", 30)
        }

        assertEquals(mapOf("x" to 10, "y" to 20, "z" to 30), map)
    }

    // ==================== useResource() Tests ====================

    @Test
    fun testUseResourceWithMutableList() {
        val initialList = mutableListOf("A", "B", "C")

        useResource(initialList) {
            add("D")
            remove("A")
        }

        assertEquals(listOf("B", "C", "D"), initialList)
    }

    @Test
    fun testUseResourceWithStringBuilder() {
        val sb = StringBuilder("Hello")

        useResource(sb) {
            append(" ")
            append("World")
        }

        assertEquals("Hello World", sb.toString())
    }

    @Test
    fun testUseResourceAccessesReceiverMembers() {
        val list = mutableListOf(1, 2, 3)

        useResource(list) {
            // Can access 'this' (the list)
            clear()
            addAll(listOf(10, 20, 30))
        }

        assertEquals(listOf(10, 20, 30), list)
    }

    // ==================== withLock() Tests ====================

    @Test
    fun testWithLockAcquiresAndReleasesLock() {
        val lock = ReentrantLock()
        var insideLock = false

        withLock(lock) {
            insideLock = isHeldByCurrentThread
        }

        assertTrue(insideLock, "Lock should be held inside withLock")
        assertFalse(lock.isHeldByCurrentThread, "Lock should be released after withLock")
    }

    @Test
    fun testWithLockReturnsValue() {
        val lock = ReentrantLock()

        val result = withLock(lock) {
            "Lock is held: $isHeldByCurrentThread"
        }

        assertTrue(result.contains("true"))
        assertFalse(lock.isHeldByCurrentThread)
    }

    @Test
    fun testWithLockReleasesOnException() {
        val lock = ReentrantLock()

        try {
            withLock(lock) {
                throw RuntimeException("Test exception")
            }
        } catch (e: RuntimeException) {
            // Expected
        }

        assertFalse(lock.isHeldByCurrentThread, "Lock should be released even after exception")
    }

    @Test
    fun testLockExtensionRunFunction() {
        val lock = ReentrantLock()
        var insideLock = false

        lock.run {
            insideLock = isHeldByCurrentThread
        }

        assertTrue(insideLock, "Lock should be held inside lock.run")
        assertFalse(lock.isHeldByCurrentThread, "Lock should be released after lock.run")
    }

    // ==================== Advanced Use Cases ====================

    @Test
    fun testNestedReceiverFunctions() {
        val result = buildString {
            append("List: ")
            val list = buildList {
                add(1)
                add(2)
                add(3)
            }
            append(list.toString())
        }

        assertEquals("List: [1, 2, 3]", result)
    }

    @Test
    fun testReceiverFunctionWithComplexChaining() {
        val result = mutableListOf<Int>()
            .apply {
                addAll(listOf(1, 2, 3))
            }
            .run {
                filter { it > 1 }
            }
            .apply {
                println("Filtered list: $this")
            }

        assertEquals(listOf(2, 3), result)
    }

    @Test
    fun testBuildListWithNestedBuilders() {
        val list = buildList {
            add(buildString {
                append("Item ")
                append(1)
            })
            add(buildString {
                append("Item ")
                append(2)
            })
        }

        assertEquals(listOf("Item 1", "Item 2"), list)
    }

    @Test
    fun testReceiverStyleForDSLPattern() {
        // Simulating a DSL-style builder
        class Config {
            var name: String = ""
            var timeout: Int = 0
            val items = mutableListOf<String>()
        }

        fun configure(block: Config.() -> Unit): Config {
            return Config().apply(block)
        }

        val config = configure {
            name = "MyConfig"
            timeout = 5000
            items.add("item1")
            items.add("item2")
        }

        assertEquals("MyConfig", config.name)
        assertEquals(5000, config.timeout)
        assertEquals(listOf("item1", "item2"), config.items)
    }

    @Test
    fun testReceiverFunctionAsCallbackPattern() {
        fun processList(items: List<Int>, action: MutableList<Int>.() -> Unit): List<Int> {
            return items.toMutableList().apply(action)
        }

        val result = processList(listOf(1, 2, 3)) {
            add(4)
            removeFirst()
        }

        assertEquals(listOf(2, 3, 4), result)
    }

    @Test
    fun testRunVsApplyReturnValueDifference() {
        val list = mutableListOf(1, 2, 3)

        // run returns the lambda result
        val runResult: Int = list.run {
            add(4)
            size // returns Int
        }

        // apply returns the receiver
        val applyResult: MutableList<Int> = list.apply {
            add(5)
            size // this is ignored, returns MutableList
        }

        assertEquals(4, runResult)
        assertSame(applyResult, list)
        assertEquals(5, list.size)
    }

    @Test
    fun testReceiverFunctionForObjectInitialization() {
        data class DatabaseConfig(
            var host: String = "localhost",
            var port: Int = 5432,
            var username: String = "",
            var password: String = ""
        )

        fun createDbConfig(init: DatabaseConfig.() -> Unit): DatabaseConfig {
            return DatabaseConfig().apply(init)
        }

        val config = createDbConfig {
            host = "db.example.com"
            port = 3306
            username = "admin"
            password = "secret"
        }

        assertEquals("db.example.com", config.host)
        assertEquals(3306, config.port)
    }
}
