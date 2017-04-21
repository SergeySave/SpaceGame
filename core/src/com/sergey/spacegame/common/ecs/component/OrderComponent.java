package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.sergey.spacegame.common.orders.IOrder;

public class OrderComponent implements Component {
	public static final ComponentMapper<OrderComponent> MAPPER = ComponentMapper.getFor(OrderComponent.class);
	
	public IOrder order;
	
	public OrderComponent() {}

	public OrderComponent(IOrder order) {
		this.order = order;
	}
}
