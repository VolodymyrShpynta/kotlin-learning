package com.playground.kotlin.oop.core.devices.v1

/**
 * Singleton object that manages the registration and listing of devices.
 */
object DeviceRegistry {

    private val devices = mutableListOf<Device>()

    /**
     * Registers a new device in the registry.
     *
     * @param device The device to register.
     */
    fun register(device: Device) {
        devices.add(device)
    }

    /**
     * Returns a list of all registered devices.
     *
     * @return A list of devices.
     */
    fun listAll(): List<Device> = devices.toList()

    fun cleanUp() {
        devices.clear()
    }
}

/**
 * Extension function on [DeviceRegistry] to count the number of devices of a specific type.
 *
 * @return The number of devices of type [T].
 */
inline fun <reified T> DeviceRegistry.countDevicesOfType(): Int {
    return DeviceRegistry.listAll().count { it is T }
}
