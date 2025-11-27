package com.playground.kotlin.features.companionobjects

import com.playground.kotlin.oop.core.devices.v1.Lamp
import com.playground.kotlin.oop.core.devices.v1.Speaker
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests demonstrating Kotlin companion objects and object declarations.
 *
 * Key concepts:
 * - Companion objects as factory methods
 * - Object declarations (singleton pattern)
 * - Nested objects (Settings inside Speaker)
 * - Custom property getters/setters
 * - Exception handling for uninitialized properties
 */
class CompanionObjectsPlaygroundTests {

    @BeforeTest
    fun initTests() {
        Speaker.Settings.resetForTests()
    }

    @Test
    fun testCompanionObjectAsFactory() {
        val lamp = Lamp.create("Desk Lamp")
        assertEquals("Desk Lamp", lamp.name)
        lamp.use() // Should print: Using lamp with name Desk Lamp
    }

    @Test
    fun testNamedCompanionObjectFactory() {
        val speaker = Speaker.create("Bluetooth Speaker")
        assertEquals("Bluetooth Speaker", speaker.name)
        speaker.use() // Should print: Using speaker with name Bluetooth Speaker
    }

    @Test
    fun testNestedObjectSettings() {
        Speaker.Settings.defaultVolume = 75
        Speaker.Settings.defaultStyle = "Modern"
        assertEquals(75, Speaker.Settings.defaultVolume)
        assertEquals("Modern", Speaker.Settings.defaultStyle)
    }

    @Test
    fun testCustomPropertyGetterThrowsException() {
        // arrange so that defaultStyle is UNINITIALIZED (see section 2)
        assertFailsWith<UninitializedPropertyAccessException> {
            // the *read* must happen inside the lambda
            val ignore = Speaker.Settings.defaultStyle
        }
    }
}
