import com.vshpynta.devices.v2.findByName
import com.vshpynta.devices.v2.Device as DeviceV2
import com.vshpynta.devices.v2.DeviceRegistry as DeviceRegistryV2
import com.vshpynta.devices.v2.Lamp as LampV2
import com.vshpynta.devices.v2.Speaker as SpeakerV2
import com.vshpynta.devices.v2.Thermostat as ThermostatV2

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val name = "Kotlin"
    //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
    // to see how IntelliJ IDEA suggests fixing it.
    println("Hello, $name!")

    for (i in 1..5) {
        //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
        // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
        println("i = $i")
    }

    println("------ Devices V1: -----")

    DeviceRegistry.register(Lamp.create("white"));
    DeviceRegistry.register(Lamp.Companion.create("black"));
    DeviceRegistry.register(Lamp(name = "blue"))
    DeviceRegistry.register(Lamp.createDefault())
    DeviceRegistry.register(Speaker.create("Dell "))
    DeviceRegistry.register(Speaker.Factory.create("AMD "))
    DeviceRegistry.register(object : Device("Anonymous device") {
        override fun use() {
            println("Using the device with name: ${this.name}")
        }
    })

    println(DeviceRegistry.listAll())
    println("The number of devices of type ${Lamp::class.qualifiedName} is: ${DeviceRegistry.countDevicesOfType<Lamp>()}")
    println("The number of devices of type ${Speaker::class.qualifiedName} is: ${DeviceRegistry.countDevicesOfType<Speaker>()}")

    DeviceRegistry.listAll().forEach { it.use() }

    Speaker.Settings.defaultStyle = "Retro"
    Speaker.Settings.defaultVolume = 50
    println("Speaker.Settings.defaultStyle: ${Speaker.Settings.defaultStyle}")
    println("Speaker.Settings.defaultVolume: ${Speaker.Settings.defaultVolume}")

    println("------ Devices V2: -----")

    DeviceRegistryV2.register(LampV2.create("Desk Lamp"))
    DeviceRegistryV2.register(SpeakerV2.create("Kitchen Speaker", 10))
    DeviceRegistryV2.register(ThermostatV2.create(name = "Living Room Thermostat", temperature = 19.5))
    DeviceRegistryV2.register(ThermostatV2.create(name = "Outdoor Thermostat", temperature = ThermostatV2.Setting.minTemperature))
    DeviceRegistryV2.register(ThermostatV2.create(name = "Sauna Thermostat", temperature = ThermostatV2.Setting.maxTemperature))

    println(DeviceRegistryV2.summary())
    println("Find by name: ${DeviceRegistryV2.findByName("Desk Lamp")}")
    DeviceRegistryV2.listAll().forEach(DeviceV2::use)
}
