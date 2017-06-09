package com.sergey.spacegame.common.game.orders;

import com.badlogic.ashley.core.Entity;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.game.Level;

public class BuildShipOrder implements IOrder {
	private String entity;
	private float time;
	
	public BuildShipOrder(String entityName, float time) {
		this.entity = entityName;
		this.time = time;
	}

	@Override
	public void update(Entity e, float deltaTime, Level level) {
		if (time < 0) return;
		
		time -= deltaTime;
		
		if (time < 0) {
			Entity newEntity = level.getEntities().get(entity).createEntity(level);
			if (PositionComponent.MAPPER.has(newEntity) && PositionComponent.MAPPER.has(e)) {
				PositionComponent pos = PositionComponent.MAPPER.get(newEntity);
				PositionComponent curr = PositionComponent.MAPPER.get(e);
				pos.x = curr.x;
				pos.y = curr.y;
			}
			
			level.getECS().getEngine().addEntity(newEntity);
		}
	}

	@Override
	public boolean isValidFor(Entity e) {
		return true;
	}

	@Override
	public boolean completed() {
		return time < 0;
	}
}
