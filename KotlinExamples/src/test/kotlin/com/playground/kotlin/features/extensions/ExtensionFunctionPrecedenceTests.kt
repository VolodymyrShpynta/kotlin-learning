package com.playground.kotlin.features.extensions

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests demonstrating extension function precedence in Kotlin.
 *
 * Key concept: Member extension functions (defined inside a class) take precedence
 * over top-level extension functions when called within the class scope.
 *
 * This demonstrates:
 * - Member extension functions vs. top-level extension functions
 * - Scope resolution for extension functions
 * - Using receiver scope with 'with' function
 */
class ExtensionFunctionPrecedenceTests {

    // Helper class with a member extension function
    class StringProcessor {
        fun String.format(): String {
            return "Member extension: $this"
        }

        fun processWithMemberExtension(input: String): String {
            return input.format() // Calls member extension function
        }
    }

    // Top-level extension function with the same signature
    private fun String.format(): String {
        return "Top-level extension: $this"
    }

    @Test
    fun testTopLevelExtensionFunctionInTopLevelScope() {
        // When called at top level, uses the top-level extension function
        val result = "Hello".format()
        assertEquals("Top-level extension: Hello", result)
    }

    @Test
    fun testMemberExtensionFunctionTakesPrecedenceOverTopLevel() {
        // When called inside the class scope, member extension takes precedence
        val processor = StringProcessor()
        val result = processor.processWithMemberExtension("Hello")
        assertEquals("Member extension: Hello", result)
    }

    @Test
    fun testMemberExtensionFunctionWithReceiverScope() {
        // Using 'with' to bring the class into scope
        val result = with(StringProcessor()) {
            "Hello".format() // Member extension function is called
        }
        assertEquals("Member extension: Hello", result)
    }

    @Test
    fun testExtensionFunctionResolutionDependsOnScope() {
        // Outside the class scope, top-level extension is used
        val topLevelResult = "World".format()
        val memberResult = with(StringProcessor()) { "World".format() }

        // Different results based on scope
        assertEquals("Top-level extension: World", topLevelResult)
        assertEquals("Member extension: World", memberResult)
    }
}
