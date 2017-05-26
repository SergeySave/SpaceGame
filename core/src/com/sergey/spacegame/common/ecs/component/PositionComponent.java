package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;

public class PositionComponent implements Component {
	public static final ComponentMapper<PositionComponent> MAPPER = ComponentMapper.getFor(PositionComponent.class);
	
	public float x;
	public float y;
	
	public PositionComponent() {}

	public PositionComponent(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector2 createVector() {
		return new Vector2(x, y);
	}
}
