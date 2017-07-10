package com.sergey.spacegame.common.game.orders;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.sergey.spacegame.common.ecs.component.BuildingComponent;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.system.OrderSystem;
import com.sergey.spacegame.common.game.Level;

public class BuildShipOrder implements IOrder {
    
    private String entity;
    private float  time;
    private double price;
    
    public BuildShipOrder(String entityName, float time, double price) {
        this.entity = entityName;
        this.time = time;
        this.price = price;
    }
    
    @Override
    public void init(Entity e, Level level, OrderSystem orderSystem) {
        level.setMoney(level.getMoney() - price);
    }
    
    @Override
    public void update(Entity e, float deltaTime, Level level) {
        if (time < 0) return;
        
        time -= deltaTime;
        
        if (time < 0) {
            Entity newEntity = level.getEntities().get(entity).createEntity(level);
            if (PositionComponent.MAPPER.has(newEntity) && PositionComponent.MAPPER.has(e)) {
                PositionComponent pos  = PositionComponent.MAPPER.get(newEntity);
                PositionComponent curr = PositionComponent.MAPPER.get(e);
    
                if (BuildingComponent.MAPPER.has(e)) {
                    Vector2 planetPosition = PositionComponent.MAPPER.get(BuildingComponent.MAPPER.get(e)
                                                                                  .getPlanet()).createVector();
        
                    pos.setFrom(curr.createVector()
                                        .sub(planetPosition)
                                        .scl(1.5f)
                                        .add(planetPosition)
                                        .add(20f * e.hashCode() / 2147483647f, 20f * pos.hashCode() / 2147483647f));
                } else {
                    pos.x = curr.x + 20f * newEntity.hashCode() / 2147483647f;
                    pos.y = curr.y + 20f * pos.hashCode() / 2147483647f;
                }
            }
            
            level.getECS().addEntity(newEntity);
        }
    }
    
    @Override
    public void onCancel(Entity e, Level level) {
        level.setMoney(level.getMoney() + price);
    }
    
    @Override
    public boolean isValidFor(Entity e) {
        return true;
    }
    
    @Override
    public boolean completed() {
        return time < 0;
    }
}
