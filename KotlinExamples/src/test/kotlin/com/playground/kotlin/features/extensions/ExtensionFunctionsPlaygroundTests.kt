package com.playground.kotlin.features.extensions

import com.playground.kotlin.oop.core.devices.v1.Lamp
import com.playground.kotlin.oop.core.devices.v1.createDefault
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests demonstrating Kotlin extension functions on companion objects.
 *
 * Key concepts:
 * - Extension functions on companion objects
 * - Extending factory patterns with additional convenience methods
 */
class ExtensionFunctionsPlaygroundTests {

    @Test
    fun testCompanionObjectExtensionFunction() {
        val defaultLamp = Lamp.createDefault()
        assertEquals("Default Lamp", defaultLamp.name)
    }
}
