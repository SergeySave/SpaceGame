package com.sergey.spacegame.common.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Like java.util.Observable, but using generics
 *
 * @param <T> the type of the object being observed
 *
 * @author sergeys
 * @see java.util.Observable
 * @see com.sergey.spacegame.common.util.Observer<T>
 */
public class Observable<T> {
    
    private List<Observer<T>> observers;
    
    public Observable(int expectedObservers) {
        observers = new ArrayList<>(expectedObservers);
    }
    
    public void addObserver(Observer<T> observer) {
        if (!observers.contains(observer)) observers.add(observer);
    }
    
    public void removeObserver(Observer<T> observer) {
        observers.remove(observer);
    }
    
    public void notifyObservers(T obj) {
        for (Observer<T> obs : observers) {
            obs.update(this, obj);
        }
    }
}
