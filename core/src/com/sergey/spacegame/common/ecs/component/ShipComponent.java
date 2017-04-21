package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class ShipComponent implements Component {
	public static final ComponentMapper<ShipComponent> MAPPER = ComponentMapper.getFor(ShipComponent.class);
	
	public float moveSpeed;
}
