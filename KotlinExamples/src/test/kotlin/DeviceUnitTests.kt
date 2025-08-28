import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DeviceTests {

    @BeforeTest
    fun initTests(){
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
    fun initTests(){
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
