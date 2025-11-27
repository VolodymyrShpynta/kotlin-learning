package com.playground.kotlin.concurrency

/**
 * Coroutines & Flows Testing Showcase
 *
 * Purpose:
 * A compact, idiomatic reference for testing Kotlin coroutines and flows using
 * kotlinx-coroutines-test and Turbine. Everything here is fast, deterministic, and free of real delays.
 *
 * Libraries / Versions (sync with build.gradle.kts):
 * - Kotlin: 2.2.10
 * - kotlinx-coroutines-core / -test: 1.10.2
 * - Turbine: 1.2.1
 *
 * Covered Concepts (each test numbered):
 *  01. Virtual time basics – delay() advances virtual clock, not wall time.
 *  02. runCurrent – drain already-scheduled (immediate / undispatched) work without time travel.
 *  03. advanceTimeBy – jump forward by a fixed duration; runs due delayed tasks.
 *  04. advanceUntilIdle – keep advancing until no queued tasks remain (avoid with infinite sources!).
 *  05. Cold Flow – builder re-executes per collection (side-effect proves restart).
 *  06. Turbine success path – ordered item assertions + completion.
 *  07. Turbine failure path – capture terminal exception via awaitError().
 *  08. StateFlow distinct emissions – duplicates suppressed; deterministic progression using runCurrent.
 *  09. SharedFlow replay – late subscriber receives cached emission (replay=1).
 *  10. Shared TestCoroutineScheduler – virtual time carries across multiple runTest blocks.
 *  11. SupervisorJob + launch – child failure handled by CoroutineExceptionHandler; parent not cancelled.
 *  12. SupervisorJob + async – failure surfaces only at await(); parent continues.
 *  13. Deferred failure surfacing under supervisorScope – sibling completes; exception observed at await().
 *  14. Flow debounce – last emission after quiet period; rapid earlier emissions suppressed.
 *  15. Flow sample – periodic snapshots of latest value.
 *  16. Flow timeout – cancellation when next emission exceeds deadline.
 *  17. awaitAll – structured concurrency waiting for multiple deferred results; ordering preserved.
 *  18. Parent cancellation – cooperative propagation to children (non-supervised hierarchy).
 *
 * Quick Cheat Sheet:
 *  - runTest { }                  => Provides TestScope + virtual scheduler.
 *  - delay(x)                     => Advances virtual time; use currentTime for assertions.
 *  - runCurrent()                 => Execute tasks scheduled at NOW (microtasks) without time travel.
 *  - advanceTimeBy(ms)            => Jump forward ms; executes tasks whose scheduled time <= new time.
 *  - advanceUntilIdle()           => Repeatedly runCurrent/advance until no tasks remain.
 *  - Turbine test { }             => awaitItem(), awaitComplete(), awaitError(), expectNoEvents().
 *  - MutableStateFlow             => Emits only changed/distinct values.
 *  - MutableSharedFlow(replay=1)  => New collector instantly receives last value.
 *  - debounce(timeout)            => Suppresses rapid emissions; last value after quiet window.
 *  - sample(period)               => Emits latest value at fixed time intervals.
 *  - timeout(duration)            => Throws TimeoutCancellationException if next emission late.
 *  - awaitAll(d1, d2, ...)        => Await multiple deferred results concurrently.
 *  - supervisorScope { }          => Child failure isolated; must await() to surface exception.
 *  - Parent job cancel            => Cascades cancellation to children unless supervised.
 *
 * Gotchas / Tips:
 *  - Do NOT mix real Dispatchers (e.g., Dispatchers.IO) inside runTest; keep everything test-only for determinism.
 *  - advanceUntilIdle() will never terminate if new delayed work keeps being scheduled (e.g., while(true) { delay(...) }).
 *  - Using a shared TestCoroutineScheduler lets you assert cumulative virtual time across separate tests or blocks.
 *  - Supervision patterns vary: SupervisorJob + launch vs supervisorScope + async; both isolate failures differently.
 *  - For time-window operators (debounce / sample / timeout), prefer explicit advanceTimeBy steps.
 *  - Some operators (debounce, sample) may require @OptIn(FlowPreview::class).
 *
 * Extending Ideas:
 *  - Add throttleFirst / throttleLatest tests (custom operators or channel-based implementations).
 *  - Contrast nested supervisorScopes with mixed supervised / unsupervised children.
 *  - Explore testScheduler.advanceUntilIdle vs manual staged advanceTimeBy for complex graphs.
 *
 * All tests are intentionally small; they double as teaching snippets.
 */
import app.cash.turbine.test
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class CoroutinesAndFlowsTest {

    /** Test 01: Virtual time – a 20s delay completes instantly under runTest; currentTime reflects advancement. */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun delay_is_virtual_with_runTest() = runTest {
        val start = currentTime
        delay(20.seconds) // advances virtual time, not wall clock
        assertEquals(start + 20_000, currentTime)
    }

    /** Test 02: runCurrent drains undispatched tasks without advancing virtual time. */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun runCurrent_executes_pending_without_advancing_time() = runTest {
        var count = 0
        launch {
            count++
            launch { count++ }
        } // scheduled immediately but not run yet
        assertEquals(0, count) // not run yet
        runCurrent() // drain microtasks / immediate dispatchers
        assertEquals(2, count)
        assertEquals(0, currentTime)
    }

    /** Test 03: advanceTimeBy moves virtual clock; tasks whose delay elapses run. */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun advanceTimeBy_advances_and_runs_due_tasks() = runTest {
        var flag = 0
        launch { delay(500.milliseconds); flag = 1 }
        advanceTimeBy(400) // not yet due
        assertEquals(0, flag)
        advanceTimeBy(110) // reaches >= 500ms
        assertEquals(1, flag)
        assertEquals(510, currentTime) // cumulative jumping, not exact targeted delay value
    }

    /** Test 04: advanceUntilIdle keeps advancing until queue empty (avoid with infinite repeating work). */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun advanceUntilIdle_runs_all_scheduled_work() = runTest {
        var hits = 0
        launch {
            repeat(3) {
                delay(200); hits++
            }
        }
        advanceUntilIdle()
        assertEquals(3, hits)
        assertEquals(600, currentTime) // 3 * 200ms
    }

    /** Test 05: Cold flow builder re-executes per collection (build counter proves restart). */
    @Test
    fun coldFlow_is_restarted_per_collection() = runTest {
        var buildCount = 0
        val cold = flow {
            buildCount++
            emit(1); emit(2)
        }
        val first = mutableListOf<Int>()
        val second = mutableListOf<Int>()
        cold.collect { first += it }
        cold.collect { second += it }
        assertEquals(listOf(1, 2), first)
        assertEquals(listOf(1, 2), second)
        assertEquals(2, buildCount) // proves separate executions
    }

    /** Test 06: Turbine success path – assert ordered emissions and completion. */
    @Test
    fun turbine_collects_emissions_in_order_and_completion() = runTest {
        flowOf("A", "B", "C").test {
            assertEquals("A", awaitItem())
            assertEquals("B", awaitItem())
            assertEquals("C", awaitItem())
            awaitComplete() // Flow finishes successfully
        }
    }

    /** Test 07: Turbine failure path – capture upstream exception with awaitError(). */
    @Test
    fun turbine_captures_exception() = runTest {
        val ex = IllegalStateException("Boom")
        flow { emit("X"); throw ex }.test {
            assertEquals("X", awaitItem())
            val thrown = awaitError()
            assertTrue(thrown is IllegalStateException)
            assertEquals("Boom", thrown.message)
        }
    }

    /** Test 08: StateFlow emits only distinct values; runCurrent processes synchronous emissions. */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun stateFlow_emits_distinct_values_only() = runTest {
        val state = MutableStateFlow(0)
        val values = mutableListOf<Int>()
        val job = launch { state.collect { values += it } }

        runCurrent() // collect initial value (0)

        state.value = 1
        runCurrent() // process emission of 1

        state.value = 1 // duplicate ignored (no new emission)
        runCurrent() // nothing new

        state.value = 2
        runCurrent() // process emission of 2

        job.cancel()
        assertEquals(listOf(0, 1, 2), values)
    }

    /** Test 09: SharedFlow replay (replay=1) delivers cached value to first late collector. */
    @Test
    fun sharedFlow_replay_delivers_cached_value() = runTest {
        val shared = MutableSharedFlow<Int>(replay = 1)
        shared.emit(42)
        var received = 0
        shared.take(1).collect { received = it }
        assertEquals(42, received)
    }

    /** Test 10: Custom TestCoroutineScheduler shared across multiple runTest blocks accumulates virtual time. */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun customScheduler_sharedVirtualTime_across_runTest_blocks() {
        val scheduler = TestCoroutineScheduler()
        runTest(scheduler) {
            var ticks = 0
            launch { repeat(3) { delay(1.seconds); ticks++ } }
            advanceTimeBy(3_010) // advance 3s (a little extra padding)
            assertEquals(3, ticks)
            assertEquals(3_010, currentTime)
        }
        // Second runTest with same scheduler resumes at prior virtual time.
        runTest(scheduler) {
            assertEquals(3_010, currentTime) // carried over
            var flag = false
            launch { delay(500.milliseconds); flag = true }
            advanceTimeBy(510)
            assertTrue(flag)
            assertEquals(3_520, currentTime)
        }
    }

    /**
     * Test 11: SupervisorJob + launch – child coroutine failure does NOT cancel its parent
     * when the child is launched with a `SupervisorJob`. The exception is handled
     * by a `CoroutineExceptionHandler` instead of propagating upward.
     *
     * Key points:
     * - `SupervisorJob` breaks the cancellation link between child and parent.
     * - `CoroutineExceptionHandler` catches the thrown exception globally for that coroutine.
     * - Parent scope completes normally despite the child failure.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun child_failure_is_handled_by_exceptionHandler_with_supervisorJob() = runTest {
        var handlerInvoked = false
        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception")
            handlerInvoked = true
        }

        coroutineScope {
            launch {
                // Child launched with SupervisorJob + exception handler
                launch(SupervisorJob() + exceptionHandler) {
                    delay(1000.milliseconds)
                    throw IllegalArgumentException("Error in coroutine")
                }
            }
        }

        // Advance virtual time so the inner coroutine runs and fails
        advanceTimeBy(1000)
        runCurrent()

        // Assert that the handler caught the exception and parent scope survived
        assertTrue(handlerInvoked, "ExceptionHandler should have been invoked")
    }

    /**
     * Test 12: SupervisorJob + async – the parent coroutine
     * is NOT cancelled on child failure. The exception surfaces only at `await()`
     * and can be handled locally with `try/catch`.
     *
     * Key points:
     * - `async` normally cancels its parent on failure; adding `SupervisorJob` prevents that.
     * - Exception is deferred until `await()` is called.
     * - Parent coroutine continues running and handles the error gracefully.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun async_failure_is_caught_locally_with_supervisorJob() = runTest {
        var caughtMessage: String? = null

        launch {
            val deferred = async(SupervisorJob()) {
                delay(1000.milliseconds)
                throw IllegalArgumentException("Error in coroutine")
            }

            try {
                deferred.await()
            } catch (e: Exception) {
                println("Caught via await: $e")
                caughtMessage = e.message
            }
        }

        // Advance virtual time so the async block executes and fails
        advanceTimeBy(1000)
        runCurrent()

        // Assert that the exception was caught locally
        assertEquals("Error in coroutine", caughtMessage)
    }

    /** Test 13: Verifies that an async coroutine's failure does not cancel siblings
     * and that the exception surfaces only when `await()` is called.
     *
     * Why this approach?
     * - We use `supervisorScope` to enforce supervision semantics:
     *   a failing child does NOT cancel its parent or siblings.
     * - This keeps everything inside the test scope (no extra CoroutineScope),
     *   so lifecycle and virtual time remain simple and leak-free.
     * - The test clearly demonstrates the intended behavior:
     *   sibling completes first, then failure is observed at await().
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun async_failure_is_deferred_until_await() = runTest {
        supervisorScope {
            val deferred = async {
                delay(100)
                throw IllegalArgumentException("boom")
            }

            var sideWork = 0
            launch {
                delay(50)
                sideWork = 1
            }

            // Advance time so sibling finishes but async hasn't failed yet.
            advanceTimeBy(60)
            assertEquals(1, sideWork, "Sibling should complete before async failure surfaces")

            // Advance time so async finishes and fails internally.
            advanceTimeBy(50)

            // Exception should surface only now, at await().
            val ex = assertFailsWith<IllegalArgumentException> { deferred.await() }
            assertEquals("boom", ex.message)
        }
    }

    /** Test 14: Debounce – rapid emissions suppressed; only last before quiet window + later distinct emission survive. */
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    @Test
    fun debounce_emits_after_quiet_period() = runTest {
        val results = mutableListOf<Int>()
        val flow = flow {
            emit(1)
            delay(90) // within debounce window
            emit(2)
            delay(150) // quiet long enough
            emit(3)
        }.debounce(100)

        val job = launch { flow.collect { results += it } }

        advanceTimeBy(300) // enough for all emissions
        job.cancel()

        assertEquals(listOf(2, 3), results) // 1 suppressed, 2 and 3 emitted
    }

    /** Test 15: Sample – periodically emit latest element at fixed sampling interval. */
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    @Test
    fun sample_emits_latest_at_fixed_intervals() = runTest {
        val results = mutableListOf<Int>()
        val flow = flow {
            repeat(5) {
                emit(it)
                delay(50)
            }
        }.sample(100)

        val job = launch { flow.collect { results += it } }

        advanceTimeBy(300)
        job.cancel()

        // sample emits the latest value every 100ms window
        assertEquals(listOf(1, 3), results) // 0 skipped because first sample window ends at 100ms
    }

    /** Test 16: Timeout – collection cancelled when next emission exceeds timeout threshold. */
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    @Test
    fun timeout_cancels_on_slow_emission() = runTest {
        val flow = flow {
            emit("fast")
            delay(200.milliseconds)
            emit("too slow")
        }.timeout(100.milliseconds)

        val collectedValues = mutableListOf<String>()
        assertFailsWith<TimeoutCancellationException> {
            flow.collect { value ->
                collectedValues += value
            }
        }
        assertEquals(listOf("fast"), collectedValues)
    }

    /** Test 17: awaitAll – waits for all async children; return order matches awaitAll argument order. */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun awaitAll_waits_for_all_children() = runTest {
        val results = mutableListOf<Int>()
        val d1 = async { delay(100); 1 }
        val d2 = async { delay(200); 2 }
        val d3 = async { delay(50); 3 }

        advanceTimeBy(250)
        results += awaitAll(d1, d2, d3)

        assertEquals(listOf(1, 2, 3), results)
    }

    /** Test 18: Parent cancellation – cancelling parent job propagates cancellation to children (non-supervised). */
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun parent_cancellation_propagates_to_children() = runTest {
        var cancelled = false
        val job = launch {
            launch {
                try {
                    delay(1_000)
                } finally {
                    cancelled = true
                }
            }
        }

        advanceTimeBy(100)
        job.cancel() // cancel parent
        runCurrent() // process cancellation

        assertTrue(cancelled, "Child should be cancelled when parent is cancelled")
    }
}
