package com.sergey.spacegame.common.ecs;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.sergey.spacegame.common.ecs.component.IDComponent;

public class ECSManager {
	private Engine engine;
	
	public ECSManager() {
		engine = new Engine();
	}
	
	public Engine getEngine() {
		return engine;
	}
	
	public Entity newEntity() {
		Entity entity = new Entity();
		IDComponent comp = new IDComponent();
		entity.add(comp);
		IDComponent.entities.put(comp.id, entity);
		return entity;
	}
}
