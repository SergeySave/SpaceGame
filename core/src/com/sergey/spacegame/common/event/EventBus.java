package com.sergey.spacegame.common.event;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class acts as an advanced event bus that converts reflection methods into lambdas for efficient event dispatch
 *
 * @author sergeys
 */
@SuppressWarnings("SuspiciousMethodCalls")
public class EventBus {
    
    private HashMap<Class, List<EventHandler>>                  handles  = new HashMap<>();
    private HashMap<Object, HashMap<Class, List<EventHandler>>> handlers = new HashMap<>();
    
    /**
     * Register the annotated methods in a given event handler object
     *
     * @param handler - the handler object to search for handler methods
     *
     * @see com.sergey.spacegame.common.event.EventHandle
     */
    public void registerAnnotated(Object handler) {
        registerAnnotated(handler, handler);
    }
    
    /**
     * Register the annotated methods in a given event handler object using a different object as the handler owner
     *
     * @param registeredHandler - the object to search for handler methods
     * @param eventHandler      - the object to act as the handler owner
     *
     * @see com.sergey.spacegame.common.event.EventHandle
     */
    public void registerAnnotated(Object registeredHandler, Object eventHandler) {
        if (handlers.containsKey(eventHandler)) {
            throw new IllegalStateException("This handler has already been registered.");
        }
        
        Method[]                           methods     = eventHandler.getClass().getMethods();
        HashMap<Class, List<EventHandler>> thisHandler = new HashMap<>();
        
        for (Method method : methods) {
            EventHandle handleAnn = method.getAnnotation(EventHandle.class);
            if (handleAnn != null) {
                //Find methods with @EventHandle annotations
                
                //Make sure the parameter type is correct
                if (method.getParameterCount() != 1 ||
                    method.getParameters()[0].getType().isAssignableFrom(Event.class)) {
                    throw new IllegalStateException("Methods annotated with @EventHandle must only have 1 parameter that extends Event.");
                }
                
                //parameter extends Event.class
                Class parameter = method.getParameters()[0].getType();
                
                if (!handles.containsKey(parameter)) {
                    handles.put(parameter, getNewList());
                }
                if (!thisHandler.containsKey(parameter)) {
                    thisHandler.put(parameter, getNewList());
                }
                
                try {
                    //Construct the lambda
                    MethodHandles.Lookup lookup       = MethodHandles.lookup();
                    MethodHandle         methodHandle = lookup.unreflect(method);
                    MethodType invokedType = MethodType.methodType(EventHandler.class, eventHandler
                            .getClass());//, handler.getClass()
                    MethodType samType                = MethodType.methodType(void.class, Event.class);
                    MethodType instantiatedMethodType = MethodType.methodType(void.class, parameter);
                    EventHandler lambda = (EventHandler) LambdaMetafactory.metafactory(lookup, "accept", invokedType, samType, methodHandle, instantiatedMethodType)
                            .getTarget()
                            .invoke(eventHandler);
                    
                    //Add it to the handles
                    handles.get(parameter).add(lambda);
                    thisHandler.get(parameter).add(lambda);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
        
        //Register the handler owner
        if (!thisHandler.isEmpty()) {
            if (handlers.containsKey(registeredHandler)) {
                handlers.get(registeredHandler).putAll(thisHandler);
            } else {
                handlers.put(registeredHandler, thisHandler);
            }
        }
    }
    
    private List<EventHandler> getNewList() {
        return new ArrayList<>();
    }
    
    /**
     * Register an event handler with a given event type and a given owner
     *
     * @param owner     - the owner object of the event handler
     * @param eventType - the type of event to listen for
     * @param handler   - the event handler
     */
    public void registerSpecific(Object owner, Class<? extends Event> eventType, EventHandler handler) {
        if (!handles.containsKey(eventType)) {
            handles.put(eventType, getNewList());
        }
        
        handles.get(eventType).add(handler);
        
        if (!handlers.containsKey(owner)) {
            handlers.put(owner, new HashMap<>());
        }
        
        if (!handlers.get(owner).containsKey(eventType)) {
            handlers.get(owner).put(eventType, getNewList());
        }
        
        handlers.get(owner).get(eventType).add(handler);
    }
    
    /**
     * Unregister all event handlers for a given owner
     *
     * @param owner - the owner to unregister event handlers for
     */
    public void unregisterAll(Object owner) {
        if (!handlers.containsKey(owner)) {
            throw new IllegalStateException("This handler has not been registered.");
        }
        
        HashMap<Class, List<EventHandler>> registeredEvents = handlers.get(owner);
        
        for (Map.Entry<Class, List<EventHandler>> classListEntry : registeredEvents.entrySet()) {
            List<EventHandler> classHandles = handles.get(classListEntry.getKey());
            classHandles.removeAll(classListEntry.getValue());
            if (classHandles.isEmpty()) {
                handles.remove(classListEntry.getKey());
            }
        }
        
        handles.remove(owner);
    }
    
    /**
     * Unregister a specific event handler
     *
     * @param owner     - the owner of the event handler
     * @param eventType - the type of event it is handling
     * @param handler   - the event handler
     */
    public void unregisterSpecific(Object owner, Class<? extends Event> eventType, EventHandler handler) {
        if (handlers.containsKey(owner) && handlers.get(owner).containsKey(eventType) &&
            handlers.get(owner).get(eventType).contains(handler)) {
            handles.get(eventType).remove(handler);
            if (handles.get(eventType).isEmpty()) {
                handles.remove(eventType);
            }
            handlers.get(owner).get(eventType).remove(handler);
            if (handlers.get(owner).get(eventType).isEmpty()) {
                handlers.get(owner).remove(eventType);
            }
            if (handlers.get(owner).isEmpty()) {
                handlers.remove(owner);
            }
        }
    }
    
    /**
     * Post an event to this event bus
     *
     * @param e - the event to post
     */
    public void post(Event e) {
        Class              type       = e.getClass();
        List<EventHandler> handleList = handles.get(type);
        if (handleList == null) return;
        for (EventHandler handle : handleList) {
            handle.accept(e);
        }
    }
    
    /**
     * Represents an event handler
     *
     * @author sergeys
     */
    @FunctionalInterface
    public interface EventHandler {
        
        /**
         * Handle an event
         *
         * @param e - the event to handle
         */
        void accept(Event e);
    }
}