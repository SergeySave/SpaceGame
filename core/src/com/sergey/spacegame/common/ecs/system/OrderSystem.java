package com.sergey.spacegame.common.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.sergey.spacegame.common.ecs.component.OrderComponent;

public class OrderSystem extends IteratingSystem {

	public OrderSystem() {
		super(Family.all(OrderComponent.class).get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		OrderComponent order = OrderComponent.MAPPER.get(entity);
		order.order.update(entity, deltaTime);
		if (order.order.completed()) {
			entity.remove(OrderComponent.class);
		}
	}
}
