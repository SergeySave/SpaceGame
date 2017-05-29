package com.sergey.spacegame.common.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.VelocityComponent;

public class MovementSystem extends IteratingSystem {

	public MovementSystem() {
		super(Family.all(PositionComponent.class, VelocityComponent.class).get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		PositionComponent pos = PositionComponent.MAPPER.get(entity);
		VelocityComponent vel = VelocityComponent.MAPPER.get(entity);
		pos.x += vel.vx*deltaTime;
		pos.y += vel.vy*deltaTime;
	}
}