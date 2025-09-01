/**
 * A functional (SAM) interface representing a predicate over [Int].
 *
 * Marked with `fun` to enable Kotlin SAM conversion: you can pass lambdas wherever
 * an [IntCondition] is expected:
 *
 * Example:
 * ```
 * val isOdd: IntCondition = IntCondition { it % 2 != 0 }
 * ```
 *
 * Convenience methods [checkString] and [checkChar] convert inputs to [Int] and
 * delegate to [check].
 */
fun interface IntCondition {

    /**
     * Evaluates the condition on the provided integer.
     *
     * @param i The integer under test.
     * @return `true` if the condition holds; `false` otherwise.
     *
     * Example:
     * ```
     * IntCondition { it > 0 }.check(42) // true
     * ```
     */
    fun check(i: Int): Boolean

    /**
     * Parses [s] as a decimal integer and evaluates [check] on the parsed value.
     *
     * This uses `String.toInt()` (radix 10), which is strict: no whitespace, no
     * underscores, and only an optional leading sign.
     *
     * @param s A decimal string (e.g., `"42"`, `"-7"`).
     * @return The result of [check] for the parsed integer.
     * @throws NumberFormatException if [s] is not a valid decimal integer.
     *
     * Example:
     * ```
     * IntCondition { it % 2 == 0 }.checkString("10") // true
     * ```
     */
    fun checkString(s: String) = check(s.toInt())

    /**
     * Converts a decimal digit character (e.g. `'0'..'9'`) to an [Int] and evaluates [check].
     *
     * Internally uses [Char.digitToInt] with radix 10.
     *
     * @param c A character representing a decimal digit.
     * @return The result of [check] for the digit value.
     * @throws IllegalArgumentException if [c] is not a valid decimal digit in radix 10.
     *
     * Example:
     * ```
     * IntCondition { it >= 5 }.checkChar('7') // true
     * ```
     */
    fun checkChar(c: Char) = check(c.digitToInt())
}

/**
 * Applies the provided [condition] to the integer [i].
 *
 * Prefer this overload when you want to take advantage of SAM conversion for [IntCondition],
 * including the helper methods [IntCondition.checkString] and [IntCondition.checkChar].
 *
 * @param i The integer to check.
 * @param condition A predicate expressed as an [IntCondition].
 * @return `true` if [condition] returns `true`; otherwise `false`.
 *
 * Example:
 * ```
 * println(checkCondition(5, IntCondition { it % 2 != 0 })) // true
 * ```
 */
fun checkCondition(i: Int, condition: IntCondition) = condition.check(i)

/**
 * Parses [s] as a decimal and applies the [condition] expressed as an [IntCondition].
 *
 * Internally delegates to [IntCondition.checkString].
 *
 * @param s The string to parse as a decimal integer.
 * @param condition A predicate expressed as an [IntCondition].
 * @return `true` if the parsed integer satisfies [condition]; otherwise `false`.
 * @throws NumberFormatException if [s] is not a valid decimal integer.
 *
 * Example:
 * ```
 * val isOdd = IntCondition { it % 2 != 0 }
 * println(checkCondition("5", isOdd)) // true
 * ```
 */
fun checkCondition(s: String, condition: IntCondition) = condition.checkString(s)

/**
 * Applies a string-based predicate [condition] directly to [s] (no numeric parsing).
 *
 * Prefer this overload when your rule is about the *string itself* (e.g., length or pattern),
 * not its numeric value.
 *
 * @param s The input string.
 * @param condition A predicate over [String].
 * @return The result of applying [condition] to [s].
 *
 * Example:
 * ```
 * println(checkCondition("Kotlin") { it.length == 6 }) // true
 * ```
 */
fun checkCondition(s: String, condition: (String) -> Boolean) = condition(s)

/**
 * Applies a binary predicate [condition] to two doubles [d1] and [d2].
 *
 * @param d1 The first value.
 * @param d2 The second value.
 * @param condition A predicate over two [Double]s.
 * @return The result of [condition] invoked with [d1] and [d2].
 *
 * Example:
 * ```
 * println(checkCondition(5.6, 5.7) { a, b -> a < b }) // true
 * ```
 */
fun checkCondition(
    d1: Double,
    d2: Double,
    condition: (Double, Double) -> Boolean
) = condition.invoke(d1, d2)

/**
 * Builds a string containing the uppercase English alphabet followed by a message.
 * The main goal of this method is to demonstrate the use of [with] function.
 *
 * @return A string like "ABCDEFGHIJKLMNOPQRSTUVWXYZ\nNow I know the alphabet!"
 */
fun alphabet() = with(StringBuilder()) {
    for (c in 'A'..'Z') {
        append(c)
    }
    append("\nNow I know the alphabet!")
    toString()
}

/**
 * Demonstrates usage patterns:
 *
 * - Explicit cast of lambda type (rarely needed).
 * - Trailing-lambda syntax for the last function parameter.
 * - Parameter name inference and `it` usage.
 * - Reusing a lambda via a variable.
 * - SAM construction for [IntCondition] and use of its helpers.
 * - Overload that accepts `(String) -> Boolean`.
 * - Overload that accepts a binary predicate over `Double`.
 *
 * ⚠️ Note on SAM and overload resolution:
 * If your Kotlin version does **not** allow passing a *function type variable* where
 * an [IntCondition] is expected, wrap it with the SAM constructor:
 * `checkCondition("5", IntCondition(isOdd))`.
 */
fun main() {
    // 1) Explicitly casting the lambda type (almost never necessary in idiomatic Kotlin).
    println(checkCondition(5, { i: Int -> i % 2 != 0 } as (Int) -> Boolean))

    // 2) Trailing-lambda syntax with explicit parameter type.
    println(checkCondition(5) { i: Int -> i % 2 != 0 })

    // 3) Trailing-lambda syntax with inferred parameter type and `it`.
    println(checkCondition(5) { i -> i % 2 != 0 })

    // 4) Reusing a lambda via a variable.
    val isOdd: (Int) -> Boolean = { it % 2 != 0 }
    println(checkCondition(5, IntCondition(isOdd)))  // using SAM constructor explicitly is safest

    // Depending on Kotlin version/settings, the next line may require SAM wrapping:
    println(checkCondition("5", isOdd))            // might not compile on some versions
    println(checkCondition("5", IntCondition(isOdd))) // parses "5" and applies isOdd

    // Using the SAM constructor and a helper on the fun interface.
    println(IntCondition { it % 2 != 0 }.checkChar('5'))

    // Overload that accepts (String) -> Boolean (no parsing).
    println(checkCondition("Kotlin") { it.length == 6 })

    // Overload that accepts a binary predicate over Double.
    println(checkCondition(5.6, 5.7) { d1, d2 -> d1 < d2 })

    // Alphabet builder demo.
    println(alphabet())

    // StringBuilder usage with apply.
    println(
        StringBuilder()
            .apply {
                append("Hello, ")
                append("World!")
            }.toString()
    )

    // List transformation and filtering demo.
    val fruits = listOf("banana", "avocado", "apple", "kiwi")
    val upperCaseFruits = mutableListOf<String>()

    // Using map, also, filter, and reversed.
    val reversedLongFruits = fruits
        .map { it.uppercase() }
        .also { upperCaseFruits.addAll(it) }
        .filter { it.length > 5 }
        .also { println("Long fruits before reverse: $it") }
        .reversed()

    // Using sequence for lazy evaluation.
    val reversedLongFruitsWithSequence = fruits.asSequence()
        .map { it.uppercase() }
        .onEach { upperCaseFruits.add(it) }
        .filter { it.length > 5 }
        .toList()
        .reversed()

    println("Reversed long fruits: $reversedLongFruits")
    println("Reversed long fruits with sequence: $reversedLongFruitsWithSequence")
    println("Upper case fruits: $upperCaseFruits")
}
