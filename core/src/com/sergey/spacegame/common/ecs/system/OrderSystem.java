package com.sergey.spacegame.common.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.sergey.spacegame.common.ecs.component.OrderComponent;
import com.sergey.spacegame.common.game.Level;

public class OrderSystem extends IteratingSystem {
	
	private Level level;

	public OrderSystem(Level level) {
		super(Family.all(OrderComponent.class).get());
		this.level = level;
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		OrderComponent order = OrderComponent.MAPPER.get(entity);
		
		if (order.size() == 0) {
			entity.remove(OrderComponent.class);
			return;
		}
		
		while (order.size()>0 && !order.peek().isValidFor(entity)) {
			order.pop();
		}
		
		if (order.size() == 0) {
			entity.remove(OrderComponent.class);
			return;
		}
		
		order.peek().update(entity, deltaTime, level);
		if (order.peek().completed()) {
			order.pop();
		}
		if (order.size() == 0) {
			entity.remove(OrderComponent.class);
		}
	}
}
