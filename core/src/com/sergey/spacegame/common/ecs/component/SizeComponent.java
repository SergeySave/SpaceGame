package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;

public class SizeComponent implements Component {
	public float w;
	public float h;
	
	public SizeComponent() {}

	public SizeComponent(float w, float h) {
		this.w = w;
		this.h = h;
	}
}
