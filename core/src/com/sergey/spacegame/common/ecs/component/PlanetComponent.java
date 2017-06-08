package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class PlanetComponent implements ClonableComponent {
	public static final ComponentMapper<PlanetComponent> MAPPER = ComponentMapper.getFor(PlanetComponent.class);

	@Override
	public Component copy() {
		return new PlanetComponent();
	}
}
