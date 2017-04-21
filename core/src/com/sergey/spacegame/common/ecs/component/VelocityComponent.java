package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;

public class VelocityComponent implements Component {
	public float vx;
	public float vy;
	
	public VelocityComponent() {}

	public VelocityComponent(float vx, float vy) {
		this.vx = vx;
		this.vy = vy;
	}
}
