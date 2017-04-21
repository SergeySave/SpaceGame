package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class RotationVelocityComponent implements Component {
	public static final ComponentMapper<RotationVelocityComponent> MAPPER = ComponentMapper.getFor(RotationVelocityComponent.class);
	
	public float vr;
	
	public RotationVelocityComponent() {}

	public RotationVelocityComponent(float vr) {
		this.vr = vr;
	}
}
