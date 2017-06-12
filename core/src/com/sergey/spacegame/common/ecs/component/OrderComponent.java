package com.sergey.spacegame.common.ecs.component;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.sergey.spacegame.common.game.Level;
import com.sergey.spacegame.common.game.orders.IOrder;
import com.sergey.spacegame.common.util.ImmutableIterator;

public class OrderComponent implements Component, Iterable<IOrder> {
	public static final ComponentMapper<OrderComponent> MAPPER = ComponentMapper.getFor(OrderComponent.class);
	
	private List<IOrder> orders;
	
	public OrderComponent() {
		this.orders = new LinkedList<IOrder>();
	}

	public OrderComponent(IOrder... orders) {
		this();
		this.orders.addAll(Arrays.asList(orders));
	}
	
	public IOrder peek() {
		return orders.get(0);
	}
	
	public IOrder pop() {
		return orders.remove(0);
	}
	
	public void addOrder(IOrder order) {
		orders.add(order);
	}
	
	public void clearOrders(Entity e, Level level) {
		orders.forEach((o)->o.onCancel(e, level));
		orders.clear();
	}
	
	public int size() {
		return orders.size();
	}

	@Override
	public Iterator<IOrder> iterator() {
		return new ImmutableIterator<>(orders.iterator());
	}
}
