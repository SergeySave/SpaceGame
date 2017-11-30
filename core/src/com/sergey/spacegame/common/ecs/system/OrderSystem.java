package com.sergey.spacegame.common.ecs.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.sergey.spacegame.common.ecs.component.OrderComponent;
import com.sergey.spacegame.common.game.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * This system is in charge of running orders
 *
 * @author sergeys
 */
public final class OrderSystem extends EntitySystem {
    
    
    private Level                  level;
    private ImmutableArray<Entity> entities;
    private List<Entity>           newlyConstructingBuildings;
    
    /**
     * Create a new OrderSystem
     *
     * @param level - the level on which the order system operates
     */
    public OrderSystem(Level level) {
        this.level = level;
        newlyConstructingBuildings = new ArrayList<>();
    }
    
    
    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(OrderComponent.class).get());
    }
    
    @Override
    public void removedFromEngine(Engine engine) {
        entities = null;
    }
    
    @Override
    public void update(float deltaTime) {
        for (int i = 0; i < entities.size(); ++i) {
            processEntity(entities.get(i), deltaTime);
        }
        newlyConstructingBuildings.clear();
    }
    
    private void processEntity(Entity entity, float deltaTime) {
        OrderComponent order = OrderComponent.MAPPER.get(entity);
        
        if (order.size() == 0) {
            entity.remove(OrderComponent.class);
            return;
        }
        
        order.initAll(entity, level, this);
        
        while (order.size() > 0 && !order.peek().isValidFor(entity)) {
            order.pop();
        }
        
        if (order.size() == 0) {
            entity.remove(OrderComponent.class);
            return;
        }
        
        order.peek().update(entity, deltaTime, level);
        if (order.peek().completed(entity)) {
            order.pop();
        }
        if (order.size() == 0) {
            entity.remove(OrderComponent.class);
        }
    }
    
    /**
     * Register a newly in construction entity
     *
     * @param e - the new entity
     */
    public void registerNewInConstruction(Entity e) {
        newlyConstructingBuildings.add(e);
    }
    
    /**
     * Get all currently under construction buildings
     *
     * @return a list of all entities currently under construction
     */
    public List<Entity> getConstructingBuildings() {
        return newlyConstructingBuildings;
    }
}
