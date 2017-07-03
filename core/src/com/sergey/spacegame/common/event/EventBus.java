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

@SuppressWarnings("SuspiciousMethodCalls")
public class EventBus {

	private HashMap<Class, List<EventConsumer>> handles = new HashMap<>();
	private HashMap<Object, HashMap<Class, List<EventConsumer>>> handlers = new HashMap<>();

	private List<EventConsumer> getNewList() {
		return new ArrayList<>();
	}

	public void registerAnnotated(Object handler) {
		if (handlers.containsKey(handler)) {
			throw new IllegalStateException("This handler has already been registered.");
		}

		Method[] methods = handler.getClass().getMethods();
		HashMap<Class, List<EventConsumer>> thisHandler = new HashMap<>();

		for (Method method : methods) {
			EventHandle handleAnn = method.getAnnotation(EventHandle.class);
			if (handleAnn != null) {
				if (method.getParameterCount() != 1 || method.getParameters()[0].getType().isAssignableFrom(Event.class)) {
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
					MethodHandles.Lookup lookup = MethodHandles.lookup();
					MethodHandle methodHandle = lookup.unreflect(method);
					MethodType invokedType = MethodType.methodType(EventConsumer.class, handler.getClass());//, handler.getClass()
					MethodType samType = MethodType.methodType(void.class, Event.class);
					MethodType instantiatedMethodType = MethodType.methodType(void.class, parameter);
					EventConsumer lambda = (EventConsumer) LambdaMetafactory.metafactory(lookup, "accept", invokedType, samType, methodHandle, instantiatedMethodType).getTarget().invoke(handler);

					handles.get(parameter).add(lambda);
					thisHandler.get(parameter).add(lambda);
				} catch (Throwable throwable) {
					throwable.printStackTrace();
				}
			}
		}

		if (!thisHandler.isEmpty()) {
			if (handlers.containsKey(handler)) {
				handlers.get(handler).putAll(thisHandler);
			} else {
				handlers.put(handler, thisHandler);
			}
		}
	}

	public void registerSpecific(Object handler, Class<? extends Event> eventType, EventConsumer consumer) {
		if (!handles.containsKey(eventType)) {
			handles.put(eventType, getNewList());
		}

		handles.get(eventType).add(consumer);

		if (!handlers.containsKey(handler)) {
			handlers.put(handler, new HashMap<>());
		}

		if (!handlers.get(handler).containsKey(eventType)) {
			handlers.get(handler).put(eventType, getNewList());
		}

		handlers.get(handler).get(eventType).add(consumer);
	}

	public void unregisterAll(Object handler) {
		if (!handlers.containsKey(handler)) {
			throw new IllegalStateException("This handler has not been registered.");
		}

		HashMap<Class, List<EventConsumer>> registeredEvents = handlers.get(handler);

		for (Map.Entry<Class, List<EventConsumer>> classListEntry : registeredEvents.entrySet()) {
			List<EventConsumer> classHandles = handles.get(classListEntry.getKey());
			classHandles.removeAll(classListEntry.getValue());
			if (classHandles.isEmpty()) {
				handles.remove(classListEntry.getKey());
			}
		}

		handles.remove(handler);
	}

	public void unregisterSpecific(Object handler, Class<? extends Event> eventType, EventConsumer consumer) {
		if (handlers.containsKey(handler) && handlers.get(handler).containsKey(eventType) && handlers.get(handler).get(eventType).contains(consumer)) {
			handles.get(eventType).remove(consumer);
			if (handles.get(eventType).isEmpty()) {
				handles.remove(eventType);
			}
			handlers.get(handler).get(eventType).remove(consumer);
			if (handlers.get(handler).get(eventType).isEmpty()) {
				handlers.get(handler).remove(eventType);
			}
			if (handlers.get(handler).isEmpty()) {
				handlers.remove(handler);
			}
		}
	}

	public void post(Event e) {
		Class type = e.getClass();
		List<EventConsumer> handleList = handles.get(type);
		if (handleList == null) return;
		for (EventConsumer handle : handleList) {
			handle.accept(e);
		}
	}

	@FunctionalInterface
	public interface EventConsumer {
		void accept(Event e);
	}
}