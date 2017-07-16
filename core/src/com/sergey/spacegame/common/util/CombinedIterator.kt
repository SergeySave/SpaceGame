package com.sergey.spacegame.common.util

class CombinedIterator<E>(vararg iterators: Iterator<E>) : Iterator<E> {
    
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
