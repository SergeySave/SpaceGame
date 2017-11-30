package com.sergey.spacegame.common.ecs.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.VelocityComponent;

/**
 * This system is in charge of updating positions by velocities
 *
 * @author sergeys
 */
public class MovementSystem extends IteratingSystem {
    
    private Vector2 TMP = new Vector2();
    
    /**
     * Create a new MovementSystem
     */
    public MovementSystem() {
        super(Family.all(PositionComponent.class, VelocityComponent.class).get());
    }
    
    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PositionComponent pos = PositionComponent.MAPPER.get(entity);
        VelocityComponent vel = VelocityComponent.MAPPER.get(entity);
        pos.setFrom(pos.setVector(TMP).add(vel.vx * deltaTime, vel.vy * deltaTime));
    }
}
