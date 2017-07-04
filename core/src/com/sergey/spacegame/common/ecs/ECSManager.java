package com.sergey.spacegame.common.ecs;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.sergey.spacegame.common.ecs.component.IDComponent;

public final class ECSManager {
	private Engine engine;
	
	public ECSManager() {
		engine = new Engine();
	}

	public void update(float delta) {
		engine.update(delta);
	}

	public void addSystem(EntitySystem system) {
		engine.addSystem(system);
	}

	public void removeSystem(EntitySystem system) {
		engine.removeSystem(system);
	}
	
	public Entity newEntity() {
		Entity entity = new Entity();
		IDComponent comp = new IDComponent();
		entity.add(comp);
		IDComponent.entities.put(comp.id, entity);
		return entity;
	}

	public ImmutableArray<Entity> getEntities() {
		return engine.getEntities();
	}

	public ImmutableArray<Entity> getEntitiesFor(Family family) {
		return engine.getEntitiesFor(family);
	}

	public void addEntity(Entity entity) {
		engine.addEntity(entity);
	}

	public void removeEntity(Entity entity) {
		engine.removeEntity(entity);
	}
}
