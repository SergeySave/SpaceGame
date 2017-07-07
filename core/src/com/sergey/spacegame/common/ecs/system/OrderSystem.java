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

public class OrderSystem extends EntitySystem {
    
    
    private Level                  level;
    private ImmutableArray<Entity> entities;
    private List<Entity>           newlyConstructingBuildings;
    
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
    
    protected void processEntity(Entity entity, float deltaTime) {
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
        if (order.peek().completed()) {
            order.pop();
        }
        if (order.size() == 0) {
            entity.remove(OrderComponent.class);
        }
    }
    
    public void registerNewInConstruction(Entity e) {
        newlyConstructingBuildings.add(e);
    }
    
    public List<Entity> getConstructingBuildings() {
        return newlyConstructingBuildings;
    }
}
