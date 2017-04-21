package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class ControllableComponent implements Component {
	public static final ComponentMapper<ControllableComponent> MAPPER = ComponentMapper.getFor(ControllableComponent.class);
}
