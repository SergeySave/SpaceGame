package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class RotationComponent implements Component {
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
}
