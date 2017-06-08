package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class RotationComponent implements ClonableComponent {
	public static final ComponentMapper<RotationComponent> MAPPER = ComponentMapper.getFor(RotationComponent.class);
	
	public float r;
	public float originX;
	public float originY;
	
	public RotationComponent() {}
	
	public RotationComponent(float r, float originX, float originY) {
		this.r = r;
		this.originX = originX;
		this.originY = originY;
	}

	@Override
	public Component copy() {
		return new RotationComponent(r, originX, originY);
	}
}
