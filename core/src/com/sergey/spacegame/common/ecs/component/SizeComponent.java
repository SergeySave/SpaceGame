package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class SizeComponent implements Component {
	public static final ComponentMapper<SizeComponent> MAPPER = ComponentMapper.getFor(SizeComponent.class);
	
	public float w;
	public float h;
	
	public SizeComponent() {}

	public SizeComponent(float w, float h) {
		this.w = w;
		this.h = h;
	}
}