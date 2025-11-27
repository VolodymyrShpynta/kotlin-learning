package com.playground.kotlin.oop.core.devices.v2

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
    fun register(device: Device) = devices.add(device)

    /**
     * Returns a list of all registered devices.
     *
     * @return A read-only list of devices.
     */
    fun listAll(): List<Device> = devices.toList()

    /**
     * Generates a summary of all registered devices.
     *
     * @return A formatted string listing each device and its key attributes.
     */
    fun summary(): String = devices.joinToString(separator = "\n") { device ->
        when (device) {
            is Lamp -> "Lamp: ${device.name} with brightness ${device.brightness}"
            is Speaker -> "Speaker: ${device.name} with volume ${device.volume}"
            is Thermostat -> "Thermostat: ${device.name} with temperature ${device.temperature}"
        }
    }
}

/**
 * Extension function on [DeviceRegistry] to find a device by name.
 *
 * @param name The name of the device to search for.
 * @return The matching [Device] if found, or `null` otherwise.
 */
fun DeviceRegistry.findByName(name: String): Device? {
    return listAll().firstOrNull { device -> device.name.equals(name, ignoreCase = true) }
}
