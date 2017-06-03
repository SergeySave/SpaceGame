package com.sergey.spacegame.common.util;

/**
 * 
 * Like java.util.Observer, but using generics
 * 
 * @author sergeys
 * @see     java.util.Observer
 * @see     com.serget.spacegame.common.util.Observable<T>
 * @param <T> the type of the object being observed
 */
public interface Observer<T> {
	
    /**
     * This method is called whenever the observed object is changed. An
     * application calls an <tt>Observable</tt> object's
     * <code>notifyObservers</code> method to have all the object's
     * observers notified of the change.
     *
     * @param   o     the observable object.
     * @param   arg   an argument passed to the <code>notifyObservers</code>
     *                 method.
     */
    void update(Observable<T> observable, T object);
}
