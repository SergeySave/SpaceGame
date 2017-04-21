package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;

public class RotationVelocityComponent implements Component {
	public float vr;
	
	public RotationVelocityComponent() {}

	public RotationVelocityComponent(float vr) {
		this.vr = vr;
	}
}
