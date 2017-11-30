package com.sergey.spacegame.common.game.orders;

import com.badlogic.ashley.core.Entity;
import com.sergey.spacegame.common.ecs.component.PositionComponent;
import com.sergey.spacegame.common.ecs.component.ShipComponent;
import com.sergey.spacegame.common.ecs.component.VelocityComponent;
import com.sergey.spacegame.common.game.Level;

/**
 * Represents a move order that is supposed to complete in a given amount of time
 *
 * @author sergeys
 */
public class TimeMoveOrder implements MovingOrder {
    
    private double  x;
    private double  y;
    private float   time;
    private boolean done;
    
    /**
     * Create a time move order
     *
     * @param x    - the x coordinate to move to
     * @param y    - the y coordinate to move to
     * @param time - the time to take to move to those coordinates
     */
    public TimeMoveOrder(double x, double y, float time) {
        this.x = x;
        this.y = y;
        this.time = time;
    }
    
    @Override
    public void update(Entity e, float deltaTime, Level level) {
        if (done) {
            return;
        }
        
        PositionComponent pos = PositionComponent.MAPPER.get(e);
        VelocityComponent vel = VelocityComponent.MAPPER.get(e);
        if (vel == null) {
            vel = new VelocityComponent();
            e.add(vel);
        }
    
        double dx = x - pos.getX();
        double dy = y - pos.getY();
        if (time < 0) {
            done = true;
            pos.setX((float) x);
            pos.setY((float) y);
            e.remove(VelocityComponent.class);
        } else {
            vel.vx = (float) (dx / time);
            vel.vy = (float) (dy / time);
            time -= deltaTime;
        }
    }
    
    @Override
    public boolean isValidFor(Entity e) {
        return PositionComponent.MAPPER.has(e) && ShipComponent.MAPPER.has(e);
    }
    
    @Override
    public boolean completed(Entity e) {
        return done;
    }
    
    @Override
    public float getPositionX() {
        return ((float) getX());
    }
    
    /**
     * Get the destination x coordinate
     *
     * @return the destination x coordinate
     */
    public double getX() {
        return x;
    }
    
    @Override
    public float getPositionY() {
        return ((float) getY());
    }
    
    /**
     * Get the destination y coordinate
     *
     * @return the destination y coordinate
     */
    public double getY() {
        return y;
    }
}
