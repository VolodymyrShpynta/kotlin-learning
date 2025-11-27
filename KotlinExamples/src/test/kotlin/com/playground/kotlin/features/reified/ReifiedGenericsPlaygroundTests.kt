package com.playground.kotlin.features.reified

import com.playground.kotlin.oop.core.devices.v1.DeviceRegistry
import com.playground.kotlin.oop.core.devices.v1.Lamp
import com.playground.kotlin.oop.core.devices.v1.Speaker
import com.playground.kotlin.oop.core.devices.v1.countDevicesOfType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests demonstrating Kotlin reified type parameters and inline functions.
 *
 * Key concepts:
 * - Inline functions with reified type parameters
 * - Generic type checking at runtime (normally erased)
 * - Object declarations (singleton pattern for DeviceRegistry)
 */
class ReifiedGenericsPlaygroundTests {

    @BeforeTest
    fun initTests() {
        DeviceRegistry.cleanUp()
    }

    @Test
    fun testSingletonObjectPattern() {
        val lamp = Lamp.create("Desk Lamp")
        val speaker = Speaker.create("Bluetooth Speaker")
        DeviceRegistry.register(lamp)
        DeviceRegistry.register(speaker)
        val devices = DeviceRegistry.listAll()
        assertTrue(devices.contains(lamp))
        assertTrue(devices.contains(speaker))
    }

    @Test
    fun testReifiedTypeParametersForRuntimeTypeChecking() {
        DeviceRegistry.register(Lamp.create("Lamp 1"))
        DeviceRegistry.register(Lamp.create("Lamp 2"))
        DeviceRegistry.register(Speaker.create("Speaker 1"))
        val lampCount = DeviceRegistry.countDevicesOfType<Lamp>()
        val speakerCount = DeviceRegistry.countDevicesOfType<Speaker>()
        assertEquals(2, lampCount)
        assertEquals(1, speakerCount)
    }
}
