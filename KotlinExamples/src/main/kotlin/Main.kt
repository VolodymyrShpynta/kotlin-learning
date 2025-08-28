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
}
