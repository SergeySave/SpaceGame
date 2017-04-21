package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;

public class PositionComponent implements Component {
	public float x;
	public float y;
	
	public PositionComponent() {}

	public PositionComponent(float x, float y) {
		this.x = x;
		this.y = y;
	}
}
