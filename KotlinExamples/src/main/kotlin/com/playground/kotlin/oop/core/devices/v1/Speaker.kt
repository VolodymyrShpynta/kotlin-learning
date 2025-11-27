package com.playground.kotlin.oop.core.devices.v1

import org.jetbrains.annotations.TestOnly

/**
 * A concrete implementation of [Device] representing a speaker.
 *
 * @constructor Private constructor to enforce use of the [Factory] for instantiation.
 */
class Speaker private constructor(name: String) : Device(name) {

    /**
     * Companion object acting as a factory for creating [Speaker] instances.
     */
    companion object Factory {
        /**
         * Creates a new [Speaker] with the specified name.
         *
         * @param name The name of the speaker.
         * @return A new [Speaker] instance.
         */
        fun create(name: String): Speaker = Speaker(name)
    }

    /**
     * Singleton object holding configurable settings for [Speaker].
     */
    object Settings {
        private var _defaultStyle: String? = null

        /**
         * The default volume setting for all speakers.
         */
        var defaultVolume: Int? = null

        /**
         * The default style setting for all speakers.
         *
         * @throws UninitializedPropertyAccessException if the style has not been set.
         */
        var defaultStyle: String
            get() = _defaultStyle ?: throw UninitializedPropertyAccessException("defaultStyle is not initialized")
            set(value) {
                _defaultStyle = value
            }

        @TestOnly
        fun resetForTests() {
            _defaultStyle = null
            defaultVolume = null
        }

    }

    /**
     * Uses the speaker by printing a message.
     */
    override fun use() = println("Using speaker with name $name")
}
