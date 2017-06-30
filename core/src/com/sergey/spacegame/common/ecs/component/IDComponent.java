package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class IDComponent implements Component {
	public static final ComponentMapper<IDComponent> MAPPER = ComponentMapper.getFor(IDComponent.class);
	
	public static final AtomicInteger COUNTER = new AtomicInteger();
	public static final HashMap<Integer, Entity> entities = new HashMap<>();
	
	public final int id;
	
	public IDComponent() {
		id = COUNTER.getAndIncrement();
	}
}
