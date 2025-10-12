/**
 * Playground demonstrating basic Kotlin Flow concepts:
 *
 * 1. A simple cold [flow] (run in [runBaseFlowDemo]) that emits two letters. It shows:
 *    - Builder code runs from the start for every new collector (cold flow semantics).
 *    - Collector delays apply backpressure: a delay in the collector suspends upstream emission until it resumes
 *      (see timeline example below).
 * 2. A [channelFlow] example (run in [runChannelFlowDemo]) that launches concurrent coroutines to emit values
 *    and collects them while mutating shared state (`counter`). It illustrates:
 *    - Structured concurrency inside the builder via `launch {}` blocks.
 *    - Thread‑safe emission into the Flow via the channel provided by `channelFlow {}`.
 *    - Why the apparent "race condition" on `counter` does not occur: the collector block executes sequentially
 *      on a single coroutine, so even though values are produced concurrently, accumulation happens serially.
 *
 * Timeline sample from [runBaseFlowDemo]:
 *    0ms  Emitting A (builder starts)
 *  ~220ms A (collector finished its 200ms delay + logging)
 *  ~522ms Emitting B (builder resumed after collector then its own 300ms delay)
 *  ~725ms B (collector finished its delay for B)
 * This shows the collector delay holding back the builder's progress (backpressure), not running independently.
 *
 * Run the file's [main] function to print timestamped logs (uses the shared `log` helper defined in
 * `CoroutinesPlayground.kt`).
 */
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds

/**
 * A simple [FlowCollector] implementation that logs each received String value.
 */
class MyFlowCollector : FlowCollector<String> {
    override suspend fun emit(value: String) {
        log("Collected $value")
    }
}

/**
 * Demonstrates a basic cold Flow using the [flow] builder.
 *
 * Behavior details:
 * - Emits two letters with a delay between them (A then B).
 * - The collector adds an extra delay for each emitted value, intentionally demonstrating backpressure: the
 *   builder cannot proceed to its next emission until the collector resumes.
 * - The Flow is collected a second time with a custom [FlowCollector], showing that cold flows restart their
 *   upstream code for each collection.
 */
private suspend fun runBaseFlowDemo() {
    val letters = flow {
        log("Emitting A")
        emit("A")
        delay(300.milliseconds)
        log("Emitting B")
        emit("B")
    }

    letters.collect {
        delay(200.milliseconds)
        log(it)
    }

    letters.collect(MyFlowCollector())
}

/**
 * Demonstrates concurrent production of values using [channelFlow].
 *
 * Inside the builder three coroutines are launched, each sending a number after a delay. Because `send` is applied
 * to the channel provided by [channelFlow], emissions are safely coordinated without explicit synchronization.
 *
 * The collector intentionally performs a read-modify-write sequence with an added delay to mimic a potential race.
 * However, no race condition manifests because the collector lambda runs sequentially; only the production side
 * is concurrent. The final counter is expected to be 6 (1 + 2 + 3).
 */
private suspend fun runChannelFlowDemo() {
    var counter = 0

    val numbers = channelFlow {
        for (i in 1..3) {
            launch {
                delay(300.milliseconds)
                send(i)
            }
        }
    }

    numbers.collect {
        var temp = counter
        delay(200.milliseconds) // delay to increase chance of the race condition
        temp += it
        counter = temp
        log("Counter: $counter")
    }
    log("Final Counter: $counter") // should be '6' because race condition is avoided by using channelFlow
}

/**
 * Entry point that runs both flow demonstrations within a [runBlocking] scope so the program can be launched
 * as a regular Kotlin/JVM application.
 */
fun main() = runBlocking {
    runBaseFlowDemo()
    runChannelFlowDemo()
}
