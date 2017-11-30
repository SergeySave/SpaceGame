package com.sergey.spacegame.common.game.orders;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.sergey.spacegame.common.ecs.component.BuildingComponent;
import com.sergey.spacegame.common.ecs.component.ClonableComponent;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.Team1Component;
import com.sergey.spacegame.common.ecs.system.OrderSystem;
import com.sergey.spacegame.common.game.Level;
import com.sergey.spacegame.common.game.Player;

/**
 * Represents an order to build a ship
 *
 * @author sergeys
 */
public class BuildShipOrder implements IOrder {
    
    private String entity;
    private String tag;
    private float  time;
    private float  totalTime;
    private double price;
    private Player player;
    
    /**
     * Create a new BuildShipOrder
     *
     * @param entityName - the ship entity that should be built
     * @param time       - the time that building the entity should take
     * @param price      - the price of building this ship
     */
    public BuildShipOrder(String entityName, float time, double price) {
        this.entity = entityName;
        this.tag = "BuildShipOrder:" + entityName;
        this.time = 0;
        this.totalTime = time;
        this.price = price;
    }
    
    @Override
    public void init(Entity e, Level level, OrderSystem orderSystem) {
        ClonableComponent team = level.getEntities().get(entity).getTeam();
        if (team != null) {
            if (team == Team1Component.INSTANCE) {
                player = level.getPlayer1();
            } else {
                player = level.getPlayer2();
            }
            player.setMoney(player.getMoney() - price);
        }
    }
    
    @Override
    public void update(Entity e, float deltaTime, Level level) {
        if (time >= totalTime) return;
        
        time += deltaTime;
        
        if (time >= totalTime) {
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
                                        .add(20f * newEntity.hashCode() / 2147483647f,
                                             20f * pos.hashCode() / 2147483647f));
                } else {
                    pos.setX(curr.getX() + 20f * newEntity.hashCode() / 2147483647f);
                    pos.setY(curr.getY() + 20f * pos.hashCode() / 2147483647f);
                }
            }
            
            level.getECS().addEntity(newEntity);
        }
    }
    
    @Override
    public void onCancel(Entity e, Level level) {
        if (player != null) {
            player.setMoney(player.getMoney() + price);
        }
    }
    
    @Override
    public boolean isValidFor(Entity e) {
        return true;
    }
    
    @Override
    public boolean completed(Entity e) {
        return time >= totalTime;
    }
    
    @Override
    public float getEstimatedPercentComplete() {
        return time / totalTime;
    }
    
    @Override
    public String getTag() {
        return tag;
    }
}
