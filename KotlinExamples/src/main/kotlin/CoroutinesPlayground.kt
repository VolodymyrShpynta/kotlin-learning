/**
 * Coroutines Playground
 *
 * This file groups a series of focused coroutine demonstrations illustrating:
 * 1. Lifecycle & cancellation ([runCoroutineLifecycleDemo]) – launching multiple jobs, waiting, and cancelling
 *    a parent with a running child.
 * 2. Dispatchers ([runCoroutineDispatchersDemo]) – contrasting Default/IO/Unconfined scheduling characteristics.
 * 3. Sequential vs. concurrent execution ([runCoroutineConcurrentExecutionDemo]) – using [async]/await to overlap
 *    suspending work compared with sequential calls.
 * 4. Timeouts ([runCoroutineWithTimeoutDemo]) – applying structured time limits via [withTimeoutOrNull].
 *
 * A lightweight `log` helper prints a relative timestamp (ms since program start) and the current thread name so
 * you can visually correlate ordering, concurrency, and dispatcher behavior.
 *
 * Run [main] to execute the demos in sequence. Feel free to comment/uncomment individual calls while exploring.
 */
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

/** Epoch used to compute relative times for [log]. */
private var zeroTime = System.currentTimeMillis()

/**
 * Logs a message with: elapsedMillis [threadName] message.
 * Helpful for observing which dispatcher/threads execute particular coroutine segments and relative ordering.
 */
fun log(message: Any?) =
    println("${System.currentTimeMillis() - zeroTime} [${Thread.currentThread().name}] $message")

/**
 * Demonstrates coroutine lifecycle operations inside a [CoroutineScope]:
 * - Launches three jobs (one spawns a child) with varying delays.
 * - Uses [join] to await completion of specific jobs.
 * - Cancels `job3`, showing that its child ("Coroutine 3.1") is also cancelled (structured concurrency) so you
 *   should not see its completion log.
 * - Shows that other jobs proceed independently.
 */
private suspend fun CoroutineScope.runCoroutineLifecycleDemo() {
    val job1 = launch {
        repeat(3) { i ->
            log("Coroutine 1 working: $i")
            delay(700L)
        }
        log("Coroutine 1 done")
    }

    val job2 = launch {
        log("Coroutine 2 starting")
        delay(500L)
        log("Coroutine 2 finished after 1 second")
    }

    val job3 = launch {
        log("Coroutine 3 starting")
        delay(200L)
        launch {
            log("Coroutine 3.1 starting")
            delay(1000L)
            log("Coroutine 3.1 finished after 1 second")
        }
        log("Coroutine 3 finished after 0.2 second")
    }

    log("Main thread is free to do other work...")

    job2.join() // Wait for the second coroutine to finish
    job3.cancel() // Cancel the third coroutine and its child - check the logs and make sure there is no "Coroutine 3.1 finished" message
    log("Cancelled Coroutine 3")
    job1.join() // Wait for the first coroutine to finish
    log("Coroutines have completed.")
}

/**
 * Shows how different dispatchers schedule work:
 * - [Dispatchers.Default]: CPU‑optimized shared pool.
 * - [Dispatchers.IO]: Optimized for blocking IO (may expand pool size).
 * - [Dispatchers.Unconfined]: Starts in current thread, may resume in a different one after suspension; good for
 *   exploration only—avoid for production structured code unless you understand its semantics.
 */
private fun CoroutineScope.runCoroutineDispatchersDemo() {
    log("Starting coroutines with different dispatchers")
    launch(Dispatchers.Default) {
        log("Running in Default dispatcher")
        delay(500L)
        log("Finished in Default dispatcher")
    }

    launch(Dispatchers.IO) {
        log("Running in IO dispatcher")
        delay(500L)
        log("Finished in IO dispatcher")
    }

    launch(Dispatchers.Unconfined) {
        log("Running in Unconfined dispatcher")
        delay(500L)
        log("Finished in Unconfined dispatcher")
    }

    log("Main thread is free to do other work...")
}

/** Simulated network/user profile fetch (1s). */
suspend fun fetchUser(): String {
    delay(1000L) // Simulate network delay
    return "John Doe"
}

/** Simulated network/posts fetch (1.5s). */
suspend fun fetchPosts(): String {
    delay(1500L) // Simulate network delay
    return "List of posts"
}

/**
 * Compares sequential vs. concurrent execution of two independent suspend functions.
 * Sequential: waits for user then posts (≈ 2.5s total). Concurrent: launches both with [async]; total ≈ max of both
 * durations (≈ 1.5s). Demonstrates eager start of `async` blocks and awaiting results later.
 */
private suspend fun CoroutineScope.runCoroutineConcurrentExecutionDemo() {
    log("Starting sequential fetch...")
    log(fetchUser())
    log(fetchPosts())

    log("Starting concurrent fetch...")
    val deferredUser = async { fetchUser() }
    val deferredPosts = async { fetchPosts() }
    log(deferredUser.await())
    log(deferredPosts.await())
}

/** Pretend expensive computation returning a fixed value after 2s. */
private suspend fun calculateSomething(): Int {
    delay(2000L)
    return 2 + 2
}

/**
 * Demonstrates applying timeouts to suspend functions:
 * - First call has a timeout shorter than the underlying work, yielding `null`.
 * - Second call's timeout is longer, so the result completes successfully.
 * Uses [withTimeoutOrNull] to avoid exceptions on timeout and return a nullable result instead.
 */
private suspend fun runCoroutineWithTimeoutDemo() {
    val quickResult = withTimeoutOrNull(500.milliseconds) {
        calculateSomething()
    }
    log("Quick result: $quickResult")

    val longResult = withTimeoutOrNull(3000.milliseconds) {
        calculateSomething()
    }
    log("Long result: $longResult")
}

/** Entry point running all coroutine demos sequentially inside a [runBlocking] scope. */
fun main() = runBlocking {
    runCoroutineLifecycleDemo()
    runCoroutineDispatchersDemo()
    runCoroutineConcurrentExecutionDemo()
    runCoroutineWithTimeoutDemo()
}
