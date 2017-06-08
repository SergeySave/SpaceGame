package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class ShipComponent implements ClonableComponent {
	public static final ComponentMapper<ShipComponent> MAPPER = ComponentMapper.getFor(ShipComponent.class);
	
	public float moveSpeed;
	public float rotateSpeed;
	
	@Override
	public Component copy() {
		ShipComponent ship = new ShipComponent();
		ship.moveSpeed = moveSpeed;
		ship.rotateSpeed = rotateSpeed;
		return ship;
	}
}
