package com.sergey.spacegame.common.util;

/**
 * Like java.util.Observer, but using generics
 *
 * @param <T> the type of the object being observed
 *
 * @author sergeys
 * @see java.util.Observer
 * @see com.sergey.spacegame.common.util.Observable<T>
 */
public interface Observer<T> {
    
    /**
     * This method is called whenever the observed object is changed. An
     * application calls an <tt>Observable</tt> object's
     * <code>notifyObservers</code> method to have all the object's
     * observers notified of the change.
     *
     * @param observable the observable object.
     * @param object     an argument passed to the <code>notifyObservers</code>
     *                   method.
     */
    void update(Observable<T> observable, T object);
}
