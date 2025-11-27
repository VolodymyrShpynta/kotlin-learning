package com.playground.kotlin.oop.core.devices.v2

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
