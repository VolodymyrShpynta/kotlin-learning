package com.playground.kotlin.oop.core.devices.v2

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
