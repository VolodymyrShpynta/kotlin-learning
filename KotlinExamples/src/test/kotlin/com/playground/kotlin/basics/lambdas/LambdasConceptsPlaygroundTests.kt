package com.playground.kotlin.basics.lambdas

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests demonstrating Kotlin lambda concepts and functional programming.
 *
 * Key concepts:
 * - Fun interfaces (SAM interfaces)
 * - Lambda syntax variations
 * - Trailing lambda syntax
 * - Lambda type inference
 * - Function references
 * - Higher-order functions
 * - Collection operations (map, filter, etc.)
 * - Lazy evaluation with sequences
 */
class LambdasConceptsPlaygroundTests {

    // ==================== Fun Interface (SAM) Tests ====================

    @Test
    fun testFunInterfaceWithLambda() {
        val isEven = IntCondition { it % 2 == 0 }

        assertTrue(isEven.check(2))
        assertTrue(isEven.check(4))
        assertFalse(isEven.check(3))
        assertFalse(isEven.check(5))
    }

    @Test
    fun testFunInterfaceCheckString() {
        val isPositive = IntCondition { it > 0 }

        assertTrue(isPositive.checkString("42"))
        assertTrue(isPositive.checkString("1"))
        assertFalse(isPositive.checkString("-5"))
        assertFalse(isPositive.checkString("0"))
    }

    @Test
    fun testFunInterfaceCheckStringThrowsOnInvalidInput() {
        val condition = IntCondition { it > 0 }

        assertFailsWith<NumberFormatException> {
            condition.checkString("not a number")
        }

        assertFailsWith<NumberFormatException> {
            condition.checkString("12.5")
        }
    }

    @Test
    fun testFunInterfaceCheckChar() {
        val isGreaterThanFive = IntCondition { it > 5 }

        assertTrue(isGreaterThanFive.checkChar('7'))
        assertTrue(isGreaterThanFive.checkChar('9'))
        assertFalse(isGreaterThanFive.checkChar('3'))
        assertFalse(isGreaterThanFive.checkChar('5'))
    }

    @Test
    fun testFunInterfaceCheckCharThrowsOnInvalidInput() {
        val condition = IntCondition { it > 0 }

        assertFailsWith<IllegalArgumentException> {
            condition.checkChar('a')
        }

        assertFailsWith<IllegalArgumentException> {
            condition.checkChar('Z')
        }
    }

    // ==================== Lambda Syntax Variations Tests ====================

    @Test
    fun testExplicitLambdaCast() {
        // From main method - explicit cast (rarely needed)
        val result = checkCondition(5, { i: Int -> i % 2 != 0 } as (Int) -> Boolean)
        assertTrue(result)
    }

    @Test
    fun testTrailingLambdaWithExplicitType() {
        // From main method - trailing lambda with explicit parameter type
        val result = checkCondition(5) { i: Int -> i % 2 != 0 }
        assertTrue(result)
    }

    @Test
    fun testTrailingLambdaWithInferredType() {
        // From main method - inferred type with named parameter
        val result = checkCondition(5) { i -> i % 2 != 0 }
        assertTrue(result)
    }

    @Test
    fun testTrailingLambdaWithItParameter() {
        // Using 'it' for single parameter
        val result = checkCondition(5) { it % 2 != 0 }
        assertTrue(result)
    }

    @Test
    fun testLambdaVariableReuse() {
        // From main method - reusing lambda via variable
        val isOdd: (Int) -> Boolean = { it % 2 != 0 }

        assertTrue(checkCondition(5, IntCondition(isOdd)))
        assertTrue(checkCondition(7, IntCondition(isOdd)))
        assertFalse(checkCondition(4, IntCondition(isOdd)))
    }

    @Test
    fun testSAMConstructorWithLambdaVariable() {
        // From main method - SAM constructor with lambda variable
        val isOdd: (Int) -> Boolean = { it % 2 != 0 }

        assertTrue(checkCondition("5", IntCondition(isOdd)))
        assertFalse(checkCondition("8", IntCondition(isOdd)))
    }

    @Test
    fun testSAMConstructorWithCheckChar() {
        // From main method - SAM constructor and helper method
        val result = IntCondition { it % 2 != 0 }.checkChar('5')
        assertTrue(result)
    }

    // ==================== checkCondition Overloads Tests ====================

    @Test
    fun testCheckConditionWithInt() {
        assertTrue(checkCondition(10) { it % 2 == 0 })
        assertFalse(checkCondition(11) { it % 2 == 0 })
    }

    @Test
    fun testCheckConditionWithIntCondition() {
        val isNegative = IntCondition { it < 0 }

        assertTrue(checkCondition(-5, isNegative))
        assertFalse(checkCondition(5, isNegative))
    }

    @Test
    fun testCheckConditionWithStringPredicate() {
        // From main method - string predicate (no parsing)
        assertTrue(checkCondition("Kotlin") { it.length == 6 })
        assertFalse(checkCondition("Java") { it.length == 6 })
    }

    @Test
    fun testCheckConditionWithStringPredicateVariousCases() {
        assertTrue(checkCondition("Hello") { it.startsWith("H") })
        assertTrue(checkCondition("World") { it.endsWith("d") })
        assertTrue(checkCondition("Test") { it.contains("es") })
        assertFalse(checkCondition("Kotlin") { it.isEmpty() })
    }

    @Test
    fun testCheckConditionWithDoublePredicate() {
        // From main method - binary predicate over Double
        assertTrue(checkCondition(5.6, 5.7) { d1, d2 -> d1 < d2 })
        assertFalse(checkCondition(5.7, 5.6) { d1, d2 -> d1 < d2 })
    }

    @Test
    fun testCheckConditionWithDoubleVariousPredicates() {
        assertTrue(checkCondition(3.0, 4.0) { a, b -> a + b == 7.0 })
        assertTrue(checkCondition(10.0, 2.0) { a, b -> a / b == 5.0 })
        assertFalse(checkCondition(5.0, 5.0) { a, b -> a > b })
    }

    // ==================== Alphabet Builder Tests ====================

    @Test
    fun testAlphabetFunction() {
        // From main method - alphabet builder
        val result = alphabet()

        assertTrue(result.startsWith("ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
        assertTrue(result.contains("Now I know the alphabet!"))
    }

    @Test
    fun testAlphabetContainsAllLetters() {
        val result = alphabet()

        for (c in 'A'..'Z') {
            assertTrue(result.contains(c), "Alphabet should contain $c")
        }
    }

    // ==================== StringBuilder with apply() Tests ====================

    @Test
    fun testStringBuilderWithApply() {
        // From main method - StringBuilder with apply
        val result = StringBuilder()
            .apply {
                append("Hello, ")
                append("World!")
            }.toString()

        assertEquals("Hello, World!", result)
    }

    @Test
    fun testStringBuilderApplyChaining() {
        val result = StringBuilder()
            .apply { append("Kotlin ") }
            .apply { append("is ") }
            .apply { append("awesome!") }
            .toString()

        assertEquals("Kotlin is awesome!", result)
    }

    @Test
    fun testStringBuilderApplyWithConditional() {
        val includeExclamation = true

        val result = StringBuilder()
            .apply {
                append("Hello")
                if (includeExclamation) {
                    append("!")
                }
            }.toString()

        assertEquals("Hello!", result)
    }

    // ==================== Collection Operations Tests ====================

    @Test
    fun testMapTransformation() {
        val fruits = listOf("banana", "avocado", "apple", "kiwi")

        val upperCaseFruits = fruits.map { it.uppercase() }

        assertEquals(listOf("BANANA", "AVOCADO", "APPLE", "KIWI"), upperCaseFruits)
    }

    @Test
    fun testFilterOperation() {
        val fruits = listOf("banana", "avocado", "apple", "kiwi")

        val longFruits = fruits.filter { it.length > 5 }

        assertEquals(listOf("banana", "avocado"), longFruits)
    }

    @Test
    fun testMapFilterChaining() {
        // From main method - map and filter chaining
        val fruits = listOf("banana", "avocado", "apple", "kiwi")

        val result = fruits
            .map { it.uppercase() }
            .filter { it.length > 5 }

        assertEquals(listOf("BANANA", "AVOCADO"), result)
    }

    @Test
    fun testAlsoSideEffect() {
        // From main method - also for side effects
        val fruits = listOf("banana", "avocado", "apple", "kiwi")
        val upperCaseFruits = mutableListOf<String>()

        val result = fruits
            .map { it.uppercase() }
            .also { upperCaseFruits.addAll(it) }
            .filter { it.length > 5 }

        assertEquals(listOf("BANANA", "AVOCADO"), result)
        assertEquals(listOf("BANANA", "AVOCADO", "APPLE", "KIWI"), upperCaseFruits)
    }

    @Test
    fun testReversedOperation() {
        // From main method - reversed collection
        val fruits = listOf("banana", "avocado", "apple", "kiwi")

        val reversedLongFruits = fruits
            .map { it.uppercase() }
            .filter { it.length > 5 }
            .reversed()

        assertEquals(listOf("AVOCADO", "BANANA"), reversedLongFruits)
    }

    @Test
    fun testCompleteMapFilterReversedPipeline() {
        // From main method - complete pipeline
        val fruits = listOf("banana", "avocado", "apple", "kiwi")
        val upperCaseFruits = mutableListOf<String>()

        val reversedLongFruits = fruits
            .map { it.uppercase() }
            .also { upperCaseFruits.addAll(it) }
            .filter { it.length > 5 }
            .reversed()

        assertEquals(listOf("AVOCADO", "BANANA"), reversedLongFruits)
        assertTrue(upperCaseFruits.contains("BANANA"))
        assertTrue(upperCaseFruits.contains("KIWI"))
    }

    // ==================== Sequence (Lazy Evaluation) Tests ====================

    @Test
    fun testSequenceWithMapAndFilter() {
        // From main method - sequence for lazy evaluation
        val fruits = listOf("banana", "avocado", "apple", "kiwi")

        val result = fruits.asSequence()
            .map { it.uppercase() }
            .filter { it.length > 5 }
            .toList()

        assertEquals(listOf("BANANA", "AVOCADO"), result)
    }

    @Test
    fun testSequenceWithOnEach() {
        // From main method - sequence with onEach
        val fruits = listOf("banana", "avocado", "apple", "kiwi")
        val upperCaseFruits = mutableListOf<String>()

        val result = fruits.asSequence()
            .map { it.uppercase() }
            .onEach { upperCaseFruits.add(it) }
            .filter { it.length > 5 }
            .toList()

        assertEquals(listOf("BANANA", "AVOCADO"), result)
        // onEach in sequence is lazy - only executed for filtered items
        assertTrue(upperCaseFruits.size >= 2)
    }

    @Test
    fun testSequenceVsListPerformanceDifference() {
        val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

        // Eager evaluation with list
        val listResult = numbers
            .map { it * 2 }
            .filter { it > 10 }
            .take(2)

        // Lazy evaluation with sequence
        val sequenceResult = numbers.asSequence()
            .map { it * 2 }
            .filter { it > 10 }
            .take(2)
            .toList()

        assertEquals(listResult, sequenceResult)
        assertEquals(listOf(12, 14), listResult)
    }

    // ==================== Additional Lambda Patterns Tests ====================

    @Test
    fun testLambdaWithMultipleParameters() {
        val combine: (String, String, String) -> String = { a, b, c -> "$a-$b-$c" }

        assertEquals("one-two-three", combine("one", "two", "three"))
    }

    @Test
    fun testLambdaReturningLambda() {
        val multiplierFactory: (Int) -> (Int) -> Int = { factor ->
            { value -> value * factor }
        }

        val double = multiplierFactory(2)
        val triple = multiplierFactory(3)

        assertEquals(10, double(5))
        assertEquals(15, triple(5))
    }

    @Test
    fun testLambdaWithDestructuring() {
        val pairs = listOf(1 to "one", 2 to "two", 3 to "three")

        val result = pairs.map { (number, word) ->
            "$number is $word"
        }

        assertEquals(listOf("1 is one", "2 is two", "3 is three"), result)
    }

    @Test
    fun testLambdaWithUnderscoreForUnusedParameter() {
        val pairs = listOf(1 to "one", 2 to "two", 3 to "three")

        val result = pairs.map { (_, word) -> word.uppercase() }

        assertEquals(listOf("ONE", "TWO", "THREE"), result)
    }

    @Test
    fun testHigherOrderFunctionReturningFunction() {
        fun createGreeter(greeting: String): (String) -> String {
            return { name -> "$greeting, $name!" }
        }

        val sayHello = createGreeter("Hello")
        val sayHi = createGreeter("Hi")

        assertEquals("Hello, Alice!", sayHello("Alice"))
        assertEquals("Hi, Bob!", sayHi("Bob"))
    }

    @Test
    fun testFunctionReference() {
        fun isEven(n: Int): Boolean = n % 2 == 0

        val numbers = listOf(1, 2, 3, 4, 5, 6)
        val evenNumbers = numbers.filter(::isEven)

        assertEquals(listOf(2, 4, 6), evenNumbers)
    }

    @Test
    fun testMemberFunctionReference() {
        val strings = listOf("hello", "world", "kotlin")
        val lengths = strings.map(String::length)

        assertEquals(listOf(5, 5, 6), lengths)
    }

    @Test
    fun testLambdaWithExplicitReturn() {
        val numbers = listOf(1, 2, 3, 4, 5)

        val result = numbers.map { n ->
            if (n % 2 == 0) {
                return@map n * 2
            }
            n
        }

        assertEquals(listOf(1, 4, 3, 8, 5), result)
    }

    @Test
    fun testNestedLambdas() {
        val matrix = listOf(
            listOf(1, 2, 3),
            listOf(4, 5, 6),
            listOf(7, 8, 9)
        )

        val flattened = matrix.flatMap { row ->
            row.map { it * 2 }
        }

        assertEquals(listOf(2, 4, 6, 8, 10, 12, 14, 16, 18), flattened)
    }

    @Test
    fun testGroupByWithLambda() {
        val words = listOf("apple", "banana", "apricot", "berry", "avocado")

        val grouped = words.groupBy { it.first() }

        assertEquals(3, grouped['a']?.size)
        assertEquals(2, grouped['b']?.size)
    }

    @Test
    fun testAssociateWithLambda() {
        val words = listOf("one", "two", "three")

        val wordToLength = words.associateWith { it.length }

        assertEquals(3, wordToLength["one"])
        assertEquals(3, wordToLength["two"])
        assertEquals(5, wordToLength["three"])
    }

    @Test
    fun testFoldWithLambda() {
        val numbers = listOf(1, 2, 3, 4, 5)

        val sum = numbers.fold(0) { acc, n -> acc + n }
        val product = numbers.fold(1) { acc, n -> acc * n }

        assertEquals(15, sum)
        assertEquals(120, product)
    }

    @Test
    fun testReduceWithLambda() {
        val numbers = listOf(1, 2, 3, 4, 5)

        val sum = numbers.reduce { acc, n -> acc + n }

        assertEquals(15, sum)
    }

    @Test
    fun testPartitionWithLambda() {
        val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

        val (even, odd) = numbers.partition { it % 2 == 0 }

        assertEquals(listOf(2, 4, 6, 8, 10), even)
        assertEquals(listOf(1, 3, 5, 7, 9), odd)
    }

    @Test
    fun testTakeWhileWithLambda() {
        val numbers = listOf(1, 2, 3, 4, 5, 1, 2, 3)

        val result = numbers.takeWhile { it < 4 }

        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun testDropWhileWithLambda() {
        val numbers = listOf(1, 2, 3, 4, 5, 6, 7)

        val result = numbers.dropWhile { it < 4 }

        assertEquals(listOf(4, 5, 6, 7), result)
    }

    @Test
    fun testLambdaWithTypeParameter() {
        fun <T> applyTwice(value: T, operation: (T) -> T): T {
            return operation(operation(value))
        }

        assertEquals(3, applyTwice(1) { it + 1 })
        assertEquals("abababab", applyTwice("ab") { it + it })
    }

    @Test
    fun testInlineFunctionConcept() {
        var executed = false
        runOperationInline {
            executed = true
        }

        assertTrue(executed)
    }

    @Test
    fun testLambdaCapturingVariables() {
        var counter = 0

        val increment = { counter++ }

        increment()
        increment()
        increment()

        assertEquals(3, counter)
    }

    @Test
    fun testLambdaWithReceiver() {
        val buildString: StringBuilder.() -> Unit = {
            append("Hello")
            append(" ")
            append("World")
        }

        val result = StringBuilder().apply(buildString).toString()

        assertEquals("Hello World", result)
    }
}

// Helper function for inline function testing
private inline fun runOperationInline(operation: () -> Unit) {
    println("Before operation")
    operation()
    println("After operation")
}
