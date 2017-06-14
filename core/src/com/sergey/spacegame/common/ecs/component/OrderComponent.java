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
	private List<IOrder> needInitialization;

	public OrderComponent() {
		this.orders = new LinkedList<IOrder>();
		this.needInitialization = new LinkedList<>();
	}

	public OrderComponent(IOrder... orders) {
		this();
		this.orders.addAll(Arrays.asList(orders));
		this.needInitialization.addAll(this.orders);
	}

	public IOrder peek() {
		return orders.get(0);
	}

	public IOrder pop() {
		IOrder order = orders.remove(0);
		needInitialization.remove(order);
		return order;
	}

	public void addOrder(IOrder order) {
		orders.add(order);
		needInitialization.add(order);
	}

	public void clearOrders(Entity e, Level level) {
		orders.forEach((o)->o.onCancel(e, level));
		orders.clear();
		needInitialization.clear();
	}

	public int size() {
		return orders.size();
	}

	public void initAll(Entity e, Level level) {
		if (!needInitialization.isEmpty()) {
			needInitialization.forEach((o)->o.init(e, level));
			needInitialization.clear();
		}
	}

	@Override
	public Iterator<IOrder> iterator() {
		return new ImmutableIterator<>(orders.iterator());
	}
}
