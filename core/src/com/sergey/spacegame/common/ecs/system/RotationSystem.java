package com.sergey.spacegame.common.ecs.system;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.sergey.spacegame.common.ecs.component.RotationComponent;
import com.sergey.spacegame.common.ecs.component.RotationVelocityComponent;

public class RotationSystem extends IteratingSystem {
	
	private static ComponentMapper<RotationComponent> rotMapper = ComponentMapper.getFor(RotationComponent.class);
	private static ComponentMapper<RotationVelocityComponent> rvlMapper = ComponentMapper.getFor(RotationVelocityComponent.class);

	public RotationSystem() {
		super(Family.all(RotationComponent.class, RotationVelocityComponent.class).get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		rotMapper.get(entity).r += rvlMapper.get(entity).vr*deltaTime;
	}
}
