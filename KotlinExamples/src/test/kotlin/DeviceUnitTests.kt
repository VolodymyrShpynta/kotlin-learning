import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DeviceTests {

    @BeforeTest
    fun initTests() {
        Speaker.Settings.resetForTests()
    }

    @Test
    fun testLampCreationAndUse() {
        val lamp = Lamp.create("Desk Lamp")
        assertEquals("Desk Lamp", lamp.name)
        lamp.use() // Should print: Using lamp with name Desk Lamp
    }

    @Test
    fun testSpeakerCreationAndUse() {
        val speaker = Speaker.create("Bluetooth Speaker")
        assertEquals("Bluetooth Speaker", speaker.name)
        speaker.use() // Should print: Using speaker with name Bluetooth Speaker
    }

    @Test
    fun testSpeakerSettings() {
        Speaker.Settings.defaultVolume = 75
        Speaker.Settings.defaultStyle = "Modern"
        assertEquals(75, Speaker.Settings.defaultVolume)
        assertEquals("Modern", Speaker.Settings.defaultStyle)
    }


    @Test
    fun testSpeakerSettingsUninitializedStyle() {
        // arrange so that defaultStyle is UNINITIALIZED (see section 2)
        assertFailsWith<UninitializedPropertyAccessException> {
            // the *read* must happen inside the lambda
            val ignore = Speaker.Settings.defaultStyle
        }
    }
}

class DeviceRegistryTests {

    @BeforeTest
    fun initTests() {
        DeviceRegistry.cleanUp()
    }

    @Test
    fun testDeviceRegistrationAndListing() {
        val lamp = Lamp.create("Desk Lamp")
        val speaker = Speaker.create("Bluetooth Speaker")
        DeviceRegistry.register(lamp)
        DeviceRegistry.register(speaker)
        val devices = DeviceRegistry.listAll()
        assertTrue(devices.contains(lamp))
        assertTrue(devices.contains(speaker))
    }

    @Test
    fun testCountDevicesOfType() {
        DeviceRegistry.register(Lamp.create("Lamp 1"))
        DeviceRegistry.register(Lamp.create("Lamp 2"))
        DeviceRegistry.register(Speaker.create("Speaker 1"))
        val lampCount = DeviceRegistry.countDevicesOfType<Lamp>()
        val speakerCount = DeviceRegistry.countDevicesOfType<Speaker>()
        assertEquals(2, lampCount)
        assertEquals(1, speakerCount)
    }
}

class ExtensionFunctionTests {

    @Test
    fun testCreateDefaultLamp() {
        val defaultLamp = Lamp.createDefault()
        assertEquals("Default Lamp", defaultLamp.name)
    }
}

/**
 * Tests demonstrating extension function precedence in Kotlin.
 *
 * Key concept: Member extension functions (defined inside a class) take precedence
 * over top-level extension functions when called within the class scope.
 */
class ExtensionFunctionPrecedenceTests {

    // Helper class with a member extension function
    class StringProcessor {
        fun String.format(): String {
            return "Member extension: $this"
        }

        fun processWithMemberExtension(input: String): String {
            return input.format() // Calls member extension function
        }
    }

    // Top-level extension function with the same signature
    private fun String.format(): String {
        return "Top-level extension: $this"
    }

    @Test
    fun testTopLevelExtensionFunction() {
        // When called at top level, uses the top-level extension function
        val result = "Hello".format()
        assertEquals("Top-level extension: Hello", result)
    }

    @Test
    fun testMemberExtensionFunctionPrecedence() {
        // When called inside the class scope, member extension takes precedence
        val processor = StringProcessor()
        val result = processor.processWithMemberExtension("Hello")
        assertEquals("Member extension: Hello", result)
    }

    @Test
    fun testMemberExtensionWithReceiverScope() {
        // Using 'with' to bring the class into scope
        val result = with(StringProcessor()) {
            "Hello".format() // Member extension function is called
        }
        assertEquals("Member extension: Hello", result)
    }

    @Test
    fun testTopLevelExtensionOutsideClassScope() {
        // Outside the class scope, top-level extension is used
        val topLevelResult = "World".format()
        val memberResult = with(StringProcessor()) { "World".format() }

        // Different results based on scope
        assertEquals("Top-level extension: World", topLevelResult)
        assertEquals("Member extension: World", memberResult)
    }
}

