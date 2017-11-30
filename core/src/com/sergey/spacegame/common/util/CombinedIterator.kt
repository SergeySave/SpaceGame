package com.sergey.spacegame.common.util

/**
 * An iterator that acts as a combination of other iterators
 *
 * @param E - the type of object returned by this iterator
 *
 * @author sergeys
 *
 * @constructor Creates a new CombinedIterator from a set of iterators
 *
 * @param iterators - an array of iterators
 */
class CombinedIterator<out E>(vararg iterators: Iterator<E>) : Iterator<E> {
    
    private var iters: Array<out Iterator<E>> = iterators
    private var index = 0
    
    override fun hasNext(): Boolean {
        while (true) {
            if (index >= iters.size) return false
            if (iters[index].hasNext()) return true
            ++index
        }
    }
    
    override fun next(): E {
        return iters[index].next()
    }
}
