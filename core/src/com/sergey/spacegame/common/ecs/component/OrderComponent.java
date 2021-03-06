package com.sergey.spacegame.common.ecs.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.sergey.spacegame.common.SpaceGame;
import com.sergey.spacegame.common.ecs.system.OrderSystem;
import com.sergey.spacegame.common.event.OrderInitializedEvent;
import com.sergey.spacegame.common.game.Level;
import com.sergey.spacegame.common.game.orders.IOrder;
import com.sergey.spacegame.common.util.ImmutableIterator;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This component represents the orders that an entity has
 *
 * @author sergeys
 */
public class OrderComponent implements Component, Iterable<IOrder> {
    
    public static final  ComponentMapper<OrderComponent> MAPPER                = ComponentMapper.getFor(OrderComponent.class);
    private static final OrderInitializedEvent.Builder   orderInitializedEvent = new OrderInitializedEvent.Builder();
    
    private List<IOrder> orders;
    private List<IOrder> needInitialization;
    
    /**
     * Create a new OrderComponent with a list of orders
     *
     * @param orders - a list of orders to start with
     */
    public OrderComponent(IOrder... orders) {
        this();
        this.orders.addAll(Arrays.asList(orders));
        this.needInitialization.addAll(this.orders);
    }
    
    /**
     * Create an empty OrderComponent
     */
    public OrderComponent() {
        this.orders = new LinkedList<>();
        this.needInitialization = new LinkedList<>();
    }
    
    /**
     * Get the first order in the queue
     *
     * @return the first order
     */
    public IOrder peek() {
        return orders.get(0);
    }
    
    /**
     * Remove the first order from the queue
     *
     * @return the first order
     */
    public IOrder pop() {
        IOrder order = orders.remove(0);
        needInitialization.remove(order);
        return order;
    }
    
    /**
     * Add an order to the queue
     *
     * @param order - the order to add
     */
    public void addOrder(IOrder order) {
        orders.add(order);
        needInitialization.add(order);
    }
    
    /**
     * Clear the orders in the queue
     *
     * @param e     - the entity to clear them for
     * @param level - the level that the entity is in
     */
    public void clearOrders(Entity e, Level level) {
        orders.forEach((o) -> o.onCancel(e, level));
        orders.clear();
        needInitialization.clear();
    }
    
    /**
     * Get the amount of orders in the queue
     *
     * @return the size of the order queue
     */
    public int size() {
        return orders.size();
    }
    
    /**
     * Initialize all the orders that are in the queue
     *
     * @param e           - the entity that the orders are being initialized for
     * @param level       - the level that the entity is in
     * @param orderSystem - the OrderSystem that is managing the orders
     *
     * @see com.sergey.spacegame.common.ecs.system.OrderSystem
     */
    public void initAll(Entity e, Level level, OrderSystem orderSystem) {
        if (!needInitialization.isEmpty()) {
            needInitialization.forEach((o) -> {
                o.init(e, level, orderSystem);
                SpaceGame.getInstance().getEventBus().post(orderInitializedEvent.get(o));
            });
            needInitialization.clear();
        }
    }
    
    @NotNull
    @Override
    public Iterator<IOrder> iterator() {
        return new ImmutableIterator<>(orders.iterator());
    }
}
