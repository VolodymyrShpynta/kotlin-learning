package com.playground.kotlin.oop.core.devices.v2

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
