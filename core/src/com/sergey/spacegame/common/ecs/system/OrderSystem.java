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
		
		if (order.orders.size() == 0) {
			entity.remove(OrderComponent.class);
			return;
		}
		
		while (order.orders.size()>0 && !order.orders.get(0).isValidFor(entity)) {
			order.orders.remove(0);
		}
		
		if (order.orders.size() == 0) {
			entity.remove(OrderComponent.class);
			return;
		}
		
		order.orders.get(0).update(entity, deltaTime);
		if (order.orders.get(0).completed()) {
			order.orders.remove(0);
		}
		if (order.orders.size() == 0) {
			entity.remove(OrderComponent.class);
		}
	}
}
