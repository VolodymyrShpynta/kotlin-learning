package com.playground.kotlin.concurrency.flows

/**
 * Kotlin Flow Playground – Demonstrates cold flows, hot flows, SharedFlow, StateFlow, shareIn, stateIn, and related concurrency/backpressure patterns.
 *
 * Demos (see [main]):
 * 1. Cold flow basics ([runBaseFlowDemo]) – Each collector restarts the builder; collector delays apply backpressure.
 * 2. Concurrent emission ([runChannelFlowDemo]) – [channelFlow] allows concurrent emission, but collection is sequential (no race conditions).
 * 3. Manual hot broadcast ([runHotFlowDemo]) – [RadioStation] uses [MutableSharedFlow]; late subscribers miss prior values (replay=0); producer/consumer scopes are separate and explicitly cancelled.
 * 4. Sharing infinite cold stream ([runSharedFlowDemo]) – [getTemperaturesFlow] shared via [shareIn]; starts on first collector, runs until scope cancelled. Note: using `shareIn(this, ...)` (where `this` is the main coroutine scope) will keep the upstream alive as long as the main scope, so main never stops; using a dedicated scope allows explicit cancellation and proper demo shutdown.
 * 5. Atomic StateFlow updates ([runStateFlowAtomicUpdateDemo]) – [MutableStateFlow.update] ensures thread-safe increments across many coroutines.
 * 6. StateFlow distinct emissions ([runStateFlowChangesEmissionDemo]) – Setting the same value does not re-emit; only distinct changes are propagated.
 * 7. Cold-to-hot conversion ([runColdFlowToHotStateFlowDemo]) – [stateIn] converts cold flow to hot [StateFlow], caching the latest value; scope cancellation is required to stop upstream.
 * 8. Flow transformations ([runFlowsTransformationsDemo]) – Shows transform, take, onCompletion, buffer, flatMapMerge, conflate, debounce, flowOn, and custom rolling average operator.
 * 9. Exception handling patterns ([runFlowsExceptionHandlingDemo]) – try/catch around collect, flow [catch] operator, [onCompletion] cause inspection, validation errors, [retry]/[retryWhen], structured concurrency differences, and [CoroutineExceptionHandler].
 * 10. Async exception handling ([runAsyncExceptionHandlingDemo]) – Differences between `launch` & `async`, exception surfacing only at `await`, impact of `awaitAll`, isolation with `supervisorScope`, late awaiting of failures, and handling un-awaited async with handlers.
 *
 * Key concepts:
 * - Cold flows: Each collector re-executes the builder; collector delays apply backpressure upstream.
 * - Hot flows: Emissions occur regardless of collectors; late subscribers miss prior values unless replay > 0.
 * - SharedFlow/shareIn: shareIn wraps a cold flow, managing upstream job and sharing emissions; scope cancellation is crucial for proper shutdown.
 * - StateFlow: Only emits on distinct changes; supports atomic updates for concurrency.
 * - Backpressure: Collector delays can suspend upstream in cold flows; conflate/buffer can mitigate slow collectors.
 * - Scope management: For infinite/hot flows, use dedicated scopes and cancel them to avoid leaks or hanging main.
 * - Exception handling: Upstream recovery (catch/retry) vs downstream handling (try/catch at collection); structured concurrency cancellation semantics.
 * - Async semantics: Exceptions in `async` coroutines are deferred until `await`; un-awaited failures are reported via handler; `awaitAll` cancels siblings on first failure unless supervised.
 *
 * Usage: Comment/uncomment demo calls in [main] to run them individually for focused experimentation.
 */
import com.playground.kotlin.concurrency.coroutines.log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * A simple [FlowCollector] implementation that logs each received String value.
 */
class MyFlowCollector : FlowCollector<String> {
    override suspend fun emit(value: String) {
        log("Collected $value")
    }
}

/**
 * Represents a simple broadcaster producing random integers every 200ms using a private [MutableSharedFlow].
 * The exposed [messageFlow] is a hot flow (shared) so emissions occur whether or not collectors are currently
 * subscribed. Because no replay/cache is specified (default replay = 0), late subscribers only see future values.
 *
 * Cancellation: The broadcasting loop runs in a coroutine launched on the provided [scope]; cancelling that scope
 * stops further emissions without affecting downstream collectors unless they are also cancelled.
 */
class RadioStation {
    private val _messageFlow = MutableSharedFlow<Int>()
    val messageFlow = _messageFlow.asSharedFlow()

    fun startBroadcasting(scope: CoroutineScope) {
        scope.launch {
            log("Starting broadcasting...")
            while (true) {
                delay(200)
                val number = Random.nextInt(0, 10)
                log("Broadcasting $number")
                _messageFlow.emit(number)
            }
        }
    }
}

/**
 * Holds a numeric counter backed by [MutableStateFlow].
 * Exposes an immutable [StateFlow] via [counter]; updates use atomic [update] to avoid lost increments under concurrency.
 */
class ViewCounter {
    private val _counter = MutableStateFlow(0)
    val counter = _counter.asStateFlow()

    fun increment() {
        _counter.update { it + 1 } // atomic update
    }
}

/** Possible left/right direction values used in the StateFlow demo. */
enum class Direction { LEFT, RIGHT }

/**
 * Simple wrapper around a [MutableStateFlow] of [Direction].
 * Calling [turn] publishes only distinct changes (a same-value assignment is ignored by StateFlow).
 */
class DirectionSelector {
    private val _direction = MutableStateFlow(Direction.LEFT)
    val direction = _direction.asStateFlow()

    fun turn(d: Direction) {
        _direction.update { d }
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
        log("Starting to launch coroutines to send numbers")
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
 * Demonstrates a hot flow using [MutableSharedFlow]:
 * - A [RadioStation] emits random integers indefinitely every 200ms on a dedicated producer scope.
 * - Two collectors start listening at staggered times (500ms and 1000ms) illustrating that they miss earlier
 *   values (no replay) and only receive emissions from the moment they subscribe.
 * - After 2 seconds the producer scope and the grouped receivers job are cancelled, stopping emissions and
 *   collection cleanly.
 * - Shows separation of producer and consumer lifecycles.
 */
private suspend fun CoroutineScope.runHotFlowDemo() {
    val radioStation = RadioStation()
    val stationScope = CoroutineScope(Dispatchers.Default)

    radioStation.startBroadcasting(stationScope)

    // Group both collectors under a single Job
    val receiversJob = launch {
        launch {
            delay(500.milliseconds)
            log("First collector is starting to listen")
            radioStation.messageFlow.collect {
                log("First collector received $it")
            }
        }

        launch {
            delay(1000.milliseconds)
            log("Second collector is starting to listen")
            radioStation.messageFlow.collect {
                log("Second collector received $it")
            }
        }
    }

    delay(2000.milliseconds)
    stationScope.cancel() //stop the station scope (and thus the radio station)
    receiversJob.cancel() //stop the receivers job (and thus the collectors)
}

/**
 * Sample sensor query producing a pseudo temperature in Celsius (range -10..30).
 * Uses a random number each invocation to simulate real sensor variability.
 */
fun querySensor(): Int = Random.nextInt(-10..30)

/**
 * Infinite cold flow of temperature samples every 500 ms.
 * Each collection re-executes the loop (hence sharing it with [shareIn] prevents duplicate polling work).
 */
fun getTemperaturesFlow(): Flow<Int> {
    return flow {
        while (true) {
            val querySensor = querySensor()
            log("Emitting temperature $querySensor (Celsius)")
            emit(querySensor)
            delay(500.milliseconds)
        }
    }
}

/** Convert Celsius to Fahrenheit using the standard formula. */
fun celsiusToFahrenheit(celsius: Int) = celsius * 9.0 / 5.0 + 32.0

/**
 * Demonstrates sharing a cold infinite flow using [shareIn]:
 * - [getTemperaturesFlow] is cold; collecting twice independently would duplicate sensor polling.
 * - `shareIn(temperaturesScope, SharingStarted.Lazily)` defers upstream start until first collector subscribes.
 * - Two sibling collectors transform/display the same upstream emissions (Celsius & Fahrenheit) concurrently.
 * - After 2 seconds both the collectors job and the upstream scope are cancelled, stopping further work.
 */
private suspend fun CoroutineScope.runSharedFlowDemo() {
    log("Starting shared flow demo")
    val temperatures = getTemperaturesFlow()
    val temperaturesScope = CoroutineScope(Dispatchers.Default)
    val sharedTemperatures = temperatures.shareIn(temperaturesScope, SharingStarted.Lazily)

    // Group both collectors under a single Job
    val collectorsJob = launch {
        launch {
            sharedTemperatures.collect {
                log("Celsius: $it")
            }
        }

        launch {
            sharedTemperatures.collect {
                log("Fahrenheit: ${celsiusToFahrenheit(it)}")
            }
        }
    }

    delay(2000.milliseconds)
    collectorsJob.cancel() //stop the collectors job (and thus the collectors)
    temperaturesScope.cancel() //stop the temperatures scope (and thus the temperature flow)
}

/**
 * Spawns 1000 concurrent coroutines each incrementing a shared counter using [ViewCounter.increment].
 * Because increment relies on atomic [MutableStateFlow.update], the final value should reliably reach 1000.
 */
private suspend fun runStateFlowAtomicUpdateDemo() {
    val viewCounter = ViewCounter()
    CoroutineScope(Dispatchers.Default).launch {
        repeat(1_000) {
            launch {
                viewCounter.increment()
            }
        }
    }.join()

    log(viewCounter.counter.value) // should be 1000 because 'increment' function implemented using atomic update
}

/**
 * Demonstrates StateFlow emitting only when the underlying value actually changes.
 * A duplicate assignment (LEFT -> LEFT) does not produce an additional emission.
 */
private suspend fun CoroutineScope.runStateFlowChangesEmissionDemo() {
    log("Starting state flow changes emission demo...")
    val directionSwitch = DirectionSelector()

    val directionCollectionJob = launch {
        directionSwitch.direction.collect {
            log("Direction now $it")
        }
    }

    delay(200.milliseconds)
    directionSwitch.turn(Direction.RIGHT)
    delay(200.milliseconds)
    directionSwitch.turn(Direction.LEFT)
    delay(200.milliseconds)
    directionSwitch.turn(Direction.LEFT) // no emission, same value

    delay(500.milliseconds)
    directionCollectionJob.cancel()
}

/**
 * Converts an infinite cold temperature flow into a hot [StateFlow] using [stateIn].
 * Shows reading the latest cached value without collecting the upstream again.
 * IMPORTANT: cancel the hosting scope to stop the infinite upstream when finished.
 */
private suspend fun runColdFlowToHotStateFlowDemo() {
    log("Starting cold flow to hot state flow demo...")

    val temperaturesFlow = getTemperaturesFlow()
    val temperaturesScope = CoroutineScope(Dispatchers.Default)
    val tempState = temperaturesFlow.stateIn(temperaturesScope)

    log("Temperature state: ${tempState.value}")
    delay(800.milliseconds)
    log("Temperature state: ${tempState.value}") // should be different from the first value

    delay(800.milliseconds)
    temperaturesScope.cancel()
}

/**
 * Processes a flow of integers, logging lifecycle events and values.
 *
 * - Emits a default value if the flow is empty.
 * - Logs when the flow starts, each value, and when it completes.
 * - Collects and logs each value.
 *
 * @param flow The flow to process.
 */
suspend fun process(flow: Flow<Int>) {
    flow
        .onEmpty {
            log("Nothing - emitting default value!")
            emit(0)
        }
        .onStart {
            log("Starting!")
        }
        .onEach {
            log("On $it!")
        }
        .onCompletion {
            log("Done!")
        }
        .collect {
            log("Collecting $it")
        }
}

/**
 * Simulates fetching all user IDs from a database as a flow.
 *
 * Emits three user IDs (0, 1, 2) with a delay to mimic database latency.
 *
 * @return A cold flow emitting user IDs.
 */
fun getAllUserIds(): Flow<Int> {
    return flow {
        repeat(3) {
            delay(200.milliseconds) // Database latency
            log("Emitting user ID: $it !")
            emit(it)
        }
    }
}

/**
 * Simulates fetching a user profile from the network.
 *
 * @param id The user ID to fetch.
 * @return The profile string for the user.
 */
suspend fun getProfileFromNetwork(id: Int): String {
    delay(2.seconds) // Network latency
    return "Profile[$id]"
}

/**
 * Emits a sequence of search query strings with delays, simulating user typing.
 *
 * Emits: "K", "Ko", "Kotl", "Kotlin" with increasing delays between each.
 * Useful for demonstrating debounce and other flow operators on user input.
 */
private fun searchQuery() = flow {
    emit("K")
    delay(100.milliseconds)
    emit("Ko")
    delay(200.milliseconds)
    emit("Kotl")
    delay(500.milliseconds)
    emit("Kotlin")
}

/**
 * Extension operator for Flow<Double> that emits the average of the last [n] values seen.
 *
 * Maintains a rolling window of the last [n] numbers and emits their average on each new value.
 *
 * @param n Number of last values to average.
 * @return Flow<Double> emitting the rolling average.
 */
fun Flow<Double>.averageOfLast(n: Int): Flow<Double> =
    flow {
        val numbers = mutableListOf<Double>()
        collect {
            if (numbers.size >= n) {
                numbers.removeFirst()
            }
            numbers.add(it)
            emit(numbers.average())
        }
    }

/**
 * Demonstrates a wide range of flow transformation operators and their effects on concurrency, backpressure, context switching, and lifecycle events.
 *
 * Features shown:
 * - [transform]: Emits both uppercase and lowercase versions of each string.
 * - [take]: Collects a limited number of items from an infinite flow, showing automatic cancellation.
 * - [onCompletion], [onStart], [onEmpty], [onEach]: Logs lifecycle events and values.
 * - [buffer]: Buffers emissions to allow downstream processing in batches.
 * - [flatMapMerge]: Concurrently fetches user profiles for multiple IDs.
 * - [conflate]: Demonstrates skipping intermediate values if collector is slow, mitigating backpressure.
 * - [debounce]: Simulates user input and demonstrates debounced search queries.
 * - [flowOn]: Shows context switching between different dispatchers during flow processing.
 * - [averageOfLast]: Custom operator to emit rolling averages from a flow of numbers.
 *
 * Each transformation is logged to illustrate the order and timing of events, and the impact of concurrency, buffering, and context switching.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
private suspend fun runFlowsTransformationsDemo() {
    log("Starting flows transformations demo...")

    flowOf("Kotlin", "Java", "JavaScript")
        .transform {
            emit(it.uppercase())
            emit(it.lowercase())
        }.collect { log(it) }

    log("Take 5 temperatures from infinite flow and automatically cancel flow...")
    getTemperaturesFlow()
        .take(5)
        .onCompletion { cause ->
            if (cause == null) {
                log("Successfully collected 5 temperature samples, flow is complete.")
            } else {
                log("Flow completed with error: $cause")
            }
        }
        .collect { log(it) }

    process(flowOf(1, 2, 3))
    process(flowOf())

    log("Fetching profiles for all users sequentially...")
    getAllUserIds()
        .map { getProfileFromNetwork(it) }
        .collect { log("Got profile: $it") }

    log("Fetching profiles for all users in buffered batches of 3...")
    getAllUserIds()
        .buffer(3) // buffer up to 3 emissions before applying map
        .map { getProfileFromNetwork(it) }
        .collect { log("Got profile: $it") }

    log("Fetching profiles for all users concurrently in batches of 3...")
    getAllUserIds()
        .buffer(3) // buffer up to 3 emissions before applying map
        .flatMapMerge(concurrency = 3) { // start each network call in its own coroutine
            flow {
                emit(getProfileFromNetwork(it)) // suspends inside flow, not before
            }
        }
        .collect { log("Got profile: $it") }

    log("Conflating temperature emissions to avoid slow collector backpressure...")
    val conflateTemperaturesJob = CoroutineScope(Dispatchers.Default).launch {
        getTemperaturesFlow()
            .onEach { log("Read $it from sensor") }
            .conflate() // skip intermediate values if collector is slow
            .collect {
                delay(1.seconds)
                log("Collected $it")
            }
    }
    delay(3.seconds)
    conflateTemperaturesJob.cancel()

    log("Debouncing search query inputs...")
    searchQuery()
        .debounce(250.milliseconds)
        .collect {
            log("Searching for $it")
        }

    log("Demonstrating flowOn operator with multiple context switches...")
    flowOf(1)
        .onEach { log("A") }
        .flowOn(Dispatchers.Default)
        .onEach { log("B") }
        .flowOn(Dispatchers.IO)
        .onEach { log("C") }
        .collect()

    log("Calculating average of last 3 numbers in a flow using custom flow intermediate operator...")
    flowOf(1.0, 2.0, 30.0, 121.0)
        .averageOfLast(3)
        .collect {
            print("$it ")
        }
}

/**
 * Demonstrates multiple exception handling approaches for coroutines and flows.
 *
 * Patterns covered:
 * 1. try/catch around terminal collection – handles any exception that propagates out of the flow pipeline.
 * 2. Flow [catch] operator – intercepts upstream exceptions and can emit fallback values (resumes downstream collection).
 * 3. [onCompletion] cause inspection – distinguishes normal completion from exceptional completion.
 * 4. Validation error inside intermediate operator (e.g. [map] with [require]) – converted to fallback via [catch].
 * 5. Transient failure recovery with [retry] (fixed count) and [retryWhen] (conditional/backoff logic).
 * 6. Structured concurrency semantics: failing child inside regular [coroutineScope] cancels siblings; inside [supervisorScope] it does not.
 * 7. [CoroutineExceptionHandler] for handling uncaught exceptions in launched coroutines that are not awaited.
 *
 * Each section logs its behavior so you can observe propagation vs interception.
 */
private suspend fun runFlowsExceptionHandlingDemo() {
    log("Starting flows exception handling demo...")

    // 1. Upstream flow that fails mid-emission
    val failingFlow = flow {
        log("Upstream: emitting 1")
        emit(1)
        delay(100.milliseconds)
        log("Upstream: throwing IllegalStateException")
        throw IllegalStateException("Boom")
    }

    // try/catch around collect
    try {
        failingFlow.collect { log("Collected: $it") }
    } catch (e: Exception) {
        log("try/catch around collect handle: $e")
    }

    // catch operator + onCompletion cause inspection
    failingFlow
        .onCompletion { cause ->
            if (cause != null) {
                log("onCompletion saw cause: $cause")
            } else log("onCompletion normal completion")
        }
        .catch { e ->
            log("catch operator saw: $e; emitting fallback -1")
            emit(-1)
        }
        .collect { log("Collected with catch pipeline: $it") }

    // 2. Validation error inside map
    flowOf(1, 0, 2)
        .map { require(it > 0) { "Non-positive value: $it" }; it }
        .catch { e ->
            log("Validation flow caught: $e; emitting 999")
            emit(999)
        }
        .collect { log("Validation collected: $it") }

    // 3. Transient failure recovery with retry (fixed attempts)
    var attempt = 0
    val transientFlow = flow {
        attempt++
        log("transientFlow attempt $attempt")
        if (attempt < 3) throw RuntimeException("Transient failure on attempt $attempt")
        emit("Success on attempt $attempt")
    }
    transientFlow
        .retry(retries = 2) // total attempts = 1 original + 2 retries
        .catch { e -> log("retry gave up: $e") }
        .collect { log("retry collected: $it") }

    // 4. Conditional retryWhen with max attempts
    var attempt2 = 0
    flow {
        attempt2++
        log("retryWhen upstream attempt $attempt2")
        if (attempt2 < 4) throw IllegalStateException("Still failing attempt $attempt2")
        emit("Final success at $attempt2")
    }
        .retryWhen { cause, attemptIdx ->
            log("retryWhen saw cause=$cause at attempt=$attemptIdx; deciding...")
            attemptIdx < 5 // allow retries until 5 attempts (attemptIdx starts at 0)
        }
        .catch { e -> log("retryWhen gave up: $e") }
        .collect { log("retryWhen collected: $it") }

    // 5. Structured concurrency: regular coroutineScope cancellation propagation
    try {
        coroutineScope {
            launch {
                delay(100.milliseconds)
                throw IllegalStateException("Failure in regular scope cancels siblings")
            }
            launch {
                repeat(5) {
                    delay(150.milliseconds)
                    log("Sibling tick (regular scope) $it")
                }
            }
        }
    } catch (e: Exception) {
        log("coroutineScope propagated exception: $e")
    }

    // 6. supervisorScope keeps siblings alive on failure
    // ORIGINAL behavior (stack trace noise) replaced with a handled version.
    // We attach a CoroutineExceptionHandler to the failing child launch so its exception is logged via our
    // own handler instead of printing an uncaught stack trace. This demonstrates fully silent isolation.
    supervisorScope {
        val childHandler = CoroutineExceptionHandler { _, e ->
            log("Handled child error (supervisorScope): $e")
        }
        launch(childHandler) {
            delay(100.milliseconds)
            throw IllegalArgumentException("Child failure inside supervisorScope")
        }
        launch {
            repeat(3) {
                delay(150.milliseconds)
                log("Independent child work tick (supervisorScope) $it")
            }
        }
        delay(600.milliseconds) // allow second child to finish even though first failed
    }
    log("Supervisor scope finished; second child unaffected by first failure")

    // 7. CoroutineExceptionHandler for uncaught exception in a fire-and-forget child
    val handler = CoroutineExceptionHandler { _, e ->
        log("CoroutineExceptionHandler caught: $e")
    }
    // Fire-and-forget launch (not awaited) – handler prevents silent crash and logs exception
    CoroutineScope(Dispatchers.Default).launch(handler) {
        delay(100.milliseconds)
        throw IllegalStateException("Fire-and-forget failure")
    }.join() // we join just to keep demo deterministic

    log("Flows exception handling demo complete.")
}

/**
 * Demonstrates exception handling patterns specifically for `async` coroutines versus `launch`.
 *
 * Covered scenarios:
 * 1. Single failing async – exception deferred until `await` (try/catch around `await`).
 * 2. Multiple async children with one failure – `awaitAll` throws first failure and cancels remaining.
 * 3. Isolation using `supervisorScope` – collect partial successes; failing deferred doesn't cancel siblings.
 * 4. Late awaiting a failure – work continues until the `await` point surfaces the exception.
 * 5. Un-awaited async (fire-and-forget) – attaching a `CoroutineExceptionHandler` and `invokeOnCompletion` to observe failure.
 *
 * Logs show when exceptions are thrown versus when they are observed, highlighting deferred error propagation.
 */
private suspend fun runAsyncExceptionHandlingDemo() {
    log("Starting async exception handling demo...")

    // 1. Single failing async (exception deferred until await)
    val singleResult = try {
        coroutineScope {
            val deferred = async {
                delay(100.milliseconds)
                log("single async about to throw")
                throw IllegalStateException("Failure inside single async")
            }
            deferred.await() // exception surfaces here
        }
    } catch (e: Exception) {
        log("Caught from single await: $e")
        null
    }
    log("Single result: $singleResult (expected null due to failure)")

    // 2. Multiple async children; one fails; awaitAll cancels siblings
    try {
        coroutineScope {
            val d1 = async { delay(50.milliseconds); log("d1 done"); 1 }
            val d2 = async { delay(100.milliseconds); log("d2 throwing"); throw RuntimeException("d2 failed") }
            val d3 = async { delay(150.milliseconds); log("d3 done (will be cancelled if d2 fails early)"); 3 }
            val all = awaitAll(d1, d2, d3) // throws due to d2 failure; d3 may be cancelled if not finished
            log("All results: $all")
        }
    } catch (e: Exception) {
        log("awaitAll caught: $e")
    }

    // 3. supervisorScope: collect partial successes even with a failure
    supervisorScope {
        val d1 = async { delay(50.milliseconds); 1 }
        val d2 = async { delay(100.milliseconds); throw RuntimeException("d2 failed (supervisor)") }
        val d3 = async { delay(150.milliseconds); 3 }
        val results = listOf(d1, d2, d3).map { deferred ->
            runCatching { deferred.await() }
                .onFailure { log("Deferred failed (supervisor): $it") }
                .getOrNull()
        }
        log("Supervisor partial results: ${results.filterNotNull()}") // expect [1,3]
    }

    // 4. Late await of failure – other work proceeds until await
    try {
        coroutineScope {
            val d = async {
                delay(120.milliseconds)
                throw IllegalArgumentException("Deferred failure before late await")
            }
            // Do unrelated work prior to awaiting
            repeat(2) { i ->
                delay(60.milliseconds)
                log("Unrelated work chunk $i done before awaiting")
            }
            d.await() // exception surfaces now
        }
    } catch (e: Exception) {
        log("Late await caught: $e")
    }

    // 5. Un-awaited async with handler and invokeOnCompletion
    // This handler is not invoked because we call 'async' method, but handler is invoked only for 'launch' method.
    // If you change 'async' to 'launch' below, the handler will be invoked.
    val handler = CoroutineExceptionHandler { _, e ->
        log("Async fire-and-forget handler observed: $e")
    }
    val parent = CoroutineScope(Dispatchers.Default + handler)
    val unawaited = parent.async {
        delay(100.milliseconds)
        throw IllegalStateException("Unawaited async failure")
    }
    unawaited.invokeOnCompletion { cause ->
        log("Unawaited deferred completion cause: $cause")
    }
    unawaited.join() // ensure demo finishes deterministically

    log("Async exception handling demo complete.")
}

/**
 * Entry point running all flows demonstrations inside a blocking scope.
 *
 * Runs all demo functions sequentially. Comment/uncomment calls for focused runs.
 */
fun main(): Unit = runBlocking {
    runBaseFlowDemo()
    runChannelFlowDemo()
    runHotFlowDemo()
    runSharedFlowDemo()
    runStateFlowAtomicUpdateDemo()
    runStateFlowChangesEmissionDemo()
    runColdFlowToHotStateFlowDemo()
    runFlowsTransformationsDemo()
    runFlowsExceptionHandlingDemo()
    runAsyncExceptionHandlingDemo()
}
