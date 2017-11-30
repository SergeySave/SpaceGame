package com.sergey.spacegame.common.util;

import java.util.Iterator;

/**
 * An Iterator wrapping class that represents an iterator that is immutable
 *
 * @param <E> the type of object that the iterator returns
 *
 * @author sergeys
 */
public class ImmutableIterator<E> implements Iterator<E> {
    
    private Iterator<E> backing;
    
    public ImmutableIterator(Iterator<E> iter) {
        backing = iter;
    }
    
    @Override
    public boolean hasNext() {
        return backing.hasNext();
    }
    
    @Override
    public E next() {
        return backing.next();
    }
}
