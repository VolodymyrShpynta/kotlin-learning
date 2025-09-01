package com.vshpynta.devices.v2

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

/**
 * A concrete implementation of [Device] representing a lamp.
 *
 * @property name The name of the lamp.
 * @property brightness The brightness level of the lamp.
 */
data class Lamp(
    override val name: String,
    val brightness: Int
) : Device() {

    /**
     * Companion object providing factory methods for creating [Lamp] instances.
     */
    companion object {
        /**
         * Creates a new [Lamp] with the specified name and optional brightness.
         *
         * @param name The name of the lamp.
         * @param brightness The brightness level (default is 50).
         * @return A new [Lamp] instance.
         */
        fun create(name: String, brightness: Int = 50): Lamp {
            return Lamp(name, brightness)
        }
    }

    /**
     * Uses the lamp by printing its current brightness.
     */
    override fun use() {
        println("Lamp $name is shining with $brightness brightness.")
    }
}

/**
 * A concrete implementation of [Device] representing a speaker.
 *
 * @property name The name of the speaker.
 * @property volume The volume level of the speaker.
 */
data class Speaker(
    override val name: String,
    val volume: Int
) : Device() {

    /**
     * Companion object providing factory methods for creating [Speaker] instances.
     */
    companion object {
        /**
         * Creates a new [Speaker] with the specified name and optional volume.
         *
         * @param name The name of the speaker.
         * @param volume The volume level (default is 5).
         * @return A new [Speaker] instance.
         */
        fun create(name: String, volume: Int = 5): Speaker = Speaker(name, volume)
    }

    /**
     * Uses the speaker by printing its current volume.
     */
    override fun use() {
        println("${this::class.simpleName} $name is speaking with $volume volume.")
    }
}

/**
 * A concrete implementation of [Device] representing a thermostat.
 *
 * @property name The name of the thermostat.
 * @property temperature The current temperature setting.
 */
data class Thermostat(
    override val name: String,
    val temperature: Double
) : Device() {

    /**
     * Companion object providing factory methods for creating [Thermostat] instances.
     */
    companion object {
        /**
         * Creates a new [Thermostat] with the specified name and optional temperature.
         *
         * @param name The name of the thermostat.
         * @param temperature The temperature setting (default is 22.0).
         * @return A new [Thermostat] instance.
         */
        fun create(name: String, temperature: Double = 22.0): Thermostat = Thermostat(name, temperature)
    }

    /**
     * Uses the thermostat by printing its current temperature.
     */
    override fun use() {
        println("Thermostat $name is showing $temperature temperature.")
    }

    /**
     * Object holding global settings for all thermostats.
     */
    object Setting {
        /**
         * Minimum allowable temperature.
         */
        var minTemperature: Double = -20.0

        /**
         * Maximum allowable temperature.
         */
        var maxTemperature: Double = 50.0
    }
}

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
