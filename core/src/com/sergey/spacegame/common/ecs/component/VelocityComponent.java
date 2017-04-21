package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class VelocityComponent implements Component {
	public static final ComponentMapper<VelocityComponent> MAPPER = ComponentMapper.getFor(VelocityComponent.class);
	
	public float vx;
	public float vy;
	
	public VelocityComponent() {}

	public VelocityComponent(float vx, float vy) {
		this.vx = vx;
		this.vy = vy;
	}
}
