package com.playground.kotlin.oop.core

/**
 * AuditedList demonstrating:
 * - Class delegation (by keyword)
 * - Selective method overriding
 * - Wrapper pattern
 */
class AuditedList<T>(private val innerList: MutableList<T> = ArrayList()) : MutableList<T> by innerList {

    private fun log(action: String) = println("AuditedList modification: $action")

    override fun add(element: T): Boolean {
        log("add($element)")
        return innerList.add(element)
    }

    override fun remove(element: T): Boolean {
        log("remove($element)")
        return innerList.remove(element)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        log("addAll($elements)")
        return innerList.addAll(elements)
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        log("removeAll($elements)")
        return innerList.removeAll(elements)
    }

    override fun clear() {
        log("clear()")
        innerList.clear()
    }

    override fun set(index: Int, element: T): T {
        log("set($index, $element)")
        return innerList.set(index, element)
    }

    override fun add(index: Int, element: T) {
        log("add($index, $element)")
        innerList.add(index, element)
    }

    override fun removeAt(index: Int): T {
        log("removeAt($index)")
        return innerList.removeAt(index)
    }
}
