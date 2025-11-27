package com.playground.kotlin.oop.core.devices.v2

/**
 * Abstract base class representing a generic device.
 *
 * @property name The name of the device.
 */
sealed class Device {

    abstract val name: String

    /**
     * Abstract function to be implemented by subclasses to define how the device is used.
     */
    abstract fun use()
}
