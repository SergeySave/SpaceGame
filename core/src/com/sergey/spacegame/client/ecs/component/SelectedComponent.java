package com.sergey.spacegame.client.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class SelectedComponent implements Component {
	public static final ComponentMapper<SelectedComponent> MAPPER = ComponentMapper.getFor(SelectedComponent.class);
}
