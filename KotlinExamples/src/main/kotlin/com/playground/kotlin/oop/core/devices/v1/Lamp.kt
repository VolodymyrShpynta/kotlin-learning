package com.playground.kotlin.oop.core.devices.v1

/**
 * A concrete implementation of [Device] representing a lamp.
 *
 * @constructor Creates a new [Lamp] with the given name.
 */
class Lamp(name: String) : Device(name) {

    /**
     * Companion object providing factory methods for creating [Lamp] instances.
     */
    companion object {
        /**
         * Creates a new [Lamp] with the specified name.
         *
         * @param name The name of the lamp.
         * @return A new [Lamp] instance.
         */
        fun create(name: String): Lamp = Lamp(name)
    }

    /**
     * Uses the lamp by printing a message.
     */
    override fun use() {
        println("Using lamp with name $name")
    }
}

/**
 * Extension function on [Lamp.Companion] to create a default lamp instance.
 *
 * @return A [Lamp] with the name "Default Lamp".
 */
fun Lamp.Companion.createDefault(): Lamp = Lamp.create("Default Lamp")
