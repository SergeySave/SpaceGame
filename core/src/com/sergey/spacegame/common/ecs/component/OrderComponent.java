package com.sergey.spacegame.common.ecs.component;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.sergey.spacegame.common.orders.IOrder;

public class OrderComponent implements Component {
	public static final ComponentMapper<OrderComponent> MAPPER = ComponentMapper.getFor(OrderComponent.class);
	
	public List<IOrder> orders;
	
	public OrderComponent() {
		this.orders = new LinkedList<IOrder>();
	}

	public OrderComponent(IOrder... orders) {
		this();
		this.orders.addAll(Arrays.asList(orders));
	}
}
