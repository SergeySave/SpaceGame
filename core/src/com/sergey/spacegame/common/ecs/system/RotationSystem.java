package com.sergey.spacegame.common.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.sergey.spacegame.common.ecs.component.RotationComponent;
import com.sergey.spacegame.common.ecs.component.RotationVelocityComponent;

public class RotationSystem extends IteratingSystem {

	public RotationSystem() {
		super(Family.all(RotationComponent.class, RotationVelocityComponent.class).get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		RotationComponent.MAPPER.get(entity).r += RotationVelocityComponent.MAPPER.get(entity).vr*deltaTime;
	}
}
