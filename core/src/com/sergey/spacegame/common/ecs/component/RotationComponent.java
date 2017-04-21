package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;

public class RotationComponent implements Component {
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
