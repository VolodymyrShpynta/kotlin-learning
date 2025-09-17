package examples.receivers

import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

inline fun <T, R> T.run(block: T.() -> R): R {
    return block() // the same as 'this.block()' and 'block(this)'
}

inline fun <T, R> with(receiver: T, block: T.() -> R): R {
    return receiver.block() // the same as 'block(receiver)'
}

inline fun <T> T.apply(block: T.() -> Unit): T {
    block() // the same as 'this.block()'
    return this
}

inline fun buildString(block: StringBuilder.() -> Unit): String = StringBuilder().apply(block).toString()


inline fun <T> buildList(block: MutableList<T>.() -> Unit): List<T> {
    return mutableListOf<T>().apply { block() }.toList()
}

inline fun <T> buildSet(block: MutableSet<T>.() -> Unit): Set<T> {
    return mutableSetOf<T>().apply (block).toSet()
}

inline fun <K, V> buildMap(block: MutableMap<K, V>.() -> Unit): Map<K, V> {
    return mutableMapOf<K, V>().apply { block() }.toMap()
}

inline fun <R, T> useResource(resource: R, block: R.() -> T) {
    resource.block()
}

inline fun <L : Lock, R> withLock(lock: L, block: L.() -> R): R {
    lock.lock()
    try {
        return lock.block()
    } finally {
        lock.unlock()
    }
}

inline fun <L : Lock, R> L.run(block: L.() -> R): R {
    withLock(this) {
        return this.block()
    }
}

fun main() {
    val list = mutableListOf(1, 2, 3)

    list.run {
        add(4)
        add(5)
        remove(1)
        println(this)
    }

    with(list) {
        add(6)
        add(7)
        remove(2)
        println(this)
    }

    list.apply {
        add(8)
        add(9)
        remove(3)
    }.apply { println(this) }

    println(buildString { append("Hello"); append(" Kotlin") })

    println(buildList {
        add(1)
        add(2)
        add(3)
    })

    println(buildSet { add(11); add(22); add(33) })

    println(buildMap {
        put("one", 1)
        put("two", 2)
        put("three", 3)
    })

    useResource(mutableListOf("A", "B", "C")) {
        add("D")
        remove("A")
        println(this)
    }

    val lock = ReentrantLock()
    withLock(lock) {
        println("Lock is held: $isHeldByCurrentThread")
    }
    println("Lock is held outside: ${lock.isHeldByCurrentThread}") // should be false

    lock.run {
        println("Lock is held in run: $isHeldByCurrentThread")
    }
    println("Lock is held outside: ${lock.isHeldByCurrentThread}") // should be false
}
