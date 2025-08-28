import org.jetbrains.annotations.TestOnly

/**
 * Abstract base class representing a generic device.
 *
 * @property name The name of the device.
 */
abstract class Device(val name: String) {
    /**
     * Abstract function to be implemented by subclasses to define how the device is used.
     */
    abstract fun use()
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

/**
 * Extension function on [Lamp.Companion] to create a default lamp instance.
 *
 * @return A [Lamp] with the name "Default Lamp".
 */
fun Lamp.Companion.createDefault(): Lamp = Lamp.create("Default Lamp")

/**
 * Extension function on [DeviceRegistry] to count the number of devices of a specific type.
 *
 * @return The number of devices of type [T].
 */
inline fun <reified T> DeviceRegistry.countDevicesOfType(): Int {
    return DeviceRegistry.listAll().count { it is T }
}
