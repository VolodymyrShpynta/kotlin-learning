import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

private var zeroTime = System.currentTimeMillis()

fun log(message: Any?) =
    println("${System.currentTimeMillis() - zeroTime} [${Thread.currentThread().name}] $message")

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

suspend fun fetchUser(): String {
    delay(1000L) // Simulate network delay
    return "John Doe"
}

suspend fun fetchPosts(): String {
    delay(1500L) // Simulate network delay
    return "List of posts"
}

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

private suspend fun calculateSomething(): Int {
    delay(2000L)
    return 2 + 2
}

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

fun main() = runBlocking {
    runCoroutineLifecycleDemo()
    runCoroutineDispatchersDemo()
    runCoroutineConcurrentExecutionDemo()
    runCoroutineWithTimeoutDemo()
}
